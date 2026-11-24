package UI;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/** Caixa de seleção com o mesmo visual dos campos de texto. */
public class Seletor<T> extends JComboBox<T> {

    public Seletor() {
        super();
        estiliza();
    }

    public Seletor(T[] itens) {
        super(itens);
        estiliza();
    }

    private void estiliza() {
        setUI(new Visual());
        setOpaque(false);
        setFont(Tema.texto(java.awt.Font.PLAIN, 14f));
        setForeground(Tema.TINTA);
        setBackground(Tema.PAPEL);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setMaximumRowCount(10);
        setRenderer(new Item());
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    boolean temFoco() {
        return isFocusOwner() || isPopupVisible();
    }

    @Override
    public void updateUI() {
        // mantém o visual próprio mesmo se a aparência do Swing for reinstalada
        if (getUI() instanceof Visual) {
            return;
        }
        super.updateUI();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 38;
        return d;
    }

    private static final class Item extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice, boolean selecionado, boolean foco) {
            super.getListCellRendererComponent(lista, valor, indice, selecionado, false);
            setBorder(new EmptyBorder(7, 12, 7, 12));
            setFont(Tema.texto(java.awt.Font.PLAIN, 14f));
            if (indice < 0) {
                setOpaque(false); // valor mostrado na própria caixa: o fundo é pintado pelo Seletor
                setForeground(lista.isEnabled() ? Tema.TINTA : Tema.TINTA_SUAVE);
            } else {
                setOpaque(true);
                setBackground(selecionado ? Tema.AZUL_NEVOA : Tema.PAPEL);
                setForeground(Tema.TINTA);
            }
            return this;
        }
    }

    private static final class Visual extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                public void paint(Graphics g) {
                    Graphics2D g2 = Tema.g2(g);
                    int cx = getWidth() / 2 - 2, cy = getHeight() / 2;
                    g2.setColor(comboBox.isEnabled() ? Tema.AZUL : Tema.BORDA_ESCURA);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusable(false);
            return b;
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup p = new BasicComboPopup(comboBox);
            p.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
            p.getList().setBackground(Tema.PAPEL);
            return p;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Campo.pintaFundo(g, c);
            super.paint(g, c);
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle limites, boolean foco) {
            // o fundo já foi pintado em paint()
        }
    }
}
