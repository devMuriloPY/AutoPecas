package UI;

import java.awt.Graphics;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Bloco de conteúdo: uma placa de vidro com cantos arredondados. */
public class Cartao extends JPanel {

    private final Vidro.Tom tom;
    private final int raio;

    /** Cartão de vidro claro, o padrão das telas. */
    public Cartao(LayoutManager layout) {
        this(layout, Vidro.Tom.CLARO, 26);
    }

    public Cartao(LayoutManager layout, Vidro.Tom tom, int raio) {
        super(layout);
        this.tom = tom;
        this.raio = raio;
        setOpaque(false);
        setBorder(new EmptyBorder(22, 24, 24, 24));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Vidro.pinta(g, this, raio, tom);
    }
}
