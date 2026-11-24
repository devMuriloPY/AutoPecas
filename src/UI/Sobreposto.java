package UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Diálogo que abre "por cima" da janela: a tela de trás aparece desfocada e escurecida,
 * e o conteúdo fica numa placa de vidro no centro. Enquanto ele está aberto, o resto fica bloqueado.
 *
 * Uso: crie, monte o cartão (os botões podem chamar fecha()) e chame mostra(cartao).
 */
public final class Sobreposto {

    private final JDialog dialogo;
    private final Window dona;

    public Sobreposto(Component pai) {
        dona = pai == null ? null : pai instanceof Window w ? w : SwingUtilities.getWindowAncestor(pai);
        dialogo = new JDialog(dona, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public void fecha() {
        dialogo.dispose();
    }

    public JRootPane raiz() {
        return dialogo.getRootPane();
    }

    /** Abre e só volta quando o diálogo fecha (Esc também fecha). */
    public void mostra(JComponent cartao) {
        mostra(cartao, null);
    }

    /** @param foco componente que recebe o foco ao abrir (null = o padrão do Swing) */
    public void mostra(JComponent cartao, JComponent foco) {
        Rectangle area;
        BufferedImage atras;
        if (dona instanceof RootPaneContainer rpc && dona.isShowing() && rpc.getRootPane().getWidth() > 0) {
            JRootPane rp = rpc.getRootPane();
            area = new Rectangle(rp.getLocationOnScreen(), rp.getSize());
            atras = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = atras.createGraphics();
            rp.printAll(g); // "fotografa" a tela de trás
            g.dispose();
        } else {
            // sem janela por trás (ex.: teste): usa o fundo padrão num tamanho que acomode o cartão
            Dimension d = cartao.getPreferredSize();
            Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
            int w = Math.min(tela.width, d.width + 160), h = Math.min(tela.height, d.height + 140);
            area = new Rectangle((tela.width - w) / 2, (tela.height - h) / 2, w, h);
            atras = FundoVidro.desenha(w, h);
        }

        Veu veu = new Veu(Vidro.desfoca(atras));
        veu.add(cartao);
        dialogo.setContentPane(veu);
        dialogo.getRootPane().registerKeyboardAction(e -> fecha(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.setBounds(area);
        if (foco != null) {
            SwingUtilities.invokeLater(foco::requestFocusInWindow);
        }
        dialogo.setVisible(true);
    }

    /** O fundo do diálogo: a tela de trás desfocada, com um véu verde escuro por cima. */
    private static final class Veu extends JPanel implements Vidro.Fundo {

        private final BufferedImage desfocado;

        Veu(BufferedImage desfocado) {
            super(new GridBagLayout()); // centraliza o cartão
            this.desfocado = desfocado;
        }

        @Override
        public BufferedImage desfocado() {
            return desfocado;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            g2.drawImage(desfocado, 0, 0, null);
            g2.setColor(new Color(0x0E, 0x14, 0x28, 96));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (getComponentCount() > 0) {
                Rectangle r = getComponent(0).getBounds();
                Vidro.sombra(g2, r.x, r.y, r.width, r.height, 30);
            }
            g2.dispose();
        }
    }
}
