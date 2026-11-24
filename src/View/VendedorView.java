package View;

import DAO.VendedorDAO;
import Model.Vendedor;
import Service.ConsultaCep;
import Service.Texto;
import UI.Botao;
import UI.Campo;
import UI.Cartao;
import UI.Formulario;
import static UI.Formulario.cel;
import static UI.Formulario.junto;
import UI.Seletor;
import java.awt.BorderLayout;
import UI.ItemLista;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/** Cadastro de vendedores. */
public class VendedorView extends TelaCadastro {

    private final VendedorDAO vendedorDAO = new VendedorDAO();

    private final Campo txtNome = new Campo();
    private final Campo txtCPF = new Campo().exemplo("somente números");
    private final Campo txtCEP = new Campo();
    private final Campo txtLogradouro = new Campo();
    private final Campo txtNumero = new Campo();
    private final Campo txtBairro = new Campo();
    private final Campo txtCidade = new Campo();
    private final Seletor<String> cbEstado = new Seletor<>(ClienteView.UFS);
    private final Campo txtCelular = new Campo().exemplo("DDD + número");
    private final Campo txtEmail = new Campo();
    private final Botao btnCep = new Botao("Buscar CEP", Botao.Tipo.SECUNDARIO);

    public VendedorView() {
        super("Vendedores", "vendedor", "Código ou nome");
        btnCep.addActionListener(e -> buscaCep());
        txtCEP.addActionListener(e -> buscaCep());
        monta();
    }

    @Override
    protected JComponent corpo() {
        Formulario f = new Formulario();
        f.secao("Identificação");
        f.linha(cel("Nome", txtNome, 9), cel("CPF", txtCPF, 4));
        f.secao("Endereço");
        f.linha(cel("CEP", junto(txtCEP, btnCep), 5), cel("Logradouro", txtLogradouro, 8), cel("Número", txtNumero, 2));
        f.linha(cel("Bairro", txtBairro, 5), cel("Cidade", txtCidade, 6), cel("UF", cbEstado, 2));
        f.secao("Contato");
        f.linha(cel("Celular", txtCelular, 4), cel("E-mail", txtEmail, 8));

        Cartao cartao = new Cartao(new BorderLayout());
        cartao.add(f);
        return cartao;
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
        for (Campo c : new Campo[]{txtNome, txtCPF, txtCEP, txtLogradouro, txtNumero, txtBairro, txtCidade,
            txtCelular, txtEmail}) {
            c.setText("");
        }
        cbEstado.setSelectedIndex(0);
    }

    @Override
    protected void habilitaCampos(boolean habilita) {
        for (JComponent c : new JComponent[]{txtNome, txtCPF, txtCEP, txtLogradouro, txtNumero, txtBairro,
            txtCidade, cbEstado, txtCelular, txtEmail, btnCep}) {
            c.setEnabled(habilita);
        }
    }

    @Override
    protected boolean grava() throws SQLException {
        if (txtNome.getText().trim().isEmpty()) {
            aviso("O nome do vendedor deve ser preenchido");
            txtNome.requestFocusInWindow();
            return false;
        }
        Vendedor v = new Vendedor();
        v.setId_Vendedor(idAtual);
        v.setNome(txtNome.getText().trim());
        v.setCPF(digitos(txtCPF));
        v.setCEP(digitos(txtCEP));
        v.setLogradouro(texto(txtLogradouro));
        v.setNumero(texto(txtNumero));
        v.setBairro(texto(txtBairro));
        v.setCidade(texto(txtCidade));
        v.setEstado(cbEstado.getSelectedItem().toString());
        v.setCelular(digitos(txtCelular));
        v.setEmail(texto(txtEmail));

        if (idAtual == 0) {
            vendedorDAO.salvar(v);
            idAtual = v.getId_Vendedor();
        } else if (vendedorDAO.atualizar(v) == 0) {
            aviso("Vendedor não encontrado. Ele pode ter sido excluído.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean carrega(String busca) throws SQLException {
        Vendedor v = vendedorDAO.buscarVendedorCodigo(busca);
        if (v.getId_Vendedor() == 0) {
            return false;
        }
        idAtual = v.getId_Vendedor();
        mostra(txtNome, v.getNome());
        mostra(txtCPF, v.getCPF());
        mostra(txtCEP, v.getCEP());
        mostra(txtLogradouro, v.getLogradouro());
        mostra(txtNumero, v.getNumero());
        mostra(txtBairro, v.getBairro());
        mostra(txtCidade, v.getCidade());
        ClienteView.selecionaUf(cbEstado, v.getEstado());
        mostra(txtCelular, v.getCelular());
        mostra(txtEmail, v.getEmail());
        return true;
    }

    @Override
    protected boolean carregaId(int id) throws SQLException {
        return carrega(String.valueOf(id));
    }

    @Override
    protected List<ItemLista> pesquisa(String termo) throws SQLException {
        List<ItemLista> itens = new ArrayList<>();
        for (Vendedor v : vendedorDAO.pesquisar(termo, 200)) {
            String cidade = v.getCidade() == null ? null : v.getCidade() + (v.getEstado() == null ? "" : "/" + v.getEstado());
            String detalhe = "Cód. " + v.getId_Vendedor()
                    + (v.getCPF() == null ? "" : "  ·  CPF " + v.getCPF())
                    + (cidade == null ? "" : "  ·  " + cidade);
            itens.add(new ItemLista(v.getId_Vendedor(), v.getNome(), detalhe, null, false));
        }
        return itens;
    }

    @Override
    protected void exclui() throws SQLException {
        Vendedor v = new Vendedor();
        v.setId_Vendedor(idAtual);
        vendedorDAO.excluir(v);
    }

    private void buscaCep() {
        String cep = Texto.soDigitos(txtCEP.getText());
        if (cep.length() != 8) {
            aviso("Informe um CEP com 8 dígitos");
            txtCEP.requestFocusInWindow();
            return;
        }
        consulta(btnCep, "CEP", () -> ConsultaCep.buscar(cep), end -> {
            txtCEP.setText(cep);
            preenche(txtLogradouro, end.logradouro());
            preenche(txtBairro, end.bairro());
            preenche(txtCidade, end.cidade());
            ClienteView.selecionaUf(cbEstado, end.uf());
            txtNumero.requestFocusInWindow();
        });
    }
}
