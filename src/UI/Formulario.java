package UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Monta formulários em linhas. Cada célula tem um rótulo em cima e o campo embaixo,
 * e ocupa uma fração da linha proporcional ao seu "peso".
 */
public class Formulario extends JPanel {

    private boolean vazio = true;

    public Formulario() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /** Uma célula: rótulo + campo, com a largura relativa dentro da linha. */
    public record Celula(String rotulo, JComponent campo, int peso) {
    }

    public static Celula cel(String rotulo, JComponent campo, int peso) {
        return new Celula(rotulo, campo, peso);
    }

    /** Campo com um botão colado à direita (ex.: CEP + "Buscar CEP"). */
    public static JComponent junto(JComponent campo, JComponent botao) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(campo, BorderLayout.CENTER);
        p.add(botao, BorderLayout.EAST);
        return p;
    }

    /** Título de um grupo de campos, com um traço fino ao lado. */
    public void secao(String titulo) {
        if (!vazio) {
            add(Box.createVerticalStrut(16));
        }
        JLabel l = Tema.rotulo(titulo, Tema.AZUL);
        l.setFont(Tema.versalete(11.5f));
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                int x = l.getX() + l.getWidth() + 12;
                g.setColor(new java.awt.Color(0x15, 0x1A, 0x26, 40));
                g.fillRect(x, getHeight() / 2, Math.max(0, getWidth() - x), 1);
            }
        };
        p.setOpaque(false);
        p.add(l, BorderLayout.WEST);
        fixaAltura(p);
        add(p);
        add(Box.createVerticalStrut(14));
        vazio = false;
    }

    public void linha(Celula... celulas) {
        JPanel linha = new JPanel(new GridBagLayout());
        linha.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        for (int i = 0; i < celulas.length; i++) {
            Celula cel = celulas[i];
            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setOpaque(false);
            p.add(Tema.rotulo(cel.rotulo()), BorderLayout.NORTH);
            p.add(cel.campo(), BorderLayout.CENTER);
            // largura preferida mínima: assim a sobra da linha é dividida pelos pesos
            int altura = p.getPreferredSize().height;
            p.setPreferredSize(new Dimension(cel.peso(), altura));
            p.setMinimumSize(new Dimension(cel.peso(), altura));
            c.gridx = i;
            c.weightx = cel.peso();
            c.insets = new Insets(0, 0, 0, i == celulas.length - 1 ? 0 : 16);
            linha.add(p, c);
        }
        fixaAltura(linha);
        add(linha);
        add(Box.createVerticalStrut(14));
        vazio = false;
    }

    private static void fixaAltura(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }
}
