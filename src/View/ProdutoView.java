package View;

import DAO.ProdutoDAO;
import Model.Produto;
import Service.Imagem;
import Service.Texto;
import UI.Botao;
import UI.Campo;
import UI.Cartao;
import UI.Formulario;
import static UI.Formulario.cel;
import UI.FotoBox;
import UI.ItemLista;
import UI.Seletor;
import UI.Tema;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Cadastro de produtos, com foto guardada no banco e preço de venda calculado. */
public class ProdutoView extends TelaCadastro {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    private final Campo txtNome = new Campo();
    private final Campo txtBarras = new Campo().exemplo("bipe ou digite");
    private final Seletor<String> cbUnidade = new Seletor<>(new String[]{"UN - Unidade", "CX - Caixa", "PC - Peça",
        "KG - Quilo", "LT - Litro", "MT - Metro", "PAR - Par", "JG - Jogo"});
    private final Campo txtMarca = new Campo();
    private final Campo txtPrecoCusto = new Campo().exemplo("0,00").aDireita();
    private final Campo txtMargemLucro = new Campo().exemplo("0").aDireita();
    private final Campo txtPrecoVenda = new Campo().aDireita();
    private final Seletor<String> cbStatus = new Seletor<>(new String[]{"Ativo", "Inativo"});
    private final FotoBox foto = new FotoBox();
    private final Botao btnFoto = new Botao("Escolher foto", Botao.Tipo.SECUNDARIO);
    private final Botao btnTirarFoto = new Botao("Remover", Botao.Tipo.PERIGO);

    /** Foto já pronta para o banco (JPEG reduzido). Null = produto sem foto. */
    private byte[] fotoBytes;

    public ProdutoView() {
        super("Produtos", "produto", "Código, barras ou nome");
        txtPrecoVenda.setEnabled(false); // nunca é digitado: sai do custo e da margem
        recalculaAoDigitar(txtPrecoCusto);
        recalculaAoDigitar(txtMargemLucro);
        btnFoto.addActionListener(e -> escolheFoto());
        btnTirarFoto.addActionListener(e -> defineFoto(null));
        monta();
    }

    @Override
    protected JComponent corpo() {
        Formulario f = new Formulario();
        f.secao("Produto");
        f.linha(cel("Nome", txtNome, 9), cel("Status", cbStatus, 3));
        f.linha(cel("Código de barras", txtBarras, 5), cel("Marca", txtMarca, 4), cel("Unidade", cbUnidade, 3));
        f.secao("Preço");
        f.linha(cel("Preço de custo (R$)", txtPrecoCusto, 4), cel("Margem de lucro (%)", txtMargemLucro, 4),
                cel("Preço de venda (R$)", txtPrecoVenda, 4));
        JLabel dica = new JLabel("O preço de venda é calculado: custo + margem de lucro sobre o custo.");
        dica.setFont(Tema.texto(java.awt.Font.PLAIN, 12.5f));
        dica.setForeground(Tema.TINTA_SUAVE);
        dica.setAlignmentX(LEFT_ALIGNMENT);
        f.add(dica);
        Cartao dados = new Cartao(new BorderLayout());
        dados.add(f, BorderLayout.NORTH);

        foto.setPreferredSize(new Dimension(250, 250));
        JPanel botoesFoto = new JPanel(new BorderLayout(8, 0));
        botoesFoto.setOpaque(false);
        botoesFoto.add(btnFoto, BorderLayout.CENTER);
        botoesFoto.add(btnTirarFoto, BorderLayout.EAST);
        JPanel dicaFoto = new JPanel(new GridLayout(2, 1, 0, 2));
        dicaFoto.setOpaque(false);
        for (String linha : new String[]{"JPG ou PNG. Fica guardada no banco.", "Aparece no PDV ao bipar o produto."}) {
            JLabel l = new JLabel(linha);
            l.setFont(Tema.texto(java.awt.Font.PLAIN, 12.5f));
            l.setForeground(Tema.TINTA_SUAVE);
            dicaFoto.add(l);
        }
        JPanel sul = new JPanel(new BorderLayout(0, 12));
        sul.setOpaque(false);
        sul.add(botoesFoto, BorderLayout.NORTH);
        sul.add(dicaFoto, BorderLayout.CENTER);
        Cartao cartaoFoto = new Cartao(new BorderLayout(0, 14));
        cartaoFoto.add(Tema.rotulo("Foto do produto", Tema.AZUL), BorderLayout.NORTH);
        cartaoFoto.add(foto, BorderLayout.CENTER);
        cartaoFoto.add(sul, BorderLayout.SOUTH);
        cartaoFoto.setPreferredSize(new Dimension(300, cartaoFoto.getPreferredSize().height));

        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setOpaque(false);
        p.add(dados, BorderLayout.CENTER);
        p.add(cartaoFoto, BorderLayout.EAST);
        return p;
    }

