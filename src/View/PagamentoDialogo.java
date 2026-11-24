package View;

import Service.Texto;
import UI.Botao;
import UI.Campo;
import UI.Cartao;
import UI.Sobreposto;
import UI.Tema;
import UI.Vidro;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Tela de pagamento do PDV. Abre por cima da venda quando o operador finaliza (F2):
 * ele escolhe a forma de pagamento, informa o valor recebido se for dinheiro e confirma.
 *
 * Teclas: F5 a F8 escolhem a forma, Enter confirma, Esc volta para a venda.
 */
public final class PagamentoDialogo {

    public static final String DINHEIRO = "Dinheiro";

    /** O que o operador decidiu. recebido e troco só existem em dinheiro, quando o valor foi informado. */
    public record Pagamento(String forma, BigDecimal recebido, BigDecimal troco) {
    }

    private record Forma(String nome, String tecla, String instrucao) {
    }

    private static final Forma[] FORMAS = {
        new Forma(DINHEIRO, "F5", null),
        new Forma("Pix", "F6", "Mostre o QR Code ou a chave Pix ao cliente e confirme depois de receber."),
        new Forma("Cartão de débito", "F7", "Passe o cartão na maquininha no débito e confirme quando aprovar."),
        new Forma("Cartão de crédito", "F8", "Passe o cartão na maquininha no crédito e confirme quando aprovar."),};

    private PagamentoDialogo() {
    }

