package View;

import UI.Botao;
import UI.Campo;
import UI.Dialogo;
import UI.DialogoBusca;
import UI.ItemLista;
import UI.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Base das telas de cadastro (cliente, vendedor, produto).
 *
 * Cuida do que é igual em todas: cabeçalho com busca, barra de botões, mensagens e os
 * três modos da tela (novo, consulta e edição). Cada tela só descreve os seus campos
 * e como ler/gravar o seu registro.
 */
public abstract class TelaCadastro extends JPanel {

    /** Código do registro que está na tela. 0 = registro novo, ainda não gravado. */
    protected int idAtual = 0;
    /** true enquanto os campos estão liberados para digitação. */
    protected boolean editavel = true;

    private final String entidade;
    private final JLabel lblEtiqueta = new JLabel();
    private final JLabel lblStatus = new JLabel(" ");
    private final Campo txtBusca = new Campo();
    private final Botao btnBuscar = new Botao("Buscar", Botao.Tipo.SECUNDARIO);
    private final Botao btnNovo = new Botao("Novo", Botao.Tipo.SECUNDARIO);
    private final Botao btnEditar = new Botao("Editar", Botao.Tipo.SECUNDARIO);
    private final Botao btnExcluir = new Botao("Excluir", Botao.Tipo.PERIGO);
    private final Botao btnSalvar = new Botao("Salvar", Botao.Tipo.PRIMARIO);
    private final Timer limpaStatus = new Timer(7000, e -> lblStatus.setText(" "));

    /**
     * @param titulo nome da tela no plural (ex.: "Clientes")
     * @param entidade nome do registro no singular e minúsculo (ex.: "cliente")
     * @param dicaBusca texto de exemplo do campo de busca
     */
    protected TelaCadastro(String titulo, String entidade, String dicaBusca) {
        super(new BorderLayout(0, 22));
        this.entidade = entidade;
        setOpaque(false); // o fundo é o da janela, que aparece por trás dos vidros
        setBorder(new EmptyBorder(22, 26, 18, 22));
        limpaStatus.setRepeats(false);

        add(cabecalho(titulo, dicaBusca), BorderLayout.NORTH);
        add(rodape(), BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        txtBusca.addActionListener(e -> buscar());
        btnNovo.addActionListener(e -> novo());
        btnEditar.addActionListener(e -> {
            modoEdicao();
            primeiroCampo().requestFocusInWindow();
        });
        btnExcluir.addActionListener(e -> excluir());
        btnSalvar.addActionListener(e -> salvar());
    }

    // ------------------------------------------------------------------ o que cada tela define

    /** Painel com os campos da tela. */
    protected abstract JComponent corpo();

    protected abstract JComponent primeiroCampo();

    protected abstract void limpaCampos();

    protected abstract void habilitaCampos(boolean habilita);

    /**
     * Valida e grava: insere quando idAtual == 0 (e então preenche idAtual), senão atualiza.
     * @return false se não gravou (a própria tela já avisou o motivo)
     */
    protected abstract boolean grava() throws SQLException;

    /**
     * Procura o registro e, se achar, preenche os campos e idAtual.
     * @return false se não encontrou
     */
    protected abstract boolean carrega(String busca) throws SQLException;

    /** Igual a carrega(), mas pelo código do registro escolhido na lista de pesquisa. */
    protected abstract boolean carregaId(int id) throws SQLException;

    /** Registros cujo nome (ou documento, marca...) contém o termo. Termo vazio lista todos. */
    protected abstract List<ItemLista> pesquisa(String termo) throws SQLException;

    protected abstract void exclui() throws SQLException;

    /** Nome do registro na tela, para a pergunta de confirmação da exclusão. */
    protected abstract String nomeDoRegistro();

    /** As telas chamam no fim do construtor, depois de criar os seus campos. */
    protected final void monta() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(corpo(), BorderLayout.NORTH);
        add(UI.BarraFina.aplica(new JScrollPane(topo), new java.awt.Color(0x15, 0x1A, 0x26, 70)), BorderLayout.CENTER);
        modoNovo();
    }

