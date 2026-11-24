package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Efeito de vidro ("liquid glass") das telas.
 *
 * Um painel de vidro mostra, por trás dele, o fundo da janela desfocado; por cima vai uma
 * película translúcida, um brilho suave no topo e uma borda clara. É isso que dá a sensação
 * de material fosco em vez de uma simples caixa colorida.
 */
public final class Vidro {

    /** Quem fornece o fundo que aparece desfocado atrás dos vidros (a janela ou um diálogo). */
    public interface Fundo {

        /** Imagem do fundo já desfocada, do tamanho do componente. Pode ser null antes de aparecer. */
        BufferedImage desfocado();
    }

    public enum Tom {
        /** Vidro transparente: cartões, menu e formulários. */
        CLARO,
        /** Vidro azul royal, para um bloco que precise de destaque forte. */
        ESCURO
    }

    private Vidro() {
    }

    /** Pinta o componente inteiro como uma placa de vidro. */
    public static void pinta(Graphics g, JComponent c, int raio, Tom tom) {
        pinta(g, c, 0, 0, c.getWidth(), c.getHeight(), raio, tom);
    }

    public static void pinta(Graphics g, JComponent c, int x, int y, int w, int h, int raio, Tom tom) {
        Graphics2D g2 = Tema.g2(g);
        RoundRectangle2D forma = new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, raio, raio);
        Shape recorteAntes = g2.getClip();
        g2.clip(forma);

        // 1. o que está atrás, desfocado
        Component dono = SwingUtilities.getAncestorOfClass(Fundo.class, c);
        BufferedImage atras = dono == null ? null : ((Fundo) dono).desfocado();
        if (atras != null) {
            Point p = SwingUtilities.convertPoint(c, 0, 0, dono);
            g2.drawImage(atras, -p.x, -p.y, null);
        } else {
            g2.setColor(Tema.NEVE);
            g2.fill(forma);
        }

        // 2. película
        boolean claro = tom == Tom.CLARO;
        g2.setColor(claro ? new Color(255, 255, 255, 64) : new Color(0x17, 0x30, 0x9C, 205));
        g2.fill(forma);

        // 3. brilho que escorre do topo
        g2.setPaint(new GradientPaint(0, y, new Color(255, 255, 255, claro ? 96 : 40),
                0, y + h * 0.45f, new Color(255, 255, 255, 0)));
        g2.fill(forma);
        g2.setClip(recorteAntes);

        // 4. borda: mais clara no canto de cima, como luz batendo na aresta
        g2.setStroke(new BasicStroke(1.3f));
        g2.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, claro ? 255 : 110),
                x + w, y + h, new Color(255, 255, 255, claro ? 120 : 30)));
        g2.draw(forma);
        g2.dispose();
    }

    /** Sombra suave em volta de uma placa (para diálogos e painéis que "flutuam"). */
    public static void sombra(Graphics2D g2, int x, int y, int w, int h, int raio) {
        for (int i = 12; i >= 1; i--) {
            g2.setColor(new Color(0x0E, 0x14, 0x28, 5));
            int d = i * 2;
            g2.fillRoundRect(x - d, y - d + 8, w + 2 * d, h + 2 * d, raio + d, raio + d);
        }
    }

    /**
     * Desfoque rápido: reduz a imagem pela metade várias vezes (cada passo mistura os vizinhos)
     * e depois amplia de volta com suavização. Não deixa faixas nas bordas.
     */
    public static BufferedImage desfoca(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage atual = img;
        for (int divisor = 2; divisor <= 32; divisor *= 2) {
            atual = escala(atual, Math.max(1, w / divisor), Math.max(1, h / divisor), RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        atual = escala(atual, Math.max(1, w / 6), Math.max(1, h / 6), RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return escala(atual, w, h, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private static BufferedImage escala(BufferedImage origem, int w, int h, Object interpolacao) {
        BufferedImage destino = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolacao);
        g.drawImage(origem, 0, 0, w, h, null);
        g.dispose();
        return destino;
    }
}
