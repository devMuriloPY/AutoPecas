package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Caixas de mensagem do sistema: uma placa de vidro sobre a tela desfocada. */
public final class Dialogo {

    private Dialogo() {
    }

    /**
     * Pergunta antes de uma ação que não tem volta.
     * @param textoConfirmar o que o botão de confirmação diz (ex.: "Excluir")
     * @return true só se o usuário confirmou; apertar Esc conta como "não"
     */
    public static boolean confirma(Component pai, String titulo, String mensagem, String textoConfirmar) {
        boolean[] sim = {false};
        Sobreposto s = new Sobreposto(pai);
        Botao voltar = new Botao("Voltar", Botao.Tipo.SECUNDARIO).atalho("ESC");
        Botao confirmar = new Botao(textoConfirmar, Botao.Tipo.PERIGO);
        voltar.addActionListener(e -> s.fecha());
        confirmar.addActionListener(e -> {
            sim[0] = true;
            s.fecha();
        });
        s.raiz().setDefaultButton(voltar); // Enter nunca apaga nada por engano
        s.mostra(cartao(titulo, mensagem, Tema.ALERTA, voltar, confirmar), voltar);
        return sim[0];
    }

    /** Avisa de uma falha que precisa da atenção do usuário. */
    public static void erro(Component pai, String titulo, String mensagem) {
        Sobreposto s = new Sobreposto(pai);
        Botao ok = new Botao("Entendi", Botao.Tipo.PRIMARIO);
        ok.addActionListener(e -> s.fecha());
        s.raiz().setDefaultButton(ok);
        s.mostra(cartao(titulo, mensagem, Tema.ALERTA, ok), ok);
    }

    private static Cartao cartao(String titulo, String mensagem, Color corTitulo, Botao... botoes) {
        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        JLabel t = Tema.titulo(titulo, 24f, corTitulo);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        texto.add(t);
        texto.add(Box.createVerticalStrut(12));
        for (String linha : mensagem.split("\n")) {
            JLabel l = new JLabel(linha.isEmpty() ? " " : linha);
            l.setFont(Tema.texto(Font.PLAIN, 14.5f));
            l.setForeground(Tema.TINTA);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            texto.add(l);
            texto.add(Box.createVerticalStrut(3));
        }
        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        linhaBotoes.setOpaque(false);
        for (Botao b : botoes) {
            linhaBotoes.add(b);
        }
        Cartao c = new Cartao(new BorderLayout(0, 26), Vidro.Tom.CLARO, 30);
        c.setBorder(new EmptyBorder(30, 34, 24, 30));
        c.add(texto, BorderLayout.CENTER);
        c.add(linhaBotoes, BorderLayout.SOUTH);
        Dimension d = c.getPreferredSize();
        c.setPreferredSize(new Dimension(Math.max(d.width, 440), d.height));
        return c;
    }
}
