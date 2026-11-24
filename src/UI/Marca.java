package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Símbolo da casa: uma porca sextavada, desenhada por código (sem arquivo de imagem). */
public final class Marca {

    private Marca() {
    }

    /** Porca cheia, com o furo no meio. */
    public static void porca(Graphics2D g2, double cx, double cy, double raio, Color cor) {
        Area forma = new Area(sextavado(cx, cy, raio));
        double furo = raio * 0.46;
        forma.subtract(new Area(new Ellipse2D.Double(cx - furo, cy - furo, furo * 2, furo * 2)));
        g2.setColor(cor);
        g2.fill(forma);
    }

    /** Só o contorno da porca: usado grande, como desenho de fundo. */
    public static void contorno(Graphics2D g2, double cx, double cy, double raio, Color cor, float espessura) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(espessura, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(sextavado(cx, cy, raio));
        double furo = raio * 0.5;
        g2.draw(new Ellipse2D.Double(cx - furo, cy - furo, furo * 2, furo * 2));
    }

    private static Path2D sextavado(double cx, double cy, double raio) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angulo = Math.toRadians(60 * i + 30);
            double x = cx + raio * Math.cos(angulo), y = cy + raio * Math.sin(angulo);
            if (i == 0) {
                p.moveTo(x, y);
            } else {
                p.lineTo(x, y);
            }
        }
        p.closePath();
        return p;
    }
}
