package Model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Um produto dentro de uma venda, com a quantidade e o preço praticado naquele momento. */
public class ItemVenda {

    private final Produto produto;
    private final BigDecimal precoUnitario;
    private BigDecimal quantidade;

    public ItemVenda(Produto produto, BigDecimal quantidade) {
        this.produto = produto;
        this.precoUnitario = produto.getPrecoVenda();
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(quantidade).setScale(2, RoundingMode.HALF_UP);
    }
}
