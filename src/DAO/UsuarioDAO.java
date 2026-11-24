/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Usuario;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
/**
 *
 * @author wmsup
 */
public class UsuarioDAO {

    public Usuario buscaLogin(String login, String senha) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE BINARY login = ? AND BINARY senha = ?";
        Usuario usuario = null;
        try (PreparedStatement pst = Conexao.getInstance().prepareStatement(sql)) {
            pst.setString(1, login);
            pst.setString(2, senha);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(rs.getInt("id_usuario"), rs.getString("login"), rs.getString("senha"), rs.getString("tipo"));
                }
            }
        }
        return usuario;
    }
}
