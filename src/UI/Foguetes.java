package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Comemoração de venda fechada: foguetes sobem do pé da janela e estouram em faíscas.
 *
 * A animação é desenhada por cima de tudo, mas não segura cliques nem teclas:
 * o operador pode começar a próxima venda enquanto ela ainda está passando.
 */
public class Foguetes extends JComponent {

    // sem branco: sobre o fundo claro das telas ele some
    private static final Color[] CORES = {Tema.AZUL, Tema.AZUL_CLARO, Tema.AZUL_ESCURO, new Color(0xF5A623)};
    private static final double ESCALA = 1.9; // tamanho do desenho do foguete
    private static final double GRAVIDADE = 300; // pixels por segundo ao quadrado

    private static final class Foguete {

        double x, y, alvoY, velocidade, atraso;
        Color cor;
    }

    private static final class Faisca {

        double x, y, vx, vy, vida, tamanho;
        Color cor;
    }

    private final List<Foguete> foguetes = new ArrayList<>();
    private final List<Faisca> faiscas = new ArrayList<>();
    private final Random sorte;
    private final int largura, altura;
    private Timer relogio;
    private Component vidroAnterior;

    /** Solta os foguetes na janela onde o componente está. Não faz nada se ele ainda não está numa janela. */
    public static void solta(Component naJanela) {
        JRootPane raiz = SwingUtilities.getRootPane(naJanela);
        if (raiz == null || raiz.getWidth() <= 0) {
            return;
        }
        if (raiz.getGlassPane() instanceof Foguetes emCurso) {
            emCurso.carrega(5); // outra venda fechou antes de a festa acabar: só soma mais foguetes
            return;
        }
        Foguetes f = new Foguetes(raiz.getWidth(), raiz.getHeight(), System.nanoTime());
        f.vidroAnterior = raiz.getGlassPane();
        raiz.setGlassPane(f);
        f.setVisible(true);
        long[] antes = {System.nanoTime()};
        f.relogio = new Timer(16, e -> {
            long agora = System.nanoTime();
            boolean continua = f.avanca(Math.min(0.05, (agora - antes[0]) / 1e9));
            antes[0] = agora;
            f.repaint();
            if (!continua) {
                f.relogio.stop();
                f.setVisible(false);
                raiz.setGlassPane(f.vidroAnterior);
            }
        });
        f.relogio.start();
    }

    /** @param semente fixa a sequência de sorteios (os testes usam para repetir a mesma cena) */
    public Foguetes(int largura, int altura, long semente) {
        this.largura = largura;
        this.altura = altura;
        this.sorte = new Random(semente);
        setOpaque(false);
        carrega(9);
    }

    private void carrega(int quantos) {
        for (int i = 0; i < quantos; i++) {
            Foguete f = new Foguete();
            f.x = largura * (0.10 + 0.80 * sorte.nextDouble());
            f.y = altura + 30;
            f.alvoY = altura * (0.16 + 0.30 * sorte.nextDouble());
            f.velocidade = altura * (1.05 + 0.35 * sorte.nextDouble());
            f.atraso = i * 0.14 + 0.10 * sorte.nextDouble();
            f.cor = CORES[sorte.nextInt(CORES.length)];
            foguetes.add(f);
        }
    }

    /**
     * Anda a animação um passo.
     * @return false quando não sobrou nada na tela
     */
    public boolean avanca(double segundos) {
        for (Iterator<Foguete> it = foguetes.iterator(); it.hasNext();) {
            Foguete f = it.next();
            if (f.atraso > 0) {
                f.atraso -= segundos;
                continue;
            }
            f.y -= f.velocidade * segundos;
            if (f.y <= f.alvoY) {
                estoura(f);
                it.remove();
            }
        }
        for (Iterator<Faisca> it = faiscas.iterator(); it.hasNext();) {
            Faisca s = it.next();
            s.vy += GRAVIDADE * segundos;
            s.vx *= 1 - 1.4 * segundos; // o ar freia
            s.vy *= 1 - 1.4 * segundos;
            s.x += s.vx * segundos;
            s.y += s.vy * segundos;
            s.vida -= 0.70 * segundos;
            if (s.vida <= 0) {
                it.remove();
            }
        }
        return !foguetes.isEmpty() || !faiscas.isEmpty();
    }

