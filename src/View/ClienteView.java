package View;

import DAO.ClienteDAO;
import Model.Cliente;
import Service.ConsultaCep;
import Service.ConsultaCnpj;
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

/** Cadastro de clientes. */
public class ClienteView extends TelaCadastro {

    static final String[] UFS = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
        "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};

    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final Seletor<String> cbTipo = new Seletor<>(new String[]{"F - Física", "J - Jurídica"});
    private final Campo txtNome = new Campo();
    private final Campo txtFantasia = new Campo();
    private final Campo txtCPF = new Campo().exemplo("somente números");
    private final Campo txtCNPJ = new Campo().exemplo("somente números");
    private final Campo txtCEP = new Campo();
    private final Campo txtLogradouro = new Campo();
    private final Campo txtNumero = new Campo();
    private final Campo txtBairro = new Campo();
    private final Campo txtCidade = new Campo();
    private final Seletor<String> cbEstado = new Seletor<>(UFS);
    private final Campo txtCelular = new Campo().exemplo("DDD + número");
    private final Campo txtEmail = new Campo();
    private final Botao btnCnpj = new Botao("Buscar CNPJ", Botao.Tipo.SECUNDARIO);
    private final Botao btnCep = new Botao("Buscar CEP", Botao.Tipo.SECUNDARIO);

    public ClienteView() {
        super("Clientes", "cliente", "Código ou nome");
        btnCep.addActionListener(e -> buscaCep());
        txtCEP.addActionListener(e -> buscaCep());
        btnCnpj.addActionListener(e -> buscaCnpj());
        txtCNPJ.addActionListener(e -> buscaCnpj());
        monta();
    }

    @Override
    protected JComponent corpo() {
        Formulario f = new Formulario();
        f.secao("Identificação");
        f.linha(cel("Tipo de pessoa", cbTipo, 3), cel("CPF", txtCPF, 4), cel("CNPJ", junto(txtCNPJ, btnCnpj), 7));
        f.linha(cel("Nome ou razão social", txtNome, 7), cel("Nome fantasia", txtFantasia, 5));
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
        for (Campo c : new Campo[]{txtNome, txtFantasia, txtCPF, txtCNPJ, txtCEP, txtLogradouro, txtNumero,
            txtBairro, txtCidade, txtCelular, txtEmail}) {
            c.setText("");
        }
        cbTipo.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
    }

    @Override
    protected void habilitaCampos(boolean habilita) {
        for (JComponent c : new JComponent[]{cbTipo, txtNome, txtFantasia, txtCPF, txtCNPJ, txtCEP, txtLogradouro,
            txtNumero, txtBairro, txtCidade, cbEstado, txtCelular, txtEmail, btnCnpj, btnCep}) {
            c.setEnabled(habilita);
        }
    }

    @Override
    protected boolean grava() throws SQLException {
        if (txtNome.getText().trim().isEmpty()) {
            aviso("O nome do cliente deve ser preenchido");
            txtNome.requestFocusInWindow();
            return false;
        }
        Cliente c = new Cliente();
        c.setId_Cliente(idAtual);
        c.setNome(txtNome.getText().trim());
        c.setFantasia(texto(txtFantasia));
        c.setCPF(digitos(txtCPF));
        c.setCNPJ(digitos(txtCNPJ));
        c.setTipo(cbTipo.getSelectedItem().toString().substring(0, 1));
        c.setCEP(digitos(txtCEP));
        c.setLogradouro(texto(txtLogradouro));
        c.setNúmero(texto(txtNumero));
        c.setBairro(texto(txtBairro));
        c.setCidade(texto(txtCidade));
        c.setEstado(cbEstado.getSelectedItem().toString());
        c.setCelular(digitos(txtCelular));
        c.setEmail(texto(txtEmail));

        if (idAtual == 0) {
            clienteDAO.salvar(c);
            idAtual = c.getId_Cliente();
        } else if (clienteDAO.atualizar(c) == 0) {
            aviso("Cliente não encontrado. Ele pode ter sido excluído.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean carrega(String busca) throws SQLException {
        Cliente c = clienteDAO.buscarClienteCodigo(busca);
        if (c.getId_Cliente() == 0) {
            return false;
        }
        idAtual = c.getId_Cliente();
        mostra(txtNome, c.getNome());
        mostra(txtFantasia, c.getFantasia());
        mostra(txtCPF, c.getCPF());
        mostra(txtCNPJ, c.getCNPJ());
        cbTipo.setSelectedIndex("J".equals(c.getTipo()) ? 1 : 0);
        mostra(txtCEP, c.getCEP());
        mostra(txtLogradouro, c.getLogradouro());
        mostra(txtNumero, c.getNúmero());
        mostra(txtBairro, c.getBairro());
        mostra(txtCidade, c.getCidade());
        selecionaUf(cbEstado, c.getEstado());
        mostra(txtCelular, c.getCelular());
        mostra(txtEmail, c.getEmail());
        return true;
    }

    @Override
    protected boolean carregaId(int id) throws SQLException {
        return carrega(String.valueOf(id));
    }

    @Override
    protected List<ItemLista> pesquisa(String termo) throws SQLException {
        List<ItemLista> itens = new ArrayList<>();
        for (Cliente c : clienteDAO.pesquisar(termo, 200)) {
            String documento = c.getCNPJ() != null ? "CNPJ " + c.getCNPJ() : c.getCPF() != null ? "CPF " + c.getCPF() : null;
            String cidade = c.getCidade() == null ? null : c.getCidade() + (c.getEstado() == null ? "" : "/" + c.getEstado());
            String detalhe = "Cód. " + c.getId_Cliente()
                    + (c.getFantasia() == null ? "" : "  ·  " + c.getFantasia())
                    + (documento == null ? "" : "  ·  " + documento)
                    + (cidade == null ? "" : "  ·  " + cidade);
            itens.add(new ItemLista(c.getId_Cliente(), c.getNome(), detalhe, null, false));
        }
        return itens;
    }

    @Override
    protected void exclui() throws SQLException {
        Cliente c = new Cliente();
        c.setId_Cliente(idAtual);
        clienteDAO.excluir(c);
    }

    static void selecionaUf(Seletor<String> combo, String uf) {
        combo.setSelectedIndex(0);
        if (uf != null) {
            combo.setSelectedItem(uf.trim().toUpperCase());
        }
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
            selecionaUf(cbEstado, end.uf());
            txtNumero.requestFocusInWindow();
        });
    }

    private void buscaCnpj() {
        String cnpj = Texto.soDigitos(txtCNPJ.getText());
        if (cnpj.length() != 14) {
            aviso("Informe um CNPJ com 14 dígitos");
            txtCNPJ.requestFocusInWindow();
            return;
        }
        consulta(btnCnpj, "CNPJ", () -> ConsultaCnpj.buscar(cnpj), emp -> {
            txtCNPJ.setText(cnpj);
            cbTipo.setSelectedIndex(1); // CNPJ é sempre pessoa jurídica
            preenche(txtNome, emp.razaoSocial());
            preenche(txtFantasia, emp.nomeFantasia());
            preenche(txtCEP, emp.cep());
            preenche(txtLogradouro, emp.logradouro());
            preenche(txtNumero, emp.numero());
            preenche(txtBairro, emp.bairro());
            preenche(txtCidade, emp.cidade());
            selecionaUf(cbEstado, emp.uf());
            preenche(txtCelular, emp.telefone());
            preenche(txtEmail, emp.email());
        });
    }
}