    // ------------------------------------------------------------------ montagem

    private JComponent cabecalho(String titulo, String dicaBusca) {
        JPanel esquerda = new JPanel();
        esquerda.setOpaque(false);
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.add(Tema.rotulo("Cadastro", Tema.AZUL));
        esquerda.add(Box.createVerticalStrut(2));
        JPanel linhaTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaTitulo.setOpaque(false);
        linhaTitulo.setAlignmentX(LEFT_ALIGNMENT);
        linhaTitulo.add(Tema.titulo(titulo, 34f, Tema.TINTA));
        linhaTitulo.add(Box.createHorizontalStrut(16));
        lblEtiqueta.setFont(Tema.versalete(10.5f));
        lblEtiqueta.setBorder(new EmptyBorder(6, 13, 6, 11));
        JPanel etiqueta = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // cápsula: vidro claro para registro novo, verde para registro já gravado
                Graphics2D g2 = Tema.g2(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(idAtual == 0 ? new java.awt.Color(255, 255, 255, 150) : new java.awt.Color(0x24, 0x47, 0xD6, 235));
                g2.fillRoundRect(0, 0, w, h, h, h);
                g2.setColor(new java.awt.Color(255, 255, 255, idAtual == 0 ? 235 : 70));
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
                g2.dispose();
            }
        };
        etiqueta.setOpaque(false);
        etiqueta.add(lblEtiqueta);
        linhaTitulo.add(etiqueta);
        esquerda.add(linhaTitulo);

        txtBusca.exemplo(dicaBusca);
        txtBusca.setColumns(17);
        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.setOpaque(false);
        direita.add(txtBusca);
        direita.add(btnBuscar);
        JPanel direitaBaixo = new JPanel(new BorderLayout());
        direitaBaixo.setOpaque(false);
        direitaBaixo.add(direita, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(esquerda, BorderLayout.WEST);
        p.add(direitaBaixo, BorderLayout.EAST);
        return p;
    }

