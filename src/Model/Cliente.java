/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author wmsup
 */
public class Cliente {
    private int id_Cliente;
    private String Nome;
    private String Fantasia;
    private String CPF;
    private String CNPJ;
    private String Tipo;
    private String Logradouro;
    private String Bairro;
    private String Número;
    private String CEP;
    private String Cidade;
    private String Estado;
    private String Celular;
    private String Email;

    public Cliente(int id_Cliente, String Nome, String Fantasia, String CPF, String CNPJ, String Tipo, String Logradouro, String Bairro, String Número, String CEP, String Cidade, String Estado, String Celular, String Email) {
        this.id_Cliente = id_Cliente;
        this.Nome = Nome;
        this.Fantasia = Fantasia;
        this.CPF = CPF;
        this.CNPJ = CNPJ;
        this.Tipo = Tipo;
        this.Logradouro = Logradouro;
        this.Bairro = Bairro;
        this.Número = Número;
        this.CEP = CEP;
        this.Cidade = Cidade;
        this.Estado = Estado;
        this.Celular = Celular;
        this.Email = Email;
    }

    public Cliente() {    }   

    public int getId_Cliente() {
        return id_Cliente;
    }

    public void setId_Cliente(int id_Cliente) {
        this.id_Cliente = id_Cliente;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }


    public String getFantasia() {
        return Fantasia;
    }

    public void setFantasia(String Fantasia) {
        this.Fantasia = Fantasia;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getCNPJ() {
        return CNPJ;
    }

    public void setCNPJ(String CNPJ) {
        this.CNPJ = CNPJ;
    }

    public String getTipo() {
        return Tipo;
    }

    public void setTipo(String Tipo) {
        this.Tipo = Tipo;
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

    public String getNúmero() {
        return Número;
    }

    public void setNúmero(String Número) {
        this.Número = Número;
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
