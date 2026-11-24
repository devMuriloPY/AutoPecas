package View;

import DAO.UsuarioDAO;
import Model.Usuario;
import UI.Botao;
import UI.Campo;
import UI.CampoSenha;
import UI.Cartao;
import UI.FundoVidro;
import UI.Marca;
import UI.Tema;
import UI.Vidro;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Tela de entrada do sistema. */
public class LoginView extends JFrame {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Campo txtUsuario = new Campo();
    private final CampoSenha txtSenha = new CampoSenha();
    private final JLabel lblErro = new JLabel(" ");
    private final Botao btnEntrar = new Botao("Entrar", Botao.Tipo.PRIMARIO);

    public LoginView() {
        super("AutoPeças");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        FundoVidro raiz = new FundoVidro(new BorderLayout());
        raiz.setPreferredSize(new Dimension(940, 580));
        raiz.add(new Vitrine(), BorderLayout.WEST);
        raiz.add(formulario(), BorderLayout.CENTER);
        setContentPane(raiz);

        btnEntrar.addActionListener(e -> entrar());
        getRootPane().setDefaultButton(btnEntrar); // Enter em qualquer campo faz o login

        pack();
        setLocationRelativeTo(null);
    }

    private JComponent formulario() {
        JLabel sub = new JLabel("Use o seu usuário e a sua senha do sistema.");
        sub.setFont(Tema.texto(Font.PLAIN, 14f));
        sub.setForeground(Tema.TINTA_SUAVE);
        lblErro.setFont(Tema.texto(Font.PLAIN, 13.5f));
        lblErro.setForeground(Tema.ALERTA);
        btnEntrar.setPreferredSize(new Dimension(100, 46));

        JPanel coluna = new JPanel();
        coluna.setOpaque(false);
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        Object[] itens = {
            Tema.rotulo("Acesso", Tema.AZUL), 4,
            Tema.titulo("Entrar", 38f, Tema.TINTA), 6,
            sub, 30,
            Tema.rotulo("Usuário"), 6, txtUsuario, 18,
            Tema.rotulo("Senha"), 6, txtSenha, 12,
            lblErro, 14,
            btnEntrar
        };
        for (Object item : itens) {
            if (item instanceof Integer espaco) {
                coluna.add(Box.createVerticalStrut(espaco));
            } else {
                JComponent c = (JComponent) item;
                c.setAlignmentX(Component.LEFT_ALIGNMENT);
                c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
                coluna.add(c);
            }
        }

        Cartao cartao = new Cartao(new BorderLayout(), Vidro.Tom.CLARO, 34);
        cartao.setBorder(new EmptyBorder(38, 40, 40, 40));
        cartao.add(coluna);
        cartao.setPreferredSize(new Dimension(400, cartao.getPreferredSize().height));

        JPanel p = new JPanel(new GridBagLayout()); // centraliza o cartão na metade direita
        p.setOpaque(false);
        p.add(cartao);
        return p;
    }

    private void entrar() {
        String login = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        if (login.isEmpty()) {
            erro("Informe o usuário");
            txtUsuario.requestFocusInWindow();
            return;
        }
        if (senha.isEmpty()) {
            erro("Informe a senha");
            txtSenha.requestFocusInWindow();
            return;
        }
        try {
            Usuario usuario = usuarioDAO.buscaLogin(login, senha);
            if (usuario == null) {
                erro("Usuário ou senha incorretos");
                txtSenha.setText("");
                txtSenha.requestFocusInWindow();
                return;
            }
            new Principal(usuario).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            Logger.getLogger(LoginView.class.getName()).log(Level.SEVERE, null, ex);
            erro(ex.getCause() instanceof java.io.IOException
                    ? "Falta o arquivo config.properties. Veja o README."
                    : "Sem conexão com o banco. Verifique se o MySQL está ligado.");
        }
    }

    private void erro(String mensagem) {
        lblErro.setText(mensagem);
    }

    /** Lado esquerdo: o nome da casa escrito direto sobre o fundo. */
    private static final class Vitrine extends JComponent {

        Vitrine() {
            setPreferredSize(new Dimension(440, 580));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            int h = getHeight();
            Marca.porca(g2, 70, 70, 20, Tema.AZUL);

            int x = 50, y = h - 160;
            g2.setFont(Tema.display(Font.BOLD, 66f));
            g2.setColor(Tema.TINTA);
            g2.drawString("AUTO", x, y);
            g2.setColor(Tema.AZUL);
            g2.drawString("PEÇAS", x, y + 62);
            g2.setColor(Tema.AZUL);
            g2.fillRoundRect(x, y + 86, 56, 5, 5, 5);
            g2.setFont(Tema.versalete(11f));
            g2.setColor(Tema.TINTA_SUAVE);
            g2.drawString("BALCÃO  ·  ESTOQUE  ·  VENDAS", x, y + 116);
            g2.dispose();
        }
    }
}
