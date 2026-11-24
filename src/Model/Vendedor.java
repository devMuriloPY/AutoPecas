/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author wmsup
 */
public class Vendedor {
    private int id_Vendedor;
    private String Nome;
    private String CPF;
    private String Logradouro;
    private String Bairro;
    private String Numero;
    private String CEP;
    private String Cidade;
    private String Estado;
    private String Celular;
    private String Email;

    public Vendedor() {
    }

    public Vendedor(int id_Vendedor, String Nome, String CPF, String Logradouro, String Bairro, String Numero, String CEP, String Cidade, String Estado, String Celular, String Email) {
        this.id_Vendedor = id_Vendedor;
        this.Nome = Nome;
        this.CPF = CPF;
        this.Logradouro = Logradouro;
        this.Bairro = Bairro;
        this.Numero = Numero;
        this.CEP = CEP;
        this.Cidade = Cidade;
        this.Estado = Estado;
        this.Celular = Celular;
        this.Email = Email;
    }

    public int getId_Vendedor() {
        return id_Vendedor;
    }

    public void setId_Vendedor(int id_Vendedor) {
        this.id_Vendedor = id_Vendedor;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getLogradouro() {
        return Logradouro;
    }

    public void setLogradouro(String Logradouro) {
        this.Logradouro = Logradouro;
    }

    public String getBairro() {
        return Bairro;
    }

    public void setBairro(String Bairro) {
        this.Bairro = Bairro;
    }

    public String getNumero() {
        return Numero;
    }

    public void setNumero(String Numero) {
        this.Numero = Numero;
    }

    public String getCEP() {
        return CEP;
    }

    public void setCEP(String CEP) {
        this.CEP = CEP;
    }

    public String getCidade() {
        return Cidade;
    }

    public void setCidade(String Cidade) {
        this.Cidade = Cidade;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String Estado) {
        this.Estado = Estado;
    }

    public String getCelular() {
        return Celular;
    }

    public void setCelular(String Celular) {
        this.Celular = Celular;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }
}
