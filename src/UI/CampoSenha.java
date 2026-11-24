package UI;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPasswordField;

/** Campo de senha: mesmo visual do Campo, mas esconde o que é digitado. */
public class CampoSenha extends JPasswordField {

    private String exemplo;

    public CampoSenha() {
        Campo.prepara(this);
    }

    public CampoSenha exemplo(String texto) {
        this.exemplo = texto;
        repaint();
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 38);
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Campo.pintaFundo(g, this);
        super.paintComponent(g);
        Campo.pintaExemplo(g, this, exemplo);
    }
}