    private void estoura(Foguete f) {
        for (int i = 0; i < 70; i++) {
            Faisca s = new Faisca();
            double angulo = sorte.nextDouble() * Math.PI * 2;
            double forca = 120 + 380 * sorte.nextDouble();
            s.x = f.x;
            s.y = f.y;
            s.vx = Math.cos(angulo) * forca;
            s.vy = Math.sin(angulo) * forca;
            s.vida = 0.75 + 0.25 * sorte.nextDouble();
            s.tamanho = 4 + 5 * sorte.nextDouble();
            s.cor = sorte.nextInt(4) == 0 ? CORES[sorte.nextInt(CORES.length)] : f.cor;
            faiscas.add(s);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.g2(g);
        for (Faisca s : faiscas) {
            int alfa = (int) (255 * Math.max(0, Math.min(1, s.vida)));
            Color c = new Color(s.cor.getRed(), s.cor.getGreen(), s.cor.getBlue(), alfa);
            // risco curto na direção contrária ao movimento, para dar sensação de velocidade
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alfa / 3));
            g2.setStroke(new BasicStroke((float) s.tamanho * 0.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(s.x, s.y, s.x - s.vx * 0.05, s.y - s.vy * 0.05));
            g2.setColor(c);
            g2.fill(new java.awt.geom.Ellipse2D.Double(s.x - s.tamanho / 2, s.y - s.tamanho / 2, s.tamanho, s.tamanho));
        }
        for (Foguete f : foguetes) {
            if (f.atraso > 0) {
                continue;
            }
            Graphics2D gf = (Graphics2D) g2.create();
            gf.translate(f.x, f.y);
            gf.scale(ESCALA, ESCALA);
            // rastro na cor do foguete, sumindo para baixo
            gf.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gf.setPaint(new GradientPaint(0, 24, new Color(f.cor.getRed(), f.cor.getGreen(), f.cor.getBlue(), 150),
                    0, 130, new Color(f.cor.getRed(), f.cor.getGreen(), f.cor.getBlue(), 0)));
            gf.draw(new Line2D.Double(0, 24, 0, 130));
            // chama, que treme a cada quadro
            Path2D chama = new Path2D.Double();
            chama.moveTo(-5, 20);
            chama.quadTo(0, 42 + 10 * sorte.nextDouble(), 5, 20);
            chama.closePath();
            gf.setColor(new Color(0xF5A623));
            gf.fill(chama);
            // aletas, corpo, ponta e janela
            gf.setColor(f.cor);
            Path2D aletas = new Path2D.Double();
            aletas.moveTo(-6, 10);
            aletas.lineTo(-12, 23);
            aletas.lineTo(12, 23);
            aletas.lineTo(6, 10);
            aletas.closePath();
            gf.fill(aletas);
            gf.setColor(Color.WHITE);
            gf.fill(new RoundRectangle2D.Double(-6, -6, 12, 28, 6, 6));
            gf.setColor(new Color(0x15, 0x1A, 0x26, 60));
            gf.setStroke(new BasicStroke(0.7f));
            gf.draw(new RoundRectangle2D.Double(-6, -6, 12, 28, 6, 6));
            gf.setColor(f.cor);
            Path2D ponta = new Path2D.Double();
            ponta.moveTo(-6, -2);
            ponta.quadTo(0, -22, 6, -2);
            ponta.closePath();
            gf.fill(ponta);
            gf.setColor(Tema.AZUL_ESCURO);
            gf.fill(new java.awt.geom.Ellipse2D.Double(-3, 4, 6, 6));
            gf.dispose();
        }
        g2.dispose();
    }
}
