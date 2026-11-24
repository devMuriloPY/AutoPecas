package View;

import DAO.ClienteDAO;
import DAO.ProdutoDAO;
import DAO.VendaDAO;
import DAO.VendedorDAO;
import Model.Cliente;
import Model.ItemVenda;
import Model.Produto;
import Model.Usuario;
import Model.Venda;
import Model.Vendedor;
import Service.Imagem;
import Service.Texto;
import UI.BarraFina;
import UI.Botao;
import UI.Campo;
import UI.Cartao;
import UI.FotoBox;
import UI.Seletor;
import UI.Tema;
import UI.Vidro;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import UI.Dialogo;
import UI.DialogoBusca;
import UI.Foguetes;
import UI.ItemLista;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Ponto de venda: bipa (ou digita) o produto, confere a foto e fecha a venda.
 * Atalhos: F2 finaliza, F4 cancela a venda, F6 volta ao leitor, Delete remove o item selecionado.
 */
public class PdvView extends JPanel {

    /** Linha das listas de cliente e vendedor. id null = "não informado". */
    private record Opcao(Integer id, String nome) {

        @Override
        public String toString() {
            return nome;
        }
    }

    private final Usuario usuario;
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final List<ItemVenda> itens = new ArrayList<>();
    private final ModeloItens modelo = new ModeloItens();

    private static final int MAX_SUGESTOES = 8;

    private final Campo txtLeitor = new Campo().exemplo("Código, código de barras ou nome");
    private final DefaultListModel<ItemLista> modeloSugestoes = new DefaultListModel<>();
    private final JList<ItemLista> lstSugestoes = new JList<>(modeloSugestoes);
    private final JPopupMenu popSugestoes = new JPopupMenu();
    /** Espera o operador parar de digitar antes de consultar o banco (o leitor de barras digita muito rápido). */
    private final Timer esperaDigitar = new Timer(200, e -> sugere());
    private final Botao btnAdicionar = new Botao("Adicionar", Botao.Tipo.SECUNDARIO).atalho("ENTER");
    private final JTable tabela = new TabelaItens();
    private final Botao btnRemover = new Botao("Remover item", Botao.Tipo.SECUNDARIO).atalho("DEL");
    private final JLabel lblStatus = new JLabel(" ");
    private final Timer limpaStatus = new Timer(9000, e -> lblStatus.setText(" "));

    private final FotoBox foto = new FotoBox();
    private final JLabel lblProduto = new JLabel("Aguardando o primeiro produto");
    private final JLabel lblPreco = new JLabel(" ");
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblResumo = new JLabel();
    private final Seletor<Opcao> cbCliente = new Seletor<>();
    private final Seletor<Opcao> cbVendedor = new Seletor<>();
    private final Botao btnFinalizar = new Botao("Finalizar venda", Botao.Tipo.PRIMARIO).atalho("F2");
    private final Botao btnCancelar = new Botao("Cancelar venda", Botao.Tipo.SECUNDARIO).atalho("F4");

    public PdvView(Usuario usuario) {
        super(new BorderLayout(22, 0));
        this.usuario = usuario;
        setOpaque(false); // o fundo é o da janela, que aparece por trás dos vidros
        setBorder(new EmptyBorder(22, 26, 4, 4));
        limpaStatus.setRepeats(false);
        foto.setTextoVazio("FOTO DO PRODUTO");

        add(colunaVenda(), BorderLayout.CENTER);
        add(painelCaixa(), BorderLayout.EAST);

        txtLeitor.addActionListener(e -> enter());
        btnAdicionar.addActionListener(e -> enter());
        montaSugestoes();
        btnRemover.addActionListener(e -> removeSelecionado());
        btnFinalizar.addActionListener(e -> finaliza());
        btnCancelar.addActionListener(e -> cancela());
        atalho("F2", this::finaliza);
        atalho("F4", this::cancela);
        atalho("F6", this::focaLeitor);
        tabela.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "remover");
        tabela.getActionMap().put("remover", acao(this::removeSelecionado));