    /** @return o pagamento confirmado, ou null se o operador voltou para a venda */
    public static Pagamento pede(Component pai, BigDecimal total, int quantidadeItens) {
        Pagamento[] resultado = {null};
        Sobreposto s = new Sobreposto(pai);

        Campo txtRecebido = new Campo().exemplo("valor exato").aDireita();
        txtRecebido.setFont(Tema.display(Font.PLAIN, 28f));
        txtRecebido.setPreferredSize(new Dimension(230, 60));
        JLabel lblTroco = Tema.titulo(Texto.moeda(BigDecimal.ZERO), 40f, Tema.AZUL_ESCURO);
        JLabel lblRotuloTroco = Tema.rotulo("Troco");
        JLabel lblInstrucao = new JLabel(" ");
        lblInstrucao.setFont(Tema.texto(Font.PLAIN, 15f));
        lblInstrucao.setForeground(Tema.TINTA);
        JLabel lblAviso = new JLabel(" ");
        lblAviso.setFont(Tema.texto(Font.PLAIN, 13.5f));
        lblAviso.setForeground(Tema.ALERTA);
        Botao voltar = new Botao("Voltar à venda", Botao.Tipo.SECUNDARIO).atalho("ESC");
        Botao confirmar = new Botao("Confirmar pagamento", Botao.Tipo.PRIMARIO).atalho("ENTER");
        confirmar.setPreferredSize(new Dimension(confirmar.getPreferredSize().width + 10, 48));
        voltar.setPreferredSize(new Dimension(voltar.getPreferredSize().width, 48));

        // ---- detalhe da forma escolhida: dinheiro (valor e troco) ou instrução para as outras
        JPanel recebido = new JPanel(new BorderLayout(0, 6));
        recebido.setOpaque(false);
        recebido.add(Tema.rotulo("Valor recebido (R$)"), BorderLayout.NORTH);
        recebido.add(txtRecebido, BorderLayout.CENTER);
        JPanel troco = new JPanel(new BorderLayout(0, 2));
        troco.setOpaque(false);
        troco.add(lblRotuloTroco, BorderLayout.NORTH);
        troco.add(lblTroco, BorderLayout.CENTER);
        JPanel dinheiro = new JPanel(new BorderLayout(34, 0));
        dinheiro.setOpaque(false);
        dinheiro.add(recebido, BorderLayout.WEST);
        dinheiro.add(troco, BorderLayout.CENTER);
        JPanel outras = new JPanel(new BorderLayout());
        outras.setOpaque(false);
        outras.add(lblInstrucao, BorderLayout.CENTER);
        CardLayout cartas = new CardLayout();
        JPanel detalhe = new JPanel(cartas);
        detalhe.setOpaque(false);
        detalhe.add(dinheiro, "dinheiro");
        detalhe.add(outras, "outras");

        // ---- as formas de pagamento
        List<Opcao> opcoes = new ArrayList<>();
        int[] escolhida = {0};
        Runnable atualizaTroco = () -> {
            BigDecimal valor = le(txtRecebido);
            lblAviso.setText(" ");
            if (valor == null) {
                lblRotuloTroco.setText("TROCO");
                lblTroco.setForeground(Tema.AZUL_ESCURO);
                lblTroco.setText(Texto.moeda(BigDecimal.ZERO));
                return;
            }
            BigDecimal diferenca = valor.subtract(total);
            boolean falta = diferenca.signum() < 0;
            lblRotuloTroco.setText(falta ? "FALTAM" : "TROCO");
            lblTroco.setForeground(falta ? Tema.ALERTA : Tema.AZUL_ESCURO);
            lblTroco.setText(Texto.moeda(diferenca.abs()));
        };
        java.util.function.IntConsumer escolhe = i -> {
            escolhida[0] = i;
            for (int k = 0; k < opcoes.size(); k++) {
                opcoes.get(k).marcada = k == i;
                opcoes.get(k).repaint();
            }
            boolean emDinheiro = FORMAS[i].nome().equals(DINHEIRO);
            cartas.show(detalhe, emDinheiro ? "dinheiro" : "outras");
            lblAviso.setText(" ");
            if (emDinheiro) {
                txtRecebido.requestFocusInWindow();
            } else {
                lblInstrucao.setText(FORMAS[i].instrucao());
                confirmar.requestFocusInWindow();
            }
        };
        JPanel grade = new JPanel(new GridLayout(1, FORMAS.length, 12, 0));
        grade.setOpaque(false);
        for (int i = 0; i < FORMAS.length; i++) {
            int indice = i;
            Opcao o = new Opcao(FORMAS[i], i);
            o.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    escolhe.accept(indice);
                }
            });
            opcoes.add(o);
            grade.add(o);
            s.raiz().registerKeyboardAction(e -> escolhe.accept(indice), KeyStroke.getKeyStroke(FORMAS[i].tecla()),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }

        txtRecebido.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizaTroco.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizaTroco.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizaTroco.run();
            }
        });
        voltar.addActionListener(e -> s.fecha());
        confirmar.addActionListener(e -> {
            Forma forma = FORMAS[escolhida[0]];
            BigDecimal valor = null, trocoFinal = null;
            if (forma.nome().equals(DINHEIRO) && !txtRecebido.getText().isBlank()) {
                valor = le(txtRecebido);
                if (valor == null) {
                    lblAviso.setText("Valor recebido inválido. Exemplo: 100,00");
                    txtRecebido.requestFocusInWindow();
                    return;
                }
                if (valor.compareTo(total) < 0) {
                    lblAviso.setText("O valor recebido é menor que o total. Faltam " + Texto.moeda(total.subtract(valor)) + ".");
                    txtRecebido.requestFocusInWindow();
                    return;
                }
                valor = valor.setScale(2, RoundingMode.HALF_UP);
                trocoFinal = valor.subtract(total).setScale(2, RoundingMode.HALF_UP);
            }
            resultado[0] = new Pagamento(forma.nome(), valor, trocoFinal);
            s.fecha();
        });
        s.raiz().setDefaultButton(confirmar); // Enter confirma, mesmo com o cursor no valor recebido

        // ---- montagem
        JPanel titulo = new JPanel();
        titulo.setOpaque(false);
        titulo.setLayout(new BoxLayout(titulo, BoxLayout.Y_AXIS));
        titulo.add(Tema.rotulo("Pagamento", Tema.AZUL));
        titulo.add(Box.createVerticalStrut(2));
        titulo.add(Tema.titulo("Como o cliente vai pagar?", 28f, Tema.TINTA));
        JLabel lblTotal = Tema.titulo(Texto.moeda(total), 46f, Tema.TINTA);
        lblTotal.setHorizontalAlignment(JLabel.RIGHT);
        JLabel lblItens = Tema.rotulo(quantidadeItens == 1 ? "Total  ·  1 item" : "Total  ·  " + quantidadeItens + " itens");
        lblItens.setHorizontalAlignment(JLabel.RIGHT);
        JPanel valorTotal = new JPanel(new BorderLayout(0, 0));
        valorTotal.setOpaque(false);
        valorTotal.add(lblItens, BorderLayout.NORTH);
        valorTotal.add(lblTotal, BorderLayout.CENTER);
        JPanel topo = new JPanel(new BorderLayout(30, 0));
        topo.setOpaque(false);
        topo.add(titulo, BorderLayout.WEST);
        topo.add(valorTotal, BorderLayout.EAST);

        detalhe.setPreferredSize(new Dimension(100, 86));
        JPanel meio = new JPanel(new BorderLayout(0, 24));
        meio.setOpaque(false);
        meio.add(grade, BorderLayout.NORTH);
        meio.add(detalhe, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(voltar);
        botoes.add(confirmar);
        JPanel rodape = new JPanel(new BorderLayout(12, 0));
        rodape.setOpaque(false);
        rodape.add(lblAviso, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);

        Cartao cartao = new Cartao(new BorderLayout(0, 26), Vidro.Tom.CLARO, 34);
        cartao.setBorder(new EmptyBorder(32, 36, 28, 36));
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(meio, BorderLayout.CENTER);
        cartao.add(rodape, BorderLayout.SOUTH);
        cartao.setPreferredSize(new Dimension(860, cartao.getPreferredSize().height));

        opcoes.get(0).marcada = true;
        s.mostra(cartao, txtRecebido);
        return resultado[0];
    }

    /** @return o valor digitado; null se vazio ou não for número */
    private static BigDecimal le(Campo campo) {
        try {
            return Texto.parseDecimal(campo.getText());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Uma forma de pagamento: bloco de vidro com desenho, nome e a tecla que a escolhe. */
    private static final class Opcao extends JComponent {

        private final Forma forma;
        private final int tipo;
        private boolean marcada;

        Opcao(Forma forma, int tipo) {
            this.forma = forma;
            this.tipo = tipo;
            setPreferredSize(new Dimension(180, 128));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D f = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 24, 24);
            if (marcada) {
                g2.setPaint(new GradientPaint(0, 0, new Color(0x3A5FE8), 0, h, new Color(0x1B39B4)));
                g2.fill(f);
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 64), 0, h * 0.5f, new Color(255, 255, 255, 0)));
                g2.fill(f);
                g2.setColor(new Color(255, 255, 255, 110));
            } else {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fill(f);
                g2.setColor(new Color(255, 255, 255, 235));
            }
            g2.setStroke(new BasicStroke(1.3f));
            g2.draw(f);

            Color tinta = marcada ? Tema.PAPEL : Tema.AZUL_ESCURO;
            desenho(g2, 20, 20, tinta);

            // tecla, no canto de cima
            g2.setFont(Tema.versalete(10.5f));
            FontMetrics ft = g2.getFontMetrics();
            int tw = ft.stringWidth(forma.tecla()) + 16, th = ft.getHeight() + 6;
            g2.setColor(new Color(tinta.getRed(), tinta.getGreen(), tinta.getBlue(), 40));
            g2.fillRoundRect(w - tw - 16, 18, tw, th, th, th);
            g2.setColor(tinta);
            g2.drawString(forma.tecla(), w - tw - 8, 18 + 3 + ft.getAscent());

            g2.setFont(Tema.texto(Font.BOLD, 16f));
            g2.setColor(marcada ? Tema.PAPEL : Tema.TINTA);
            g2.drawString(forma.nome(), 20, h - 22);
            g2.dispose();
        }

        /** Desenho simples de cada forma: cédula, losango do Pix ou cartão. */
        private void desenho(Graphics2D g2, int x, int y, Color cor) {
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case 0 -> {
                    g2.drawRoundRect(x, y + 4, 44, 26, 6, 6);
                    g2.drawOval(x + 15, y + 10, 14, 14);
                    g2.drawLine(x + 6, y + 17, x + 8, y + 17);
                    g2.drawLine(x + 36, y + 17, x + 38, y + 17);
                }
                case 1 -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(x + 18, y);
                    p.lineTo(x + 36, y + 18);
                    p.lineTo(x + 18, y + 36);
                    p.lineTo(x, y + 18);
                    p.closePath();
                    g2.draw(p);
                    g2.drawLine(x + 9, y + 9, x + 18, y + 18);
                    g2.drawLine(x + 27, y + 9, x + 18, y + 18);
                    g2.drawLine(x + 9, y + 27, x + 18, y + 18);
                    g2.drawLine(x + 27, y + 27, x + 18, y + 18);
                }
                default -> {
                    g2.drawRoundRect(x, y + 4, 44, 28, 7, 7);
                    g2.drawLine(x, y + 13, x + 44, y + 13);
                    if (tipo == 2) {
                        g2.drawLine(x + 7, y + 24, x + 18, y + 24);          // débito: um traço
                    } else {
                        g2.drawLine(x + 7, y + 24, x + 14, y + 24);          // crédito: parcelas
                        g2.drawLine(x + 19, y + 24, x + 26, y + 24);
                        g2.drawLine(x + 31, y + 24, x + 37, y + 24);
                    }
                }
            }
        }
    }
}
