package View;

import Model.Usuario;
import UI.Botao;
import UI.Cartao;
import UI.FundoVidro;
import UI.Marca;
import UI.Tema;
import UI.Vidro;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Janela principal: menu lateral de vidro à esquerda e a tela escolhida à direita. */
public class Principal extends JFrame {

    private final Usuario usuario;
    private final JPanel conteudo = new JPanel(new BorderLayout());
    private final List<ItemMenu> itens = new ArrayList<>();

    public Principal(Usuario usuario) {
        super("AutoPeças");
        this.usuario = usuario;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 700));

        conteudo.setOpaque(false);
        FundoVidro raiz = new FundoVidro(new BorderLayout(4, 0));
        raiz.setBorder(new EmptyBorder(14, 14, 14, 14)); // o menu "flutua", sem encostar nas bordas
        raiz.add(lateral(), BorderLayout.WEST);
        raiz.add(conteudo, BorderLayout.CENTER);
        setContentPane(raiz);

        setSize(1280, 760);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        abre(itens.get(0));
    }

    private JComponent lateral() {
        // quem entra como "vendedor" só vende e consulta produtos
        boolean vendedor = "vendedor".equalsIgnoreCase(usuario.getTipo());

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.rotulo("Menu");
        titulo.setBorder(new EmptyBorder(0, 26, 10, 0));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(titulo);
        adiciona(menu, "Venda (PDV)", () -> new PdvView(usuario));
        adiciona(menu, "Produtos", ProdutoView::new);
        if (!vendedor) {
            adiciona(menu, "Clientes", ClienteView::new);
            adiciona(menu, "Vendedores", VendedorView::new);
        }

        JLabel nome = new JLabel(usuario.getLogin() == null ? "" : usuario.getLogin());
        nome.setFont(Tema.texto(Font.BOLD, 15f));
        nome.setForeground(Tema.TINTA);
        JLabel tipo = Tema.rotulo(usuario.getTipo() == null ? "usuário" : usuario.getTipo(), Tema.AZUL);
        Botao sair = new Botao("Sair", Botao.Tipo.SECUNDARIO);
        sair.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });
        JPanel conta = new JPanel();
        conta.setOpaque(false);
        conta.setLayout(new BoxLayout(conta, BoxLayout.Y_AXIS));
        conta.setBorder(new EmptyBorder(0, 26, 0, 26));
        for (JComponent c : new JComponent[]{tipo, nome}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            conta.add(c);
            conta.add(Box.createVerticalStrut(3));
        }
        conta.add(Box.createVerticalStrut(12));
        sair.setAlignmentX(Component.LEFT_ALIGNMENT);
        sair.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        conta.add(sair);

        Cartao p = new Cartao(new BorderLayout(0, 34), Vidro.Tom.CLARO, 30);
        p.setBorder(new EmptyBorder(26, 0, 24, 0));
        p.setPreferredSize(new Dimension(232, 0));
        p.add(new Logo(), BorderLayout.NORTH);
        p.add(menu, BorderLayout.CENTER);
        p.add(conta, BorderLayout.SOUTH);
        return p;
    }

    private void adiciona(JPanel menu, String texto, Supplier<JComponent> tela) {
        ItemMenu item = new ItemMenu(texto, tela);
        item.addActionListener(e -> abre(item));
        itens.add(item);
        menu.add(item);
    }

    private void abre(ItemMenu item) {
        for (ItemMenu i : itens) {
            i.ativo = i == item;
            i.repaint();
        }
        JComponent tela = item.tela.get(); // tela nova a cada clique: sempre começa limpa
        conteudo.removeAll();
        conteudo.add(tela, BorderLayout.CENTER);
        conteudo.revalidate();
        conteudo.repaint();
        if (tela instanceof PdvView pdv) {
            SwingUtilities.invokeLater(pdv::focaLeitor);
        }
    }

    /** Nome da casa no topo do menu. */
    private static final class Logo extends JComponent {

        Logo() {
            setPreferredSize(new Dimension(232, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            Marca.porca(g2, 42, 22, 16, Tema.AZUL);
            g2.setFont(Tema.display(Font.BOLD, 21f));
            g2.setColor(Tema.TINTA);
            g2.drawString("AUTO", 70, 20);
            g2.setColor(Tema.AZUL);
            g2.drawString("PEÇAS", 70, 40);
            g2.dispose();
        }
    }

    /** Linha do menu lateral. A tela aberta ganha uma cápsula de vidro claro, texto e ponto em azul. */
    private static final class ItemMenu extends JButton {

        private final Supplier<JComponent> tela;
        private boolean ativo;

        ItemMenu(String texto, Supplier<JComponent> tela) {
            super(texto);
            this.tela = tela;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(232, 48));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            int w = getWidth(), h = getHeight(), x = 12, alt = h - 6;
            if (ativo) {
                g2.setColor(new Color(255, 255, 255, 170));
                g2.fillRoundRect(x, 3, w - 2 * x, alt, alt, alt);
                g2.setColor(new Color(255, 255, 255, 250));
                g2.drawRoundRect(x, 3, w - 2 * x - 1, alt - 1, alt, alt);
                g2.setColor(Tema.AZUL);
                g2.fillOval(x + 16, h / 2 - 3, 7, 7);
            } else if (getModel().isRollover() || isFocusOwner()) {
                g2.setColor(new Color(255, 255, 255, 90));
                g2.fillRoundRect(x, 3, w - 2 * x, alt, alt, alt);
            }
            g2.setFont(Tema.texto(ativo ? Font.BOLD : Font.PLAIN, 15f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(ativo ? Tema.AZUL_ESCURO : Tema.TINTA);
            g2.drawString(getText(), x + 36, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
