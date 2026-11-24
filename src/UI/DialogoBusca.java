package UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Janela de pesquisa: o usuário digita parte do nome, a lista vai filtrando,
 * e ele escolhe um registro com Enter ou duplo clique.
 */
public final class DialogoBusca {

    /** De onde vêm os resultados (normalmente uma consulta ao banco). */
    public interface Fonte {

        List<ItemLista> busca(String termo) throws Exception;
    }

    private DialogoBusca() {
    }

    /**
     * @param termoInicial texto que já vem digitado no filtro (pode ser vazio: lista tudo)
     * @return o código do registro escolhido, ou null se o usuário fechou sem escolher
     */
    public static Integer escolhe(Component pai, String titulo, String dica, String termoInicial, Fonte fonte) {
        Sobreposto d = new Sobreposto(pai);
        Integer[] escolhido = {null};

        Campo filtro = new Campo().exemplo(dica);
        filtro.setText(termoInicial == null ? "" : termoInicial);
        DefaultListModel<ItemLista> modelo = new DefaultListModel<>();
        JList<ItemLista> lista = new JList<>(modelo);
        lista.setCellRenderer(new ItemLista.Desenho());
        lista.setFixedCellHeight(ItemLista.ALTURA);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Tema.PAPEL);
        lista.setFocusable(false); // o foco fica sempre no filtro: digitar e usar as setas ao mesmo tempo
        JLabel situacao = new JLabel(" ");
        situacao.setFont(Tema.texto(Font.PLAIN, 12.5f));
        situacao.setForeground(Tema.TINTA_SUAVE);
        Botao abrir = new Botao("Abrir", Botao.Tipo.PRIMARIO);
        Botao voltar = new Botao("Voltar", Botao.Tipo.SECUNDARIO);

        Runnable pesquisa = () -> {
            modelo.clear();
            try {
                for (ItemLista item : fonte.busca(filtro.getText().trim())) {
                    modelo.addElement(item);
                }
                int n = modelo.size();
                situacao.setForeground(Tema.TINTA_SUAVE);
                situacao.setText(n == 0 ? "Nada encontrado. Tente outra parte do nome."
                        : (n == 1 ? "1 resultado" : n + " resultados") + "  ·  setas escolhem, Enter abre");
                if (n > 0) {
                    lista.setSelectedIndex(0);
                }
            } catch (Exception ex) {
                situacao.setForeground(Tema.ALERTA);
                situacao.setText("Não foi possível pesquisar: " + ex.getMessage());
            }
            abrir.setEnabled(!modelo.isEmpty());
        };
        Runnable confirma = () -> {
            ItemLista item = lista.getSelectedValue();
            if (item != null) {
                escolhido[0] = item.id();
                d.fecha();
            }
        };

        // pesquisa pouco depois de parar de digitar, para não consultar o banco a cada tecla
        Timer espera = new Timer(220, e -> pesquisa.run());
        espera.setRepeats(false);
        filtro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                espera.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                espera.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                espera.restart();
            }
        });
        filtro.addActionListener(e -> confirma.run());
        abrir.addActionListener(e -> confirma.run());
        voltar.addActionListener(e -> d.fecha());
        tecla(filtro, "DOWN", () -> move(lista, 1));
        tecla(filtro, "UP", () -> move(lista, -1));
        tecla(filtro, "PAGE_DOWN", () -> move(lista, 6));
        tecla(filtro, "PAGE_UP", () -> move(lista, -6));
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirma.run();
                }
            }
        });

        JPanel topo = new JPanel(new BorderLayout(0, 12));
        topo.setOpaque(false);
        topo.add(Tema.titulo(titulo, 24f, Tema.TINTA), BorderLayout.NORTH);
        topo.add(filtro, BorderLayout.CENTER);

        lista.setOpaque(false); // a lista fica sobre o vidro do cartão
        JScrollPane rolagem = BarraFina.aplica(new JScrollPane(lista), new java.awt.Color(0x15, 0x1A, 0x26, 70));
        rolagem.setPreferredSize(new Dimension(580, ItemLista.ALTURA * 7 + 2));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(voltar);
        botoes.add(abrir);
        JPanel rodape = new JPanel(new BorderLayout(12, 0));
        rodape.setOpaque(false);
        rodape.add(situacao, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);

        Cartao raiz = new Cartao(new BorderLayout(0, 14), Vidro.Tom.CLARO, 30);
        raiz.setBorder(new EmptyBorder(28, 30, 22, 30));
        raiz.add(topo, BorderLayout.NORTH);
        raiz.add(rolagem, BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);

        pesquisa.run();
        d.mostra(raiz, filtro); // modal: só volta quando fecha (Esc também fecha)
        espera.stop();
        return escolhido[0];
    }

    /** Liga uma tecla do campo a uma ação (as setas não fazem nada sozinhas num campo de texto). */
    public static void tecla(JComponent campo, String tecla, Runnable acao) {
        campo.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(tecla), "busca-" + tecla);
        campo.getActionMap().put("busca-" + tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }

    /** Move a seleção da lista sem tirar o foco de quem está digitando. */
    public static void move(JList<?> lista, int passo) {
        int n = lista.getModel().getSize();
        if (n == 0) {
            return;
        }
        int i = Math.max(0, Math.min(n - 1, lista.getSelectedIndex() + passo));
        lista.setSelectedIndex(i);
        lista.ensureIndexIsVisible(i);
    }
}
