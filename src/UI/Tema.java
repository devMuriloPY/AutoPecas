package UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Identidade visual do sistema: vidro transparente sobre fundo claro, com detalhes em azul royal.
 * Todas as cores e fontes das telas saem daqui, para o visual ficar igual em todo lugar.
 */
public final class Tema {

    // Azul royal: usado só nos detalhes (ação principal, item ativo, foco, rótulos de destaque)
    public static final Color AZUL_ESCURO = new Color(0x17309C);
    public static final Color AZUL = new Color(0x2447D6);
    public static final Color AZUL_CLARO = new Color(0x6C8CFF);
    public static final Color AZUL_NEVOA = new Color(0xDCE4FF);

    // Neutros frios (do fundo da janela ao branco dos campos)
    public static final Color GELO = new Color(0xECEFF5);
    public static final Color NEVE = new Color(0xF6F8FC);
    public static final Color PAPEL = new Color(0xFFFFFF);
    public static final Color BORDA = new Color(0xCBD2DE);
    public static final Color BORDA_ESCURA = new Color(0x8E98AB);

    // Texto e avisos
    public static final Color TINTA = new Color(0x151A26);
    public static final Color TINTA_SUAVE = new Color(0x5B6475);
    public static final Color ALERTA = new Color(0xC0392B);

    private static final Set<String> FONTES = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
    private static final String FAMILIA_DISPLAY = escolhe("Bahnschrift", "Segoe UI Semibold", "Segoe UI", Font.SANS_SERIF);
    private static final String FAMILIA_TEXTO = escolhe("Segoe UI", "Bahnschrift", Font.SANS_SERIF);

    private Tema() {
    }

    private static String escolhe(String... familias) {
        for (String f : familias) {
            if (FONTES.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Fonte de títulos e números grandes. */
    public static Font display(int estilo, float tamanho) {
        return new Font(FAMILIA_DISPLAY, estilo, 12).deriveFont(tamanho);
    }

    /** Fonte de leitura (campos, textos, tabelas). */
    public static Font texto(int estilo, float tamanho) {
        return new Font(FAMILIA_TEXTO, estilo, 12).deriveFont(tamanho);
    }

    /** Letras maiúsculas espaçadas, usadas em rótulos e botões. */
    public static Font versalete(float tamanho) {
        Map<TextAttribute, Object> atributos = new HashMap<>();
        atributos.put(TextAttribute.TRACKING, 0.12f);
        return texto(Font.BOLD, tamanho).deriveFont(atributos);
    }

    /** Rótulo pequeno em maiúsculas (fica acima dos campos). */
    public static JLabel rotulo(String texto) {
        return rotulo(texto, TINTA_SUAVE);
    }

    public static JLabel rotulo(String texto, Color cor) {
        JLabel l = new JLabel(texto.toUpperCase());
        l.setFont(versalete(10.5f));
        l.setForeground(cor);
        return l;
    }

    public static JLabel titulo(String texto, float tamanho, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(display(Font.BOLD, tamanho));
        l.setForeground(cor);
        suave(l);
        return l;
    }

    /** Texto grande fica melhor com suavização em tons de cinza (sem a franja colorida do modo LCD). */
    public static void suave(javax.swing.JComponent c) {
        c.putClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Graphics2D com suavização ligada. Quem chama deve dar dispose(). */
    public static Graphics2D g2(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2;
    }

    /**
     * Deve ser chamado uma vez, antes de abrir a primeira janela.
     * Recolore o que vem pronto do Swing (caixas de mensagem, seletor de arquivo, barras de rolagem).
     */
    public static void instalar() {
        try {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            MetalLookAndFeel.setCurrentTheme(new TemaMetal());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            // segue com a aparência padrão; as telas próprias continuam com as cores do tema
        }
        Font f = texto(Font.PLAIN, 13.5f);
        UIManager.put("OptionPane.background", NEVE);
        UIManager.put("Panel.background", NEVE);
        UIManager.put("OptionPane.messageForeground", TINTA);
        UIManager.put("OptionPane.messageFont", f);
        UIManager.put("OptionPane.buttonFont", texto(Font.BOLD, 13f));
        UIManager.put("Button.background", PAPEL);
        UIManager.put("Button.foreground", AZUL_ESCURO);
        UIManager.put("Button.select", AZUL_NEVOA);
        UIManager.put("Button.focus", AZUL_CLARO);
        UIManager.put("TextField.background", PAPEL);
        UIManager.put("TextField.selectionBackground", AZUL_NEVOA);
        UIManager.put("TextField.selectionForeground", TINTA);
        UIManager.put("PasswordField.selectionBackground", AZUL_NEVOA);
        UIManager.put("ComboBox.background", PAPEL);
        UIManager.put("ComboBox.foreground", TINTA);
        UIManager.put("ComboBox.disabledForeground", TINTA_SUAVE);
        UIManager.put("ComboBox.disabledBackground", NEVE);
        UIManager.put("ComboBox.selectionBackground", AZUL_NEVOA);
        UIManager.put("ComboBox.selectionForeground", TINTA);
        UIManager.put("List.selectionBackground", AZUL_NEVOA);
        UIManager.put("List.selectionForeground", TINTA);
        UIManager.put("ToolTip.background", TINTA);
        UIManager.put("ToolTip.foreground", PAPEL);
        UIManager.put("ToolTip.font", texto(Font.PLAIN, 12.5f));
        // janela de escolher arquivo (foto do produto) em português
        UIManager.put("FileChooser.openButtonText", "Abrir");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
        UIManager.put("FileChooser.lookInLabelText", "Procurar em:");
        UIManager.put("FileChooser.fileNameLabelText", "Nome do arquivo:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Tipo:");
    }

    /** Paleta do tema aplicada ao visual "Metal" do Java. */
    private static final class TemaMetal extends DefaultMetalTheme {

        private final FontUIResource fonte = new FontUIResource(texto(Font.PLAIN, 13.5f));
        private final FontUIResource fonteTitulo = new FontUIResource(texto(Font.BOLD, 13.5f));

        @Override
        public String getName() {
            return "Vidro e Azul Royal";
        }

        @Override
        protected ColorUIResource getPrimary1() {
            return new ColorUIResource(AZUL_ESCURO);
        }

        @Override
        protected ColorUIResource getPrimary2() {
            return new ColorUIResource(AZUL_CLARO);
        }

        @Override
        protected ColorUIResource getPrimary3() {
            return new ColorUIResource(AZUL_NEVOA);
        }

        @Override
        protected ColorUIResource getSecondary1() {
            return new ColorUIResource(BORDA_ESCURA);
        }

        @Override
        protected ColorUIResource getSecondary2() {
            return new ColorUIResource(BORDA);
        }

        @Override
        protected ColorUIResource getSecondary3() {
            return new ColorUIResource(NEVE);
        }

        @Override
        public FontUIResource getControlTextFont() {
            return fonte;
        }

        @Override
        public FontUIResource getSystemTextFont() {
            return fonte;
        }

        @Override
        public FontUIResource getUserTextFont() {
            return fonte;
        }

        @Override
        public FontUIResource getMenuTextFont() {
            return fonte;
        }

        @Override
        public FontUIResource getSubTextFont() {
            return fonte;
        }

        @Override
        public FontUIResource getWindowTitleFont() {
            return fonteTitulo;
        }
    }
}