    private JComponent rodape() {
        lblStatus.setFont(Tema.texto(java.awt.Font.PLAIN, 13.5f));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(btnNovo);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(Box.createHorizontalStrut(10));
        botoes.add(btnSalvar);

        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(255, 255, 255, 170)), new EmptyBorder(16, 0, 0, 0)));
        p.add(lblStatus, BorderLayout.CENTER);
        p.add(botoes, BorderLayout.EAST);
        return p;
    }

    // ------------------------------------------------------------------ modos da tela

    /** Registro novo: campos livres, Salvar insere. */
    protected final void modoNovo() {
        idAtual = 0;
        aplica(true, false, false);
    }

    /** Registro carregado só para consulta: campos travados, Editar libera. */
    protected final void modoConsulta() {
        aplica(false, true, true);
    }

    /** Edição de registro carregado: campos livres, Salvar atualiza. */
    protected final void modoEdicao() {
        aplica(true, false, true);
    }

    private void aplica(boolean campos, boolean editar, boolean excluir) {
        editavel = campos;
        habilitaCampos(campos);
        btnSalvar.setEnabled(campos);
        btnEditar.setEnabled(editar);
        btnExcluir.setEnabled(excluir);
        lblEtiqueta.setText(idAtual == 0 ? "NOVO REGISTRO" : "CÓDIGO " + idAtual);
        lblEtiqueta.setForeground(idAtual == 0 ? Tema.AZUL_ESCURO : Tema.PAPEL);
        lblEtiqueta.getParent().repaint();
    }

    // ------------------------------------------------------------------ ações

    private void novo() {
        limpaCampos();
        modoNovo();
        txtBusca.setText("");
        primeiroCampo().requestFocusInWindow();
    }

    /**
     * Com um código exato digitado, abre o registro direto. Em qualquer outro caso
     * (campo vazio ou parte do nome) abre a lista para o usuário escolher.
     */
    private void buscar() {
        String busca = txtBusca.getText().trim();
        try {
            boolean achou = !busca.isEmpty() && carrega(busca);
            if (!achou) {
                Integer id = DialogoBusca.escolhe(this, "Buscar " + entidade, "Digite parte do nome", busca,
                        termo -> pesquisa(termo));
                achou = id != null && carregaId(id);
            }
            if (achou) {
                modoConsulta();
                txtBusca.setText("");
                status("Mostrando o " + entidade + " de código " + idAtual, Tema.TINTA_SUAVE);
            }
        } catch (SQLException ex) {
            erro("buscar o " + entidade, ex);
        }
    }

    private void salvar() {
        boolean eraNovo = idAtual == 0;
        try {
            if (grava()) {
                modoConsulta();
                ok(eraNovo ? "Cadastrado com o código " + idAtual : "Alterações salvas");
            }
        } catch (SQLException ex) {
            erro("salvar o " + entidade, ex);
        }
    }

    private void excluir() {
        if (!Dialogo.confirma(this, "Excluir " + entidade,
                "Excluir \"" + nomeDoRegistro() + "\"?\nEssa ação não pode ser desfeita.", "Excluir")) {
            return;
        }
        try {
            exclui();
            novo();
            ok("Registro excluído");
        } catch (SQLIntegrityConstraintViolationException ex) {
            Dialogo.erro(this, "Não dá para excluir", "Este " + entidade + " já aparece em vendas registradas.\n"
                    + "Para parar de usá-lo, mude o status para Inativo.");
        } catch (SQLException ex) {
            erro("excluir o " + entidade, ex);
        }
    }

    // ------------------------------------------------------------------ mensagens

    /** Confirmação discreta no rodapé (some sozinha). */
    protected final void ok(String mensagem) {
        status(mensagem, Tema.AZUL);
    }

    /** Aviso no rodapé: algo que o usuário precisa corrigir. */
    protected final void aviso(String mensagem) {
        status(mensagem, Tema.ALERTA);
    }

    private void status(String mensagem, Color cor) {
        lblStatus.setForeground(cor);
        lblStatus.setText(mensagem);
        limpaStatus.restart();
    }

    /** Falha de banco: mostra em janela, porque precisa da atenção do usuário. */
    protected final void erro(String oQue, Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        Dialogo.erro(this, "Algo deu errado", "Não foi possível " + oQue + ".\n" + ex.getMessage());
    }

    // ------------------------------------------------------------------ apoio para as telas

    protected static String texto(Campo campo) {
        String t = campo.getText().trim();
        return t.isEmpty() ? null : t;
    }

    /** Guarda só os números (CPF, CNPJ, CEP, celular), mesmo que o usuário digite com máscara. */
    protected static String digitos(Campo campo) {
        String d = Service.Texto.soDigitos(campo.getText());
        return d.isEmpty() ? null : d;
    }

    protected static void mostra(Campo campo, String valor) {
        campo.setText(valor == null ? "" : valor);
    }

    /** Preenche o campo só quando a consulta trouxe um valor. */
    protected static void preenche(Campo campo, String valor) {
        if (valor != null && !valor.isBlank()) {
            campo.setText(valor.trim());
        }
    }

    /**
     * Roda uma consulta demorada (internet) fora da tela, para ela não travar,
     * e entrega o resultado de volta quando terminar.
     */
    protected final <T> void consulta(Botao botao, String oQue, Callable<T> tarefa, Consumer<T> aoTerminar) {
        botao.setEnabled(false);
        status("Consultando " + oQue + "…", Tema.TINTA_SUAVE);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarefa.call();
            }

            @Override
            protected void done() {
                botao.setEnabled(editavel);
                try {
                    T resultado = get();
                    if (resultado == null) {
                        aviso(oQue + " não encontrado");
                    } else {
                        aoTerminar.accept(resultado);
                        ok(oQue + " encontrado, confira os dados");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    aviso("Não foi possível consultar o " + oQue + ": " + causa.getMessage());
                }
            }
        }.execute();
    }
}
