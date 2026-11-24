/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Produto;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wmsup
 */
public class ProdutoDAO {

    public void salvar(Produto produto) throws SQLException {
        String sql = "INSERT INTO Produtos (nome, codigo_barras, unidade, marca, preco_custo, margem_lucro, preco_venda, status, foto) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preenche(pst, produto);
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId_Produto(rs.getInt(1)); // devolve o código gerado
                }
            }
        }
    }

    /**
     * Atualiza os dados do produto identificado por getId_Produto().
     * @return quantidade de linhas alteradas (0 se o código não existir)
     */
    public int atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE Produtos SET nome = ?, codigo_barras = ?, unidade = ?, marca = ?, preco_custo = ?, "
                + "margem_lucro = ?, preco_venda = ?, status = ?, foto = ? WHERE id_produto = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            preenche(pst, produto);
            pst.setInt(10, produto.getId_Produto());
            return pst.executeUpdate();
        }
    }

    /** Parâmetros 1 a 9, na mesma ordem no INSERT e no UPDATE. */
    private void preenche(PreparedStatement pst, Produto produto) throws SQLException {
        pst.setString(1, produto.getNome());
        pst.setString(2, produto.getCodigoBarras());
        pst.setString(3, produto.getUnidade());
        pst.setString(4, produto.getMarca());
        pst.setBigDecimal(5, produto.getPrecoCusto());
        pst.setBigDecimal(6, produto.getMargemLucro());
        pst.setBigDecimal(7, produto.getPrecoVenda());
        pst.setString(8, produto.getStatus());
        if (produto.getFoto() == null) {
            pst.setNull(9, Types.BLOB);
        } else {
            pst.setBytes(9, produto.getFoto());
        }
    }

    /**
     * Procura pelo que foi digitado ou bipado: primeiro como código de barras,
     * depois como código interno do produto.
     * @return o produto, ou null se nada for encontrado
     */
    public Produto buscar(String codigoOuBarras) throws SQLException {
        if (codigoOuBarras == null || codigoOuBarras.isBlank()) {
            return null;
        }
        String texto = codigoOuBarras.trim();
        int id = -1;
        if (texto.matches("\\d{1,9}")) {
            id = Integer.parseInt(texto);
        }

        String sql = "SELECT * FROM Produtos WHERE codigo_barras = ? OR id_produto = ? "
                + "ORDER BY (codigo_barras = ?) DESC LIMIT 1";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setString(1, texto);
            pst.setInt(2, id);
            pst.setString(3, texto);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return monta(rs, true);
            }
        }
    }

    public void excluir(Produto produto) throws SQLException {
        String sql = "DELETE FROM Produtos WHERE id_produto = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setInt(1, produto.getId_Produto());
            pst.executeUpdate();
        }
    }

    /** @return o produto completo (com foto), ou null se o código não existir */
    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Produtos WHERE id_produto = ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? monta(rs, true) : null;
            }
        }
    }

    /**
     * Lista os produtos cujo nome, marca ou código de barras contém o termo, em ordem de nome.
     * Não traz a foto, para a lista ficar leve.
     */
    public List<Produto> pesquisar(String termo, int limite) throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT id_produto, nome, codigo_barras, unidade, marca, preco_custo, margem_lucro, preco_venda, status "
                + "FROM Produtos WHERE nome LIKE ? OR marca LIKE ? OR codigo_barras LIKE ? ORDER BY nome LIMIT ?";
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            String filtro = contem(termo);
            pst.setString(1, filtro);
            pst.setString(2, filtro);
            pst.setString(3, filtro);
            pst.setInt(4, limite);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(monta(rs, false));
                }
            }
        }
        return lista;
    }

    private Produto monta(ResultSet rs, boolean comFoto) throws SQLException {
        Produto p = new Produto();
        p.setId_Produto(rs.getInt("id_produto"));
        p.setNome(rs.getString("nome"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setUnidade(rs.getString("unidade"));
        p.setMarca(rs.getString("marca"));
        p.setPrecoCusto(rs.getBigDecimal("preco_custo"));
        p.setMargemLucro(rs.getBigDecimal("margem_lucro"));
        p.setPrecoVenda(rs.getBigDecimal("preco_venda"));
        p.setStatus(rs.getString("status"));
        if (comFoto) {
            p.setFoto(rs.getBytes("foto"));
        }
        return p;
    }

    /** Transforma o que foi digitado em filtro "contém", sem deixar % e _ agirem como curingas. */
    static String contem(String termo) {
        String t = termo == null ? "" : termo.trim();
        return "%" + t.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
