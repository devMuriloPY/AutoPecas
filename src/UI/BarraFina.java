package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Barra de rolagem fina, sem setas, nas cores do tema. */
public class BarraFina extends BasicScrollBarUI {

    private final Color cor;

    private BarraFina(Color cor) {
        this.cor = cor;
    }

    /** Deixa a área de rolagem sem moldura, transparente e com a barra fina. */
    public static JScrollPane aplica(JScrollPane rolagem, Color corDaBarra) {
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        // redesenha tudo ao rolar: o vidro depende da posição na tela, não dá para só deslocar a imagem
        rolagem.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        rolagem.getVerticalScrollBar().setUI(new BarraFina(corDaBarra));
        rolagem.getVerticalScrollBar().setOpaque(false);
        rolagem.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        rolagem.getVerticalScrollBar().setUnitIncrement(18);
        rolagem.getHorizontalScrollBar().setUI(new BarraFina(corDaBarra));
        rolagem.getHorizontalScrollBar().setOpaque(false);
        rolagem.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return rolagem;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        // sem trilho
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g2 = Tema.g2(g);
        g2.setColor(cor);
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
        g2.dispose();
    }

    @Override
    protected JButton createDecreaseButton(int orientacao) {
        return invisivel();
    }

    @Override
    protected JButton createIncreaseButton(int orientacao) {
        return invisivel();
    }

    private static JButton invisivel() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }
}
