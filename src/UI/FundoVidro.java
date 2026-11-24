package UI;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Fundo das janelas: claro e frio, com manchas de luz azul royal e o desenho de porcas da casa.
 * Os painéis de vidro colocados em cima dele mostram este mesmo fundo desfocado.
 */
public class FundoVidro extends JPanel implements Vidro.Fundo {

    private BufferedImage nitido;
    private BufferedImage desfocado;

    public FundoVidro(LayoutManager layout) {
        super(layout);
        setOpaque(true);
        setBackground(Tema.GELO);
    }

    @Override
    public BufferedImage desfocado() {
        prepara();
        return desfocado;
    }

    @Override
    protected void paintComponent(Graphics g) {
        prepara();
        if (nitido != null) {
            g.drawImage(nitido, 0, 0, null);
        } else {
            super.paintComponent(g);
        }
    }

    /** Refaz as duas imagens só quando o tamanho da janela muda. */
    private void prepara() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (nitido == null || nitido.getWidth() != w || nitido.getHeight() != h) {
            nitido = desenha(w, h);
            desfocado = Vidro.desfoca(nitido);
        }
    }

    /** O desenho do fundo, também usado pelos diálogos que abrem sem uma janela por trás. */
    public static BufferedImage desenha(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        float maior = Math.max(w, h);

        g2.setPaint(new GradientPaint(0, 0, new Color(0xF4F6FA), w, h, new Color(0xE2E7F0)));
        g2.fillRect(0, 0, w, h);
        mancha(g2, w * 0.92f, h * 0.02f, maior * 0.50f, new Color(0x24, 0x47, 0xD6, 52), w, h);
        mancha(g2, w * 0.04f, h * 1.02f, maior * 0.46f, new Color(0x6C, 0x8C, 0xFF, 46), w, h);
        mancha(g2, w * 0.60f, h * 0.78f, maior * 0.30f, new Color(0xB8, 0xC4, 0xDA, 105), w, h);
        mancha(g2, w * 0.26f, h * 0.10f, maior * 0.30f, new Color(255, 255, 255, 220), w, h);

        // porcas: nítidas no fundo, borradas por trás dos vidros
        Marca.contorno(g2, w * 0.80, h * 0.74, maior * 0.17, new Color(0x24, 0x47, 0xD6, 44), maior * 0.026f);
        Marca.contorno(g2, w * 0.40, h * 0.16, maior * 0.09, new Color(255, 255, 255, 190), maior * 0.014f);
        Marca.contorno(g2, w * 0.05, h * 0.42, maior * 0.07, new Color(0x15, 0x1A, 0x26, 22), maior * 0.012f);
        Marca.contorno(g2, w * 0.99, h * 0.30, maior * 0.06, new Color(0x24, 0x47, 0xD6, 40), maior * 0.010f);
        g2.dispose();
        return img;
    }

    private static void mancha(Graphics2D g2, float cx, float cy, float raio, Color cor, int w, int h) {
        Color transparente = new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 0);
        g2.setPaint(new RadialGradientPaint(cx, cy, raio, new float[]{0f, 1f}, new Color[]{cor, transparente}));
        g2.fillRect(0, 0, w, h);
    }
}
