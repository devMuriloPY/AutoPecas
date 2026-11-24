package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/** Quadro que mostra a foto do produto inteira, sem cortar nem distorcer. */
public class FotoBox extends JComponent {

    private static final int RAIO = 20;
    private BufferedImage imagem;
    private String textoVazio = "SEM FOTO";

    public void setImagem(BufferedImage imagem) {
        this.imagem = imagem;
        repaint();
    }

    public BufferedImage getImagem() {
        return imagem;
    }

    public void setTextoVazio(String texto) {
        this.textoVazio = texto;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.g2(g);
        int w = getWidth(), h = getHeight();
        RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, RAIO, RAIO);
        g2.setClip(forma);

        if (imagem != null) {
            g2.setColor(Color.WHITE);
            g2.fill(forma);
            double escala = Math.min((w - 20.0) / imagem.getWidth(), (h - 20.0) / imagem.getHeight());
            int iw = (int) Math.round(imagem.getWidth() * escala), ih = (int) Math.round(imagem.getHeight() * escala);
            g2.drawImage(imagem, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
        } else {
            // sem foto: vidro riscado, como uma etiqueta em branco
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fill(forma);
            g2.setColor(new Color(255, 255, 255, 82));
            g2.setStroke(new BasicStroke(6f));
            for (int x = -h; x < w; x += 24) {
                g2.drawLine(x, h, x + h, 0);
            }
            g2.setFont(Tema.versalete(11f));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(textoVazio);
            int tx = (w - tw) / 2, ty = h / 2 + fm.getAscent() / 2 - 1, ah = fm.getHeight() + 12;
            g2.setColor(new Color(255, 255, 255, 190));
            g2.fillRoundRect(tx - 14, ty - fm.getAscent() - 7, tw + 28, ah, ah, ah);
            g2.setColor(Tema.TINTA_SUAVE);
            g2.drawString(textoVazio, tx, ty);
        }

        g2.setClip(null);
        g2.setColor(new Color(255, 255, 255, 230));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(forma);
        g2.dispose();
    }
}
