/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import Model.Cliente;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LabI
 */
public class ClienteDAO {

    public void salvar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO Clientes (nome, fantasia, CPF, CNPJ, Tipo, Logradouro, Bairro, `Número`, CEP, Cidade, Estado, Celular, Email) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, cliente.getNome());
            pst.setString(2, cliente.getFantasia());
            pst.setString(3, cliente.getCPF());
            pst.setString(4, cliente.getCNPJ());
            pst.setString(5, cliente.getTipo());
            pst.setString(6, cliente.getLogradouro());
            pst.setString(7, cliente.getBairro());
            pst.setString(8, cliente.getNúmero());
            pst.setString(9, cliente.getCEP());
            pst.setString(10, cliente.getCidade());
            pst.setString(11, cliente.getEstado());
            pst.setString(12, cliente.getCelular());
            pst.setString(13, cliente.getEmail());
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId_Cliente(rs.getInt(1)); // devolve o código gerado
                }
            }
        }
    }

    public Cliente buscarClienteCodigo(String codigo) throws SQLException {
        Cliente cli = new Cliente();
        int id;
        try {
            id = Integer.parseInt(codigo.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return cli; // código vazio, cancelado ou inválido: nenhum cliente
        }

        String sql = "SELECT * FROM Clientes WHERE id_cliente = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    cli = new Cliente(rs.getInt("id_cliente"), rs.getString("nome"), rs.getString("fantasia"),
                            rs.getString("CPF"), rs.getString("CNPJ"), rs.getString("Tipo"),
                            rs.getString("Logradouro"), rs.getString("Bairro"), rs.getString("Número"),
                            rs.getString("CEP"), rs.getString("Cidade"), rs.getString("Estado"),
                            rs.getString("Celular"), rs.getString("Email"));
                }
            }
        }
        return cli;
    }

    
    public void excluir(Cliente cliente) throws SQLException {
        String sql = "DELETE FROM Clientes WHERE id_cliente = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setInt(1, cliente.getId_Cliente());
            pst.executeUpdate();
        }
    }
    
    /**
     * Atualiza os dados do cliente identificado por getId_Cliente().
     * @return quantidade de linhas alteradas (0 se o código não existir)
     */
    public int atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE Clientes SET nome = ?, fantasia = ?, CPF = ?, CNPJ = ?, Tipo = ?, Logradouro = ?, "
                + "Bairro = ?, `Número` = ?, CEP = ?, Cidade = ?, Estado = ?, Celular = ?, Email = ? "
                + "WHERE id_cliente = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setString(1, cliente.getNome());
            pst.setString(2, cliente.getFantasia());
            pst.setString(3, cliente.getCPF());
            pst.setString(4, cliente.getCNPJ());
            pst.setString(5, cliente.getTipo());
            pst.setString(6, cliente.getLogradouro());
            pst.setString(7, cliente.getBairro());
            pst.setString(8, cliente.getNúmero());
            pst.setString(9, cliente.getCEP());
            pst.setString(10, cliente.getCidade());
            pst.setString(11, cliente.getEstado());
            pst.setString(12, cliente.getCelular());
            pst.setString(13, cliente.getEmail());
            pst.setInt(14, cliente.getId_Cliente());
            return pst.executeUpdate();
        }
    }

    /** Código e nome de todos, em ordem alfabética (para listas de escolha). */
    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nome FROM Clientes ORDER BY nome";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Cliente x = new Cliente();
                x.setId_Cliente(rs.getInt("id_cliente"));
                x.setNome(rs.getString("nome"));
                lista.add(x);
            }
        }
        return lista;
    }

    /** Lista os clientes cujo nome, nome fantasia, CPF ou CNPJ contém o termo, em ordem de nome. */
    public List<Cliente> pesquisar(String termo, int limite) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nome, fantasia, CPF, CNPJ, Cidade, Estado FROM Clientes "
                + "WHERE nome LIKE ? OR fantasia LIKE ? OR CPF LIKE ? OR CNPJ LIKE ? ORDER BY nome LIMIT ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            String filtro = ProdutoDAO.contem(termo);
            for (int i = 1; i <= 4; i++) {
                pst.setString(i, filtro);
            }
            pst.setInt(5, limite);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId_Cliente(rs.getInt("id_cliente"));
                    c.setNome(rs.getString("nome"));
                    c.setFantasia(rs.getString("fantasia"));
                    c.setCPF(rs.getString("CPF"));
                    c.setCNPJ(rs.getString("CNPJ"));
                    c.setCidade(rs.getString("Cidade"));
                    c.setEstado(rs.getString("Estado"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }
}
