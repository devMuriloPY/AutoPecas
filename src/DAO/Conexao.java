package DAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Conexao implements Serializable {

    private static Conexao conexao = null;
    private static Connection connection;
    private static SQLException falha;
    private String usuario;
    private String senha;
    private String url;

    /** Arquivo com o acesso ao banco. Fica fora do controle de versão: cada máquina tem o seu. */
    private static final String ARQUIVO = "config.properties";

    private Conexao() {
        // O acesso ao banco vem do config.properties (modelo em config.exemplo.properties).
        // A senha não fica no código para não ir parar no repositório.
        Properties config = new Properties();
        try (InputStream in = Files.newInputStream(Path.of(ARQUIVO))) {
            config.load(in);
        } catch (IOException e) {
            falha = new SQLException("Não encontrei o arquivo " + Path.of(ARQUIVO).toAbsolutePath()
                    + ". Copie config.exemplo.properties para config.properties e informe a senha do MySQL.", e);
            return;
        }
        url = config.getProperty("db.url", "jdbc:mysql://localhost:3306/autopecas").trim();
        usuario = config.getProperty("db.usuario", "root").trim();
        senha = config.getProperty("db.senha", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, usuario, senha);
        } catch (ClassNotFoundException e) {
            falha = new SQLException("Driver do MySQL não encontrado no projeto", e);
        } catch (SQLException e) {
            falha = e;
        }
    }

    /**
     * Devolve a conexão com o banco, abrindo (ou reabrindo) quando preciso.
     * @throws SQLException se o banco estiver fora do ar, em vez de devolver null
     */
    public static Connection getInstance() throws SQLException {
        synchronized (Conexao.class) {
            if (conexao == null || connection == null || connection.isClosed()) {
                falha = null;
                connection = null;
                conexao = new Conexao();
            }
            if (connection == null) {
                // sem arquivo de configuração a mensagem já diz o que fazer; senão é o MySQL que não respondeu
                String motivo = falha != null && falha.getCause() instanceof IOException ? falha.getMessage()
                        : "Não foi possível conectar ao banco de dados. Verifique se o MySQL está ligado e os dados do "
                        + ARQUIVO + ".";
                throw new SQLException(motivo, falha);
            }
            return connection;
        }
    }

    public static void closeInstance() throws SQLException {
        if (conexao != null) {
            connection.close();
        }
    }

    public static void setAutoCommit(boolean vlr) throws SQLException {
        connection.setAutoCommit(vlr);
    }

    public static void commit() throws SQLException {
        connection.commit();
    }

    public static void rollback() throws SQLException {
        connection.rollback();
    }
}
