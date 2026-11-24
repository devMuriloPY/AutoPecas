package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

/** Campo de texto do sistema, com texto de exemplo opcional. */
public class Campo extends JTextField {

    static final Color COR_EXEMPLO = new Color(0x9AA3B4);
    private String exemplo;

    public Campo() {
        prepara(this);
    }

    /** Texto cinza que aparece enquanto o campo está vazio. */
    public Campo exemplo(String texto) {
        this.exemplo = texto;
        repaint();
        return this;
    }

    public Campo aDireita() {
        setHorizontalAlignment(RIGHT);
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 38);
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        pintaFundo(g, this);
        super.paintComponent(g);
        pintaExemplo(g, this, exemplo);
    }

    // ------------------------------------------------------------ compartilhado com CampoSenha e Seletor

    static void prepara(JTextComponent c) {
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(8, 12, 8, 12));
        c.setFont(Tema.texto(java.awt.Font.PLAIN, 14f));
        c.setForeground(Tema.TINTA);
        c.setCaretColor(Tema.AZUL);
        c.setSelectionColor(Tema.AZUL_NEVOA);
        c.setSelectedTextColor(Tema.TINTA);
        c.setDisabledTextColor(Tema.TINTA_SUAVE);
        c.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                c.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.repaint();
            }
        });
    }

    /** Fundo arredondado: claro quando editável, contorno oliva quando em foco, apagado quando travado. */
    static void pintaFundo(Graphics g, JComponent c) {
        Graphics2D g2 = Tema.g2(g);
        int w = c.getWidth(), h = c.getHeight();
        boolean foco = c.isFocusOwner() || (c instanceof Seletor<?> s && s.temFoco());
        // vidro mais leitoso que o do cartão em volta, para o campo se destacar como área de digitação
        if (c.isEnabled()) {
            g2.setColor(new Color(255, 255, 255, foco ? 205 : 158));
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(foco ? Tema.AZUL : new Color(255, 255, 255, 235));
            g2.setStroke(new BasicStroke(foco ? 1.8f : 1.1f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 13, 13);
        } else {
            g2.setColor(new Color(255, 255, 255, 62));
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(new Color(255, 255, 255, 105));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 13, 13);
        }
        g2.dispose();
    }

    static void pintaExemplo(Graphics g, JTextComponent c, String exemplo) {
        if (exemplo == null || c.getDocument().getLength() > 0) {
            return;
        }
        Graphics2D g2 = Tema.g2(g);
        g2.setFont(c.getFont());
        g2.setColor(COR_EXEMPLO);
        FontMetrics fm = g2.getFontMetrics();
        Insets in = c.getInsets();
        int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(exemplo, in.left, y);
        g2.dispose();
    }
}