        carregaListas();
        atualizaTotais();
    }

    public void focaLeitor() {
        txtLeitor.requestFocusInWindow();
    }

    // ------------------------------------------------------------------ montagem

    private JComponent colunaVenda() {
        JPanel titulo = new JPanel();
        titulo.setOpaque(false);
        titulo.setLayout(new BoxLayout(titulo, BoxLayout.Y_AXIS));
        titulo.add(Tema.rotulo("Ponto de venda", Tema.AZUL));
        titulo.add(Box.createVerticalStrut(2));
        titulo.add(Tema.titulo("Nova venda", 34f, Tema.TINTA));

        // leitor
        txtLeitor.setFont(Tema.display(Font.PLAIN, 24f));
        txtLeitor.setPreferredSize(new Dimension(100, 54));
        btnAdicionar.setPreferredSize(new Dimension(btnAdicionar.getPreferredSize().width, 54));
        JPanel linhaLeitor = new JPanel(new BorderLayout(10, 0));
        linhaLeitor.setOpaque(false);
        linhaLeitor.add(txtLeitor, BorderLayout.CENTER);
        linhaLeitor.add(btnAdicionar, BorderLayout.EAST);
        JLabel dica = new JLabel("Mais de uma unidade? Digite a quantidade antes:  3*7891234567895  ou  3*filtro de óleo");
        dica.setFont(Tema.texto(Font.PLAIN, 12.5f));
        dica.setForeground(Tema.TINTA_SUAVE);
        Cartao leitor = new Cartao(new BorderLayout(0, 10));
        leitor.setBorder(new EmptyBorder(18, 20, 16, 20));
        leitor.add(Tema.rotulo("Bipe o produto, ou digite o código ou o nome"), BorderLayout.NORTH);
        leitor.add(linhaLeitor, BorderLayout.CENTER);
        leitor.add(dica, BorderLayout.SOUTH);

        JPanel topo = new JPanel(new BorderLayout(0, 18));
        topo.setOpaque(false);
        topo.add(titulo, BorderLayout.NORTH);
        topo.add(leitor, BorderLayout.CENTER);

        // itens
        JScrollPane rolagem = BarraFina.aplica(new JScrollPane(tabela), new Color(0x15, 0x1A, 0x26, 70));
        Cartao lista = new Cartao(new BorderLayout());
        lista.setBorder(new EmptyBorder(8, 8, 8, 8));
        lista.add(rolagem);

        // rodapé
        lblStatus.setFont(Tema.texto(Font.PLAIN, 13.5f));
        JPanel rodape = new JPanel(new BorderLayout(16, 0));
        rodape.setOpaque(false);
        rodape.add(lblStatus, BorderLayout.CENTER);
        rodape.add(btnRemover, BorderLayout.EAST);

        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.add(topo, BorderLayout.NORTH);
        p.add(lista, BorderLayout.CENTER);
        p.add(rodape, BorderLayout.SOUTH);
        return p;
    }

    private JComponent painelCaixa() {
        lblProduto.setFont(Tema.display(Font.BOLD, 19f));
        lblProduto.setForeground(Tema.TINTA);
        lblPreco.setFont(Tema.texto(Font.PLAIN, 14f));
        lblPreco.setForeground(Tema.TINTA_SUAVE);
        lblTotal.setFont(Tema.display(Font.BOLD, 54f));
        lblTotal.setForeground(Tema.TINTA);
        Tema.suave(lblTotal);
        Tema.suave(lblProduto);
        lblResumo.setFont(Tema.texto(Font.PLAIN, 13.5f));
        lblResumo.setForeground(Tema.TINTA_SUAVE);
        btnFinalizar.setPreferredSize(new Dimension(100, 50));
        // já com texto: a altura destes rótulos é medida agora, na montagem
        lblTotal.setText(Texto.moeda(BigDecimal.ZERO));
        lblResumo.setText("Nenhum item");

        // em cima: o produto que acabou de ser bipado e quem compra/vende
        Cartao produto = new Cartao(new BorderLayout(0, 14));
        produto.setBorder(new EmptyBorder(18, 18, 18, 18));
        produto.add(foto, BorderLayout.CENTER);
        produto.add(pilha(lblProduto, 2, lblPreco, 16, rotulado("Cliente", cbCliente), 12,
                rotulado("Vendedor", cbVendedor)), BorderLayout.SOUTH);

        // embaixo: o total e o fechamento. A forma de pagamento é escolhida na tela que o F2 abre.
        Cartao fechamento = new Cartao(new BorderLayout(), Vidro.Tom.CLARO, 28);
        fechamento.setBorder(new EmptyBorder(20, 22, 22, 22));
        fechamento.add(pilha(Tema.rotulo("Total", Tema.AZUL), lblTotal, lblResumo, 18,
                btnFinalizar, 8, btnCancelar));

        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(372, 0));
        p.add(produto, BorderLayout.CENTER);
        p.add(fechamento, BorderLayout.SOUTH);
        return p;
    }

    /** Empilha componentes na vertical; um número no meio da lista é um espaço em pixels. */
    private static JPanel pilha(Object... itens) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (Object item : itens) {
            if (item instanceof Integer espaco) {
                p.add(Box.createVerticalStrut(espaco));
            } else {
                JComponent c = (JComponent) item;
                c.setAlignmentX(Component.LEFT_ALIGNMENT);
                c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
                p.add(c);
            }
        }
        return p;
    }

    private static JComponent rotulado(String rotulo, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(Tema.rotulo(rotulo), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private void atalho(String tecla, Runnable acao) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), tecla);
        getActionMap().put(tecla, acao(acao));
    }

    private static AbstractAction acao(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        };
    }

    /** Lista que abre embaixo do leitor enquanto o operador digita o nome do produto. */
    private void montaSugestoes() {
        esperaDigitar.setRepeats(false);
        lstSugestoes.setCellRenderer(new ItemLista.Desenho());
        lstSugestoes.setFixedCellHeight(ItemLista.ALTURA);
        lstSugestoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstSugestoes.setBackground(Tema.PAPEL);
        // nem a lista nem o popup pegam o foco: o operador continua digitando no leitor
        lstSugestoes.setFocusable(false);
        popSugestoes.setFocusable(false);
        popSugestoes.setLayout(new BorderLayout());
        popSugestoes.setBorder(BorderFactory.createLineBorder(Tema.AZUL));
        popSugestoes.add(lstSugestoes, BorderLayout.CENTER);
        lstSugestoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = lstSugestoes.locationToIndex(e.getPoint());
                if (i >= 0) {
                    escolhe(modeloSugestoes.get(i));
                }
            }
        });
        lstSugestoes.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = lstSugestoes.locationToIndex(e.getPoint());
                if (i >= 0) {
                    lstSugestoes.setSelectedIndex(i);
                }
            }
        });
        txtLeitor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                esperaDigitar.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                esperaDigitar.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                esperaDigitar.restart();
            }
        });
        DialogoBusca.tecla(txtLeitor, "DOWN", () -> DialogoBusca.move(lstSugestoes, 1));
        DialogoBusca.tecla(txtLeitor, "UP", () -> DialogoBusca.move(lstSugestoes, -1));
        DialogoBusca.tecla(txtLeitor, "ESCAPE", () -> popSugestoes.setVisible(false));
    }

    private void carregaListas() {
        cbCliente.addItem(new Opcao(null, "Consumidor final"));
        cbVendedor.addItem(new Opcao(null, "Sem vendedor"));
        try {
            for (Cliente c : new ClienteDAO().listar()) {
                cbCliente.addItem(new Opcao(c.getId_Cliente(), c.getNome()));
            }
            for (Vendedor v : new VendedorDAO().listar()) {
                cbVendedor.addItem(new Opcao(v.getId_Vendedor(), v.getNome()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PdvView.class.getName()).log(Level.WARNING, null, ex);
            aviso("Não foi possível carregar clientes e vendedores: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ leitura do produto

    /**
     * O que está no leitor, já separado em quantidade e texto de busca.
     * "3*filtro" vira (3, "filtro"); sem asterisco a quantidade é 1. Quantidade null = inválida.
     */
    private record Entrada(BigDecimal quantidade, String busca) {
    }

    private Entrada entrada() {
        String texto = txtLeitor.getText().trim();
        int estrela = texto.indexOf('*');
        if (estrela < 0) {
            return new Entrada(BigDecimal.ONE, texto);
        }
        BigDecimal quantidade;
        try {
            quantidade = Texto.parseDecimal(texto.substring(0, estrela));
        } catch (NumberFormatException ex) {
            quantidade = null;
        }
        if (quantidade != null && (quantidade.signum() <= 0 || quantidade.compareTo(new BigDecimal("9999")) > 0)) {
            quantidade = null;
        }
        return new Entrada(quantidade, texto.substring(estrela + 1).trim());
    }

    /** Enter no leitor: se há uma sugestão marcada na lista, é ela que entra; senão trata o texto. */
    private void enter() {
        if (popSugestoes.isVisible() && lstSugestoes.getSelectedValue() != null) {
            escolhe(lstSugestoes.getSelectedValue());
        } else {
            le();
        }
    }

    /** Trata o que foi bipado ou digitado: código, código de barras ou parte do nome. */
    private void le() {
        esperaDigitar.stop();
        Entrada e = entrada();
        if (e.busca().isEmpty()) {
            return;
        }
        if (e.quantidade() == null) {
            recusa("Quantidade inválida. Exemplo: 3*7891234567895");
            return;
        }
        try {
            Produto produto = produtoDAO.buscar(e.busca());
            if (produto != null) {
                vende(produto, e.quantidade());
                return;
            }
            // não é um código: tenta como parte do nome
            List<Produto> achados = e.busca().matches("\\d+") ? List.of() : produtoDAO.pesquisar(e.busca(), MAX_SUGESTOES);
            if (achados.isEmpty()) {
                popSugestoes.setVisible(false);
                recusa("Produto não encontrado: " + e.busca());
            } else if (achados.size() == 1) {
                vende(produtoDAO.buscarPorId(achados.get(0).getId_Produto()), e.quantidade());
            } else {
                mostraSugestoes(achados);
                aviso("Há mais de um produto com esse nome: escolha na lista");
            }
        } catch (SQLException ex) {
            erro("consultar o produto", ex);
        }
    }

    /** Coloca o produto na venda, ou recusa se ele não puder ser vendido. */
    private void vende(Produto produto, BigDecimal quantidade) {
        popSugestoes.setVisible(false);
        if (produto == null) {
            recusa("Produto não encontrado");
        } else if (!produto.isAtivo()) {
            recusa("\"" + produto.getNome() + "\" está inativo e não pode ser vendido");
        } else {
            adiciona(produto, quantidade.setScale(3, RoundingMode.HALF_UP));
            txtLeitor.setText("");
            focaLeitor();
        }
    }

    // ------------------------------------------------------------------ sugestões por nome

    /** Chamado um instante depois de o operador parar de digitar. Só números = leitor/código: sem lista. */
    private void sugere() {
        Entrada e = entrada();
        if (e.busca().length() < 2 || e.busca().matches("\\d+")) {
            popSugestoes.setVisible(false);
            return;
        }
        try {
            mostraSugestoes(produtoDAO.pesquisar(e.busca(), MAX_SUGESTOES));
        } catch (SQLException ex) {
            popSugestoes.setVisible(false);
            Logger.getLogger(PdvView.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    private void mostraSugestoes(List<Produto> produtos) {
        modeloSugestoes.clear();
        for (Produto p : produtos) {
            modeloSugestoes.addElement(ProdutoView.item(p));
        }
        if (produtos.isEmpty() || !txtLeitor.isShowing()) {
            popSugestoes.setVisible(false);
            return;
        }
        lstSugestoes.setSelectedIndex(0);
        // largura da linha inteira (campo + botão), para o nome do produto caber
        popSugestoes.setPopupSize(txtLeitor.getParent().getWidth(), produtos.size() * ItemLista.ALTURA + 2);
        popSugestoes.show(txtLeitor, 0, txtLeitor.getHeight() + 4);
        focaLeitor();
    }

    /** O operador escolheu uma linha da lista de sugestões (Enter ou clique). */
    private void escolhe(ItemLista item) {
        Entrada e = entrada();
        if (e.quantidade() == null) {
            recusa("Quantidade inválida. Exemplo: 3*filtro");
            return;
        }
        try {
            vende(produtoDAO.buscarPorId(item.id()), e.quantidade());
        } catch (SQLException ex) {
            erro("consultar o produto", ex);
        }
    }

    /** Leitura que não virou item: avisa, apita e deixa o texto selecionado para o próximo bipe substituir. */
    private void recusa(String mensagem) {
        aviso(mensagem);
        Toolkit.getDefaultToolkit().beep();
        txtLeitor.selectAll();
        focaLeitor();
    }

    /** Soma à linha existente quando o produto já está na venda; senão cria uma linha nova. */
    void adiciona(Produto produto, BigDecimal quantidade) {
        int linha = -1;
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getProduto().getId_Produto() == produto.getId_Produto()) {
                linha = i;
                break;
            }
        }
        if (linha >= 0) {
            ItemVenda item = itens.get(linha);
            item.setQuantidade(item.getQuantidade().add(quantidade));
            modelo.fireTableRowsUpdated(linha, linha);
        } else {
            itens.add(new ItemVenda(produto, quantidade));
            linha = itens.size() - 1;
            modelo.fireTableRowsInserted(linha, linha);
        }
        tabela.setRowSelectionInterval(linha, linha);
        tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));

        foto.setImagem(Imagem.ler(produto.getFoto()));
        foto.setTextoVazio("PRODUTO SEM FOTO");
        lblProduto.setText(produto.getNome());
        lblPreco.setText(Texto.moeda(produto.getPrecoVenda()) + " por " + produto.getUnidade()
                + (produto.getMarca() == null ? "" : "   ·   " + produto.getMarca()));
        atualizaTotais();
        ok("Adicionado: " + Texto.quantidade(quantidade) + " × " + produto.getNome());
    }

    private void removeSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            aviso("Selecione na lista o item que deseja remover");
            return;
        }
        ItemVenda item = itens.remove(linha);
        modelo.fireTableRowsDeleted(linha, linha);
        if (!itens.isEmpty()) {
            int nova = Math.min(linha, itens.size() - 1);
            tabela.setRowSelectionInterval(nova, nova);
        }
        atualizaTotais();
        ok("Removido: " + item.getProduto().getNome());
        focaLeitor();
    }

    // ------------------------------------------------------------------ totais e fechamento

    private BigDecimal total() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    private void atualizaTotais() {
        lblTotal.setText(Texto.moeda(total()));
        int n = itens.size();
        lblResumo.setText(n == 0 ? "Nenhum item" : n == 1 ? "1 item" : n + " itens");
        btnFinalizar.setEnabled(n > 0);
        btnCancelar.setEnabled(n > 0);
        btnRemover.setEnabled(n > 0);
        tabela.repaint();
    }

    /** F2: abre a tela de pagamento por cima da venda; só grava depois que o operador confirma lá. */
    private void finaliza() {
        if (itens.isEmpty()) {
            aviso("Bipe ao menos um produto antes de finalizar");
            focaLeitor();
            return;
        }
        popSugestoes.setVisible(false);
        BigDecimal total = total();
        PagamentoDialogo.Pagamento pagamento = PagamentoDialogo.pede(this, total, itens.size());
        if (pagamento == null) {
            focaLeitor(); // voltou sem pagar: a venda continua aberta
            return;
        }
        Venda venda = new Venda();
        venda.getItens().addAll(itens);
        venda.setIdCliente(((Opcao) cbCliente.getSelectedItem()).id());
        venda.setIdVendedor(((Opcao) cbVendedor.getSelectedItem()).id());
        venda.setIdUsuario(usuario != null && usuario.getCodigo() > 0 ? usuario.getCodigo() : null);
        venda.setFormaPagamento(pagamento.forma());
        venda.setValorRecebido(pagamento.recebido());
        venda.setTroco(pagamento.troco());

        try {
            vendaDAO.salvar(venda);
        } catch (SQLException ex) {
            erro("registrar a venda", ex);
            return;
        }
        String mensagem = "Venda nº " + venda.getId_Venda() + " registrada  ·  " + Texto.moeda(total)
                + "  ·  " + pagamento.forma();
        if (venda.getTroco() != null && venda.getTroco().signum() > 0) {
            mensagem += "  ·  Troco " + Texto.moeda(venda.getTroco());
        }
        limpa();
        ok(mensagem);
        Foguetes.solta(this); // comemora a venda; não atrapalha começar a próxima
    }

    private void cancela() {
        if (itens.isEmpty()) {
            return;
        }
        if (Dialogo.confirma(this, "Cancelar venda", "Apagar todos os itens da lista e começar de novo?", "Cancelar venda")) {
            limpa();
            ok("Venda cancelada");
        }
    }

    /** Deixa a tela pronta para a próxima venda. */
    private void limpa() {
        itens.clear();
        modelo.fireTableDataChanged();
        foto.setImagem(null);
        foto.setTextoVazio("FOTO DO PRODUTO");
        lblProduto.setText("Aguardando o primeiro produto");
        lblPreco.setText(" ");
        cbCliente.setSelectedIndex(0);
        cbVendedor.setSelectedIndex(0);
        txtLeitor.setText("");
        atualizaTotais();
        focaLeitor();
    }

    // ------------------------------------------------------------------ mensagens

    private void ok(String mensagem) {
        status(mensagem, Tema.AZUL);
    }

    private void aviso(String mensagem) {
        status(mensagem, Tema.ALERTA);
    }

    private void status(String mensagem, Color cor) {
        lblStatus.setForeground(cor);
        lblStatus.setText(mensagem);
        limpaStatus.restart();
    }

    private void erro(String oQue, Exception ex) {
        Logger.getLogger(PdvView.class.getName()).log(Level.SEVERE, null, ex);
        Dialogo.erro(this, "Algo deu errado", "Não foi possível " + oQue + ".\n" + ex.getMessage());
    }

    // ------------------------------------------------------------------ lista de itens

    private static final String[] COLUNAS = {"#", "PRODUTO", "QTD", "UNITÁRIO", "SUBTOTAL"};
    private static final int[] LARGURAS = {46, 0, 88, 100, 118};

    private final class ModeloItens extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return itens.size();
        }

        @Override
        public int getColumnCount() {
            return COLUNAS.length;
        }

        @Override
        public String getColumnName(int coluna) {
            return COLUNAS[coluna];
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            return itens.get(linha); // quem desenha a célula escolhe o que mostrar
        }
    }

    private final class TabelaItens extends JTable {

        TabelaItens() {
            super(modelo);
            setOpaque(false);
            setFillsViewportHeight(true);
            setRowHeight(54);
            setShowGrid(false);
            setIntercellSpacing(new Dimension(0, 0));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setDefaultRenderer(Object.class, new CelulaItem());

            DefaultTableCellRenderer cabecalho = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int linha, int coluna) {
                    super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                    setFont(Tema.versalete(10.5f));
                    setForeground(Tema.TINTA_SUAVE);
                    setOpaque(false);
                    setHorizontalAlignment(coluna >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x15, 0x1A, 0x26, 40)), new EmptyBorder(0, 14, 0, 14)));
                    return this;
                }
            };
            getTableHeader().setDefaultRenderer(cabecalho);
            getTableHeader().setPreferredSize(new Dimension(0, 38));
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setResizingAllowed(false);
            getTableHeader().setOpaque(false);
            for (int i = 0; i < LARGURAS.length; i++) {
                if (LARGURAS[i] > 0) {
                    getColumnModel().getColumn(i).setMinWidth(LARGURAS[i]);
                    getColumnModel().getColumn(i).setMaxWidth(LARGURAS[i]);
                }
            }
        }

        /** O cabeçalho da tabela também fica transparente, para o vidro do cartão aparecer inteiro. */
        @Override
        protected void configureEnclosingScrollPane() {
            super.configureEnclosingScrollPane();
            if (getParent() != null && getParent().getParent() instanceof JScrollPane sp && sp.getColumnHeader() != null) {
                sp.getColumnHeader().setOpaque(false);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() > 0) {
                return;
            }
            // lista vazia: orienta o operador em vez de mostrar um espaço em branco
            Graphics2D g2 = Tema.g2(g);
            int cx = getWidth() / 2, cy = getHeight() / 2 - 10;
            g2.setFont(Tema.display(Font.BOLD, 20f));
            g2.setColor(Tema.TINTA_SUAVE);
            String t1 = "Nenhum item na venda";
            g2.drawString(t1, cx - g2.getFontMetrics().stringWidth(t1) / 2, cy);
            g2.setFont(Tema.texto(Font.PLAIN, 13.5f));
            String t2 = "Passe o leitor no código de barras, ou digite o código ou o nome do produto.";
            g2.drawString(t2, cx - g2.getFontMetrics().stringWidth(t2) / 2, cy + 26);
            g2.dispose();
        }
    }

    /** Desenha cada célula da lista. O produto ocupa duas linhas: nome e, abaixo, código e marca. */
    private final class CelulaItem extends JComponent implements TableCellRenderer {

        private ItemVenda item;
        private int linha, coluna;
        private boolean selecionada;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int linha, int coluna) {
            this.item = (ItemVenda) valor;
            this.linha = linha;
            this.coluna = coluna;
            this.selecionada = sel;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.g2(g);
            int w = getWidth(), h = getHeight();
            if (selecionada) {
                g2.setColor(new Color(0x24, 0x47, 0xD6, 30));
                g2.fillRect(0, 0, w, h);
            } else {
                g2.setColor(new Color(0x15, 0x1A, 0x26, 26));
                g2.fillRect(0, h - 1, w, 1);
            }
            Produto p = item.getProduto();
            int margem = 14;
            switch (coluna) {
                case 0 -> escreve(g2, String.format("%02d", linha + 1), Tema.versalete(11f), Tema.TINTA_SUAVE, margem, h, w, false);
                case 1 -> {
                    g2.setFont(Tema.texto(Font.BOLD, 14.5f));
                    g2.setColor(Tema.TINTA);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(corta(p.getNome(), fm, w - 2 * margem), margem, 23);
                    g2.setFont(Tema.texto(Font.PLAIN, 12f));
                    g2.setColor(Tema.TINTA_SUAVE);
                    String detalhe = "Cód. " + p.getId_Produto()
                            + (p.getCodigoBarras() == null ? "" : "  ·  " + p.getCodigoBarras())
                            + (p.getMarca() == null ? "" : "  ·  " + p.getMarca());
                    g2.drawString(corta(detalhe, g2.getFontMetrics(), w - 2 * margem), margem, 41);
                }
                case 2 -> escreve(g2, Texto.quantidade(item.getQuantidade()) + " " + p.getUnidade(),
                            Tema.texto(Font.PLAIN, 14f), Tema.TINTA, margem, h, w, true);
                case 3 -> escreve(g2, Texto.formata(item.getPrecoUnitario()), Tema.texto(Font.PLAIN, 14f), Tema.TINTA_SUAVE, margem, h, w, true);
                default -> escreve(g2, Texto.formata(item.getSubtotal()), Tema.display(Font.BOLD, 16f), Tema.TINTA, margem, h, w, true);
            }
            g2.dispose();
        }

        private void escreve(Graphics2D g2, String texto, Font fonte, Color cor, int margem, int h, int w, boolean direita) {
            g2.setFont(fonte);
            g2.setColor(cor);
            FontMetrics fm = g2.getFontMetrics();
            int x = direita ? w - margem - fm.stringWidth(texto) : margem;
            g2.drawString(texto, x, (h - fm.getHeight()) / 2 + fm.getAscent());
        }

        private String corta(String texto, FontMetrics fm, int largura) {
            if (fm.stringWidth(texto) <= largura) {
                return texto;
            }
            String s = texto;
            while (s.length() > 1 && fm.stringWidth(s + "…") > largura) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "…";
        }
    }
}