    @Override
    protected JComponent primeiroCampo() {
        return txtNome;
    }

    @Override
    protected String nomeDoRegistro() {
        return txtNome.getText().trim();
    }

    @Override
    protected void limpaCampos() {
        for (Campo c : new Campo[]{txtNome, txtBarras, txtMarca, txtPrecoCusto, txtMargemLucro, txtPrecoVenda}) {
            c.setText("");
        }
        cbUnidade.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        defineFoto(null);
    }

    @Override
    protected void habilitaCampos(boolean habilita) {
        for (JComponent c : new JComponent[]{txtNome, txtBarras, cbUnidade, txtMarca, txtPrecoCusto, txtMargemLucro,
            cbStatus, btnFoto}) {
            c.setEnabled(habilita);
        }
        btnTirarFoto.setEnabled(habilita && fotoBytes != null);
    }

    @Override
    protected boolean grava() throws SQLException {
        if (txtNome.getText().trim().isEmpty()) {
            aviso("O nome do produto deve ser preenchido");
            txtNome.requestFocusInWindow();
            return false;
        }
        BigDecimal custo = leValor(txtPrecoCusto, "o preço de custo", "99999999.99");
        if (custo == null) {
            return false;
        }
        BigDecimal margem = leValor(txtMargemLucro, "a margem de lucro", "9999.99");
        if (margem == null) {
            return false;
        }

        Produto p = new Produto();
        p.setId_Produto(idAtual);
        p.setNome(txtNome.getText().trim());
        p.setCodigoBarras(texto(txtBarras));
        p.setUnidade(cbUnidade.getSelectedItem().toString().split(" ")[0]);
        p.setMarca(texto(txtMarca));
        p.setPrecoCusto(custo);
        p.setMargemLucro(margem);
        p.setPrecoVenda(Texto.calculaPrecoVenda(custo, margem));
        p.setStatus(cbStatus.getSelectedItem().toString());
        p.setFoto(fotoBytes);

        try {
            if (idAtual == 0) {
                produtoDAO.salvar(p);
                idAtual = p.getId_Produto();
            } else if (produtoDAO.atualizar(p) == 0) {
                aviso("Produto não encontrado. Ele pode ter sido excluído.");
                return false;
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            aviso("Já existe outro produto com esse código de barras");
            txtBarras.requestFocusInWindow();
            return false;
        }
        return true;
    }

    @Override
    protected boolean carrega(String busca) throws SQLException {
        return mostraProduto(produtoDAO.buscar(busca));
    }

    @Override
    protected boolean carregaId(int id) throws SQLException {
        return mostraProduto(produtoDAO.buscarPorId(id));
    }

    @Override
    protected List<ItemLista> pesquisa(String termo) throws SQLException {
        List<ItemLista> itens = new ArrayList<>();
        for (Produto p : produtoDAO.pesquisar(termo, 200)) {
            itens.add(item(p));
        }
        return itens;
    }

    /** Linha de lista de um produto. Também usada nas sugestões do PDV. */
    static ItemLista item(Produto p) {
        String detalhe = "Cód. " + p.getId_Produto()
                + (p.getCodigoBarras() == null ? "" : "  ·  " + p.getCodigoBarras())
                + (p.getMarca() == null ? "" : "  ·  " + p.getMarca())
                + (p.isAtivo() ? "" : "  ·  INATIVO");
        return new ItemLista(p.getId_Produto(), p.getNome(), detalhe,
                Texto.moeda(p.getPrecoVenda()) + " / " + p.getUnidade(), !p.isAtivo());
    }

    private boolean mostraProduto(Produto p) {
        if (p == null) {
            return false;
        }
        idAtual = p.getId_Produto();
        mostra(txtNome, p.getNome());
        mostra(txtBarras, p.getCodigoBarras());
        seleciona(cbUnidade, p.getUnidade());
        mostra(txtMarca, p.getMarca());
        txtPrecoCusto.setText(Texto.formata(p.getPrecoCusto()));
        txtMargemLucro.setText(Texto.formata(p.getMargemLucro()));
        txtPrecoVenda.setText(Texto.formata(p.getPrecoVenda()));
        seleciona(cbStatus, p.getStatus());
        fotoBytes = p.getFoto();
        foto.setImagem(Imagem.ler(fotoBytes));
        return true;
    }

    @Override
    protected void exclui() throws SQLException {
        Produto p = new Produto();
        p.setId_Produto(idAtual);
        produtoDAO.excluir(p);
    }

    // ------------------------------------------------------------------ foto

    private void escolheFoto() {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Escolher foto do produto");
        seletor.setAcceptAllFileFilterUsed(false);
        seletor.setFileFilter(new FileNameExtensionFilter("Imagens (JPG, PNG)", "jpg", "jpeg", "png"));
        if (seletor.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            BufferedImage imagem = Imagem.normaliza(Imagem.ler(seletor.getSelectedFile()));
            defineFoto(Imagem.paraJpeg(imagem));
        } catch (IOException ex) {
            aviso("Não foi possível abrir a imagem: " + ex.getMessage());
        }
    }

    private void defineFoto(byte[] bytes) {
        fotoBytes = bytes;
        foto.setImagem(Imagem.ler(bytes));
        btnTirarFoto.setEnabled(editavel && bytes != null);
    }

    // ------------------------------------------------------------------ preço

    /** Refaz o preço de venda sempre que o custo ou a margem de lucro mudam. */
    private void recalculaAoDigitar(Campo campo) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizaPrecoVenda();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizaPrecoVenda();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizaPrecoVenda();
            }
        });
    }

    /** Fica vazio enquanto custo ou margem não forem números válidos. */
    private void atualizaPrecoVenda() {
        try {
            BigDecimal custo = Texto.parseDecimal(txtPrecoCusto.getText());
            BigDecimal margem = Texto.parseDecimal(txtMargemLucro.getText());
            txtPrecoVenda.setText(custo == null || margem == null ? ""
                    : Texto.formata(Texto.calculaPrecoVenda(custo, margem)));
        } catch (NumberFormatException ex) {
            txtPrecoVenda.setText("");
        }
    }

    /** Devolve null (e avisa o usuário) se o valor estiver vazio, inválido ou fora do limite. */
    private BigDecimal leValor(Campo campo, String nomeCampo, String limite) {
        BigDecimal valor;
        try {
            valor = Texto.parseDecimal(campo.getText());
        } catch (NumberFormatException ex) {
            valor = null;
        }
        if (valor == null || valor.signum() < 0 || valor.compareTo(new BigDecimal(limite)) > 0) {
            aviso("Informe " + nomeCampo + " com um número entre 0 e " + limite.replace('.', ','));
            campo.requestFocusInWindow();
            return null;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Seleciona o item que começa com o código guardado no banco (ex.: "UN" -> "UN - Unidade"). */
    private static void seleciona(Seletor<String> combo, String codigo) {
        combo.setSelectedIndex(0);
        for (int i = 0; codigo != null && i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.equals(codigo) || item.startsWith(codigo + " ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
