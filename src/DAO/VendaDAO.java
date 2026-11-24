package DAO;

import Model.ItemVenda;
import Model.Venda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class VendaDAO {

    /**
     * Grava a venda e todos os itens de uma vez só (transação):
     * se qualquer item falhar, nada fica gravado pela metade.
     */
    public void salvar(Venda venda) throws SQLException {
        Connection con = Conexao.getInstance();
        boolean autoCommitAntes = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            String sqlVenda = "INSERT INTO Vendas (id_cliente, id_vendedor, id_usuario, forma_pagamento, total, valor_recebido, troco) "
                    + "VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement pst = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                pst.setObject(1, venda.getIdCliente(), Types.INTEGER);
                pst.setObject(2, venda.getIdVendedor(), Types.INTEGER);
                pst.setObject(3, venda.getIdUsuario(), Types.INTEGER);
                pst.setString(4, venda.getFormaPagamento());
                pst.setBigDecimal(5, venda.getTotal());
                pst.setBigDecimal(6, venda.getValorRecebido());
                pst.setBigDecimal(7, venda.getTroco());
                pst.executeUpdate();
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    rs.next();
                    venda.setId_Venda(rs.getInt(1));
                }
            }

            String sqlItem = "INSERT INTO ItensVenda (id_venda, id_produto, descricao, quantidade, preco_unitario, subtotal) "
                    + "VALUES (?,?,?,?,?,?)";
            try (PreparedStatement pst = con.prepareStatement(sqlItem)) {
                for (ItemVenda item : venda.getItens()) {
                    pst.setInt(1, venda.getId_Venda());
                    pst.setInt(2, item.getProduto().getId_Produto());
                    pst.setString(3, item.getProduto().getNome());
                    pst.setBigDecimal(4, item.getQuantidade());
                    pst.setBigDecimal(5, item.getPrecoUnitario());
                    pst.setBigDecimal(6, item.getSubtotal());
                    pst.addBatch();
                }
                pst.executeBatch();
            }
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(autoCommitAntes);
        }
    }
}
