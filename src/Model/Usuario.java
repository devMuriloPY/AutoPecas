/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author wmsup
 */
public class Usuario {
    private int id_usuario;
    private String login;
    private String senha;
    private String tipo;

    public Usuario(int codigo, String login, String senha, String tipo) {
        this.id_usuario = codigo;
        this.login = login;
        this.senha = senha;
        this.tipo = tipo;
    }

    public Usuario() {
    }

    public int getCodigo() {
        return id_usuario;
    }

    public void setCodigo(int codigo) {
        this.id_usuario = codigo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    
}
