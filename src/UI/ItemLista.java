package UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Uma linha de resultado de pesquisa (produto, cliente ou vendedor).
 *
 * @param id código do registro
 * @param titulo texto principal (nome)
 * @param detalhe segunda linha, menor (código, marca, documento, cidade...)
 * @param direita texto alinhado à direita (preço), ou null
 * @param apagado true para mostrar esmaecido (ex.: produto inativo)
 */
public record ItemLista(int id, String titulo, String detalhe, String direita, boolean apagado) {

    public static final int ALTURA = 50;

    /** Desenha cada linha em duas alturas de texto, com o realce na linha selecionada. */
    public static final class Desenho extends JComponent implements ListCellRenderer<ItemLista> {

        private ItemLista item;
        private boolean selecionado;

        public Desenho() {
            setPreferredSize(new Dimension(100, ALTURA));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ItemLista> lista, ItemLista valor, int indice,
                boolean selecionado, boolean foco) {
            this.item = valor;
            this.selecionado = selecionado;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            int w = getWidth(), h = getHeight(), margem = 14;
            if (selecionado) {
                g2.setColor(new Color(0x24, 0x47, 0xD6, 34));
                g2.fillRoundRect(4, 2, w - 8, h - 4, 16, 16);
            } else {
                g2.setColor(new Color(0x15, 0x1A, 0x26, 22));
                g2.fillRect(margem, h - 1, w - 2 * margem, 1);
            }
            int larguraDireita = 0;
            if (item.direita() != null) {
                g2.setFont(Tema.display(Font.BOLD, 15.5f));
                FontMetrics fd = g2.getFontMetrics();
                larguraDireita = fd.stringWidth(item.direita()) + 16;
                g2.setColor(item.apagado() ? Tema.BORDA_ESCURA : Tema.TINTA);
                g2.drawString(item.direita(), w - margem - fd.stringWidth(item.direita()), (h - fd.getHeight()) / 2 + fd.getAscent());
            }
            int largura = w - 2 * margem - larguraDireita;
            g2.setFont(Tema.texto(Font.BOLD, 14f));
            g2.setColor(item.apagado() ? Tema.BORDA_ESCURA : Tema.TINTA);
            g2.drawString(corta(item.titulo(), g2.getFontMetrics(), largura), margem, 21);
            g2.setFont(Tema.texto(Font.PLAIN, 12f));
            g2.setColor(item.apagado() ? Tema.BORDA_ESCURA : Tema.TINTA_SUAVE);
            g2.drawString(corta(item.detalhe(), g2.getFontMetrics(), largura), margem, 39);
            g2.dispose();
        }

        private static String corta(String texto, FontMetrics fm, int largura) {
            if (texto == null) {
                return "";
            }
            if (fm.stringWidth(texto) <= largura) {
                return texto;
            }
            String s = texto;
            while (s.length() > 1 && fm.stringWidth(s + "…") > largura) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "…";
        }
    }
}
