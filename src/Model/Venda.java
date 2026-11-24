package Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Uma venda fechada no PDV. Cliente e vendedor são opcionais (null = não informado). */
public class Venda {

    private int id_Venda;
    private Integer idCliente;
    private Integer idVendedor;
    private Integer idUsuario;
    private String formaPagamento;
    private BigDecimal valorRecebido;
    private BigDecimal troco;
    private final List<ItemVenda> itens = new ArrayList<>();

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public int getId_Venda() {
        return id_Venda;
    }

    public void setId_Venda(int id_Venda) {
        this.id_Venda = id_Venda;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(BigDecimal valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public BigDecimal getTroco() {
        return troco;
    }

    public void setTroco(BigDecimal troco) {
        this.troco = troco;
    }
}
