/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Vendedor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wmsup
 */
public class VendedorDAO {

    public void salvar(Vendedor vendedor) throws SQLException {
        String sql = "INSERT INTO Vendedores (nome, CPF, Logradouro, Bairro, `Número`, CEP, Cidade, Estado, Celular, Email) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, vendedor.getNome());
            pst.setString(2, vendedor.getCPF());
            pst.setString(3, vendedor.getLogradouro());
            pst.setString(4, vendedor.getBairro());
            pst.setString(5, vendedor.getNumero());
            pst.setString(6, vendedor.getCEP());
            pst.setString(7, vendedor.getCidade());
            pst.setString(8, vendedor.getEstado());
            pst.setString(9, vendedor.getCelular());
            pst.setString(10, vendedor.getEmail());
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    vendedor.setId_Vendedor(rs.getInt(1)); // devolve o código gerado
                }
            }
        }
    }

    public Vendedor buscarVendedorCodigo(String codigo) throws SQLException {
        Vendedor ven = new Vendedor();
        int id;
        try {
            id = Integer.parseInt(codigo.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return ven; // código vazio, cancelado ou inválido: nenhum vendedor
        }

        String sql = "SELECT * FROM Vendedores WHERE id_vendedor = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    ven = new Vendedor(rs.getInt("id_vendedor"), rs.getString("nome"),
                            rs.getString("CPF"), rs.getString("Logradouro"),
                            rs.getString("Bairro"), rs.getString("Número"), rs.getString("CEP"),
                            rs.getString("Cidade"), rs.getString("Estado"), rs.getString("Celular"),
                            rs.getString("Email"));
                }
            }
        }
        return ven;
    }

    /**
     * Atualiza os dados do vendedor identificado por getId_Vendedor().
     * @return quantidade de linhas alteradas (0 se o código não existir)
     */
    public int atualizar(Vendedor vendedor) throws SQLException {
        String sql = "UPDATE Vendedores SET nome = ?, CPF = ?, Logradouro = ?, "
                + "Bairro = ?, `Número` = ?, CEP = ?, Cidade = ?, Estado = ?, Celular = ?, Email = ? "
                + "WHERE id_vendedor = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setString(1, vendedor.getNome());
            pst.setString(2, vendedor.getCPF());
            pst.setString(3, vendedor.getLogradouro());
            pst.setString(4, vendedor.getBairro());
            pst.setString(5, vendedor.getNumero());
            pst.setString(6, vendedor.getCEP());
            pst.setString(7, vendedor.getCidade());
            pst.setString(8, vendedor.getEstado());
            pst.setString(9, vendedor.getCelular());
            pst.setString(10, vendedor.getEmail());
            pst.setInt(11, vendedor.getId_Vendedor());
            return pst.executeUpdate();
        }
    }

    public void excluir(Vendedor vendedor) throws SQLException {
        String sql = "DELETE FROM Vendedores WHERE id_vendedor = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setInt(1, vendedor.getId_Vendedor());
            pst.executeUpdate();
        }
    }

    /** Código e nome de todos, em ordem alfabética (para listas de escolha). */
    public List<Vendedor> listar() throws SQLException {
        List<Vendedor> lista = new ArrayList<>();
        String sql = "SELECT id_vendedor, nome FROM Vendedores ORDER BY nome";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Vendedor x = new Vendedor();
                x.setId_Vendedor(rs.getInt("id_vendedor"));
                x.setNome(rs.getString("nome"));
                lista.add(x);
            }
        }
        return lista;
    }

    /** Lista os vendedores cujo nome ou CPF contém o termo, em ordem de nome. */
    public List<Vendedor> pesquisar(String termo, int limite) throws SQLException {
        List<Vendedor> lista = new ArrayList<>();
        String sql = "SELECT id_vendedor, nome, CPF, Cidade, Estado FROM Vendedores "
                + "WHERE nome LIKE ? OR CPF LIKE ? ORDER BY nome LIMIT ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            String filtro = ProdutoDAO.contem(termo);
            pst.setString(1, filtro);
            pst.setString(2, filtro);
            pst.setInt(3, limite);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Vendedor v = new Vendedor();
                    v.setId_Vendedor(rs.getInt("id_vendedor"));
                    v.setNome(rs.getString("nome"));
                    v.setCPF(rs.getString("CPF"));
                    v.setCidade(rs.getString("Cidade"));
                    v.setEstado(rs.getString("Estado"));
                    lista.add(v);
                }
            }
        }
        return lista;
    }
}
