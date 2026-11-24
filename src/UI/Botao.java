package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/** Botão do sistema, em forma de cápsula de vidro. O tipo define o peso visual: só um PRIMARIO por tela. */
public class Botao extends JButton {

    public enum Tipo {
        /** Ação principal: vidro azul royal. */
        PRIMARIO,
        /** Ações comuns: vidro claro. */
        SECUNDARIO,
        /** Ações que apagam dados: vidro claro com texto terracota. */
        PERIGO,
        /** Ação principal sobre vidro escuro: cápsula clara. */
        CLARO,
        /** Ação comum sobre vidro escuro: cápsula quase transparente. */
        CONTORNO_CLARO
    }

    private final Tipo tipo;
    private String atalho;

    public Botao(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setFont(Tema.texto(java.awt.Font.BOLD, 13.5f));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 20, 10, 20));
    }

    /** Mostra a tecla de atalho dentro do botão (ex.: "F2"). */
    public Botao atalho(String tecla) {
        this.atalho = tecla;
        revalidate();
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (atalho != null) {
            d.width += getFontMetrics(Tema.versalete(10f)).stringWidth(atalho) + 26;
        }
        d.height = Math.max(d.height, 38);
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.g2(g);
        int w = getWidth(), h = getHeight();
        ButtonModel m = getModel();
        boolean sobre = m.isRollover(), apertado = m.isArmed() && m.isPressed(), ativo = isEnabled();
        RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h);

        Color cima, baixo, texto, borda;
        switch (tipo) {
            case PRIMARIO -> {
                cima = apertado ? new Color(0x1E3BB8) : sobre ? new Color(0x4F72F5) : new Color(0x3A5FE8);
                baixo = apertado ? new Color(0x17309C) : sobre ? new Color(0x2447D6) : new Color(0x1E3FC4);
                texto = Tema.PAPEL;
                borda = new Color(255, 255, 255, 70);
            }
            case PERIGO -> {
                cima = new Color(255, 255, 255, apertado ? 120 : sobre ? 200 : 150);
                baixo = new Color(255, apertado ? 225 : 240, apertado ? 215 : 232, apertado ? 140 : sobre ? 170 : 110);
                texto = Tema.ALERTA;
                borda = new Color(0xC0, 0x39, 0x2B, sobre ? 190 : 120);
            }
            case CLARO -> {
                cima = new Color(255, 255, 255, apertado ? 190 : 255);
                baixo = new Color(0xE8, 0xEC, 0xF5, apertado ? 200 : sobre ? 255 : 235);
                texto = Tema.TINTA;
                borda = new Color(255, 255, 255, 220);
            }
            case CONTORNO_CLARO -> {
                cima = new Color(255, 255, 255, apertado ? 20 : sobre ? 58 : 34);
                baixo = new Color(255, 255, 255, apertado ? 12 : sobre ? 34 : 14);
                texto = Tema.PAPEL;
                borda = new Color(255, 255, 255, sobre ? 150 : 100);
            }
            default -> {
                cima = new Color(255, 255, 255, apertado ? 120 : sobre ? 215 : 165);
                baixo = new Color(255, 255, 255, apertado ? 90 : sobre ? 150 : 105);
                texto = Tema.AZUL_ESCURO;
                borda = new Color(255, 255, 255, 235);
            }
        }
        if (!ativo) {
            boolean escuro = tipo == Tipo.CLARO || tipo == Tipo.CONTORNO_CLARO;
            cima = baixo = new Color(255, 255, 255, escuro ? 22 : 56);
            texto = escuro ? new Color(255, 255, 255, 105) : new Color(0x15, 0x1A, 0x26, 96);
            borda = new Color(255, 255, 255, escuro ? 40 : 110);
        }

        g2.setPaint(new GradientPaint(0, 0, cima, 0, h, baixo));
        g2.fill(forma);
        if (ativo && tipo == Tipo.PRIMARIO) {
            // reflexo na metade de cima, como numa gota
            Shape recorte = g2.getClip();
            g2.clip(forma);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 70), 0, h * 0.55f, new Color(255, 255, 255, 0)));
            g2.fillRoundRect(2, 1, w - 4, h / 2, h, h);
            g2.setClip(recorte);
        }
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(forma);
        if (isFocusOwner() && ativo) {
            g2.setColor(tipo == Tipo.PRIMARIO || tipo == Tipo.CONTORNO_CLARO ? new Color(255, 255, 255, 190) : Tema.AZUL);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new RoundRectangle2D.Float(3f, 3f, w - 6, h - 6, h - 6, h - 6));
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        FontMetrics fa = g2.getFontMetrics(Tema.versalete(10f));
        int larguraTexto = fm.stringWidth(getText());
        int larguraAtalho = atalho == null ? 0 : fa.stringWidth(atalho) + 14;
        int espaco = atalho == null ? 0 : 10;
        int x = (w - larguraTexto - espaco - larguraAtalho) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(texto);
        g2.drawString(getText(), x, y);

        if (atalho != null) {
            int ax = x + larguraTexto + espaco, ah = fa.getHeight() + 4, ay = (h - ah) / 2;
            g2.setColor(new Color(texto.getRed(), texto.getGreen(), texto.getBlue(), 40));
            g2.fillRoundRect(ax, ay, larguraAtalho, ah, ah, ah);
            g2.setFont(Tema.versalete(10f));
            g2.setColor(texto);
            g2.drawString(atalho, ax + 7, ay + 2 + fa.getAscent());
        }
        g2.dispose();
    }
}
