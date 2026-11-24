package Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Funções de apoio para texto e valores monetários (padrão brasileiro). */
public final class Texto {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private Texto() {
    }

    /** Mantém só os dígitos: "01.001-000" vira "01001000". Nulo vira "". */
    public static String soDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /**
     * Converte "1.234,56", "1234,56" ou "1234.56" em BigDecimal.
     * @return null se o texto estiver vazio
     * @throws NumberFormatException se não for um número
     */
    public static BigDecimal parseDecimal(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.replace("R$", "").replace("%", "").replaceAll("\\s", "");
        if (t.isEmpty()) {
            return null;
        }
        if (t.contains(",")) {
            t = t.replace(".", "").replace(',', '.'); // "1.234,56" -> "1234.56"
        }
        return new BigDecimal(t);
    }

    /** Formata com duas casas e vírgula: 1234.5 vira "1234,50". */
    public static String formata(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(PT_BR)).format(valor);
    }

    /** Preço de venda = custo + margem de lucro (%) sobre o custo, com duas casas. */
    public static BigDecimal calculaPrecoVenda(BigDecimal custo, BigDecimal margemPercentual) {
        BigDecimal fator = BigDecimal.ONE.add(margemPercentual.divide(CEM));
        return custo.multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }

    /** Valor em reais com separador de milhar: 1234.5 vira "R$ 1.234,50". */
    public static String moeda(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return "R$ " + new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(PT_BR)).format(valor);
    }

    /** Quantidade sem zeros sobrando: 2.000 vira "2", 1.500 vira "1,5". */
    public static String quantidade(BigDecimal quantidade) {
        if (quantidade == null) {
            return "";
        }
        return quantidade.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
