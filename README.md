# AutoPeças

Sistema de balcão para loja de autopeças: cadastro de clientes, vendedores e produtos, e ponto de venda (PDV) com leitor de código de barras e foto do produto. Trabalho acadêmico (UEMG), em Java Swing com MySQL.

## O que tem

- **PDV**: bipa ou digita o código, o código de barras ou parte do nome; mostra a foto do produto; fecha a venda em dinheiro (com troco), Pix, débito ou crédito.
- **Produtos**: preço de venda calculado a partir do custo e da margem, código de barras e foto guardada no banco.
- **Clientes e vendedores**: busca de endereço pelo CEP (ViaCEP) e de empresa pelo CNPJ (BrasilAPI), sem cadastro nem token.
- **Login** com dois perfis: quem entra como `vendedor` vê só o PDV e os produtos.

## Como rodar

Você precisa de JDK 26, MySQL 8 e NetBeans (o projeto usa Ant).

1. **Crie o banco.** Rode `Banco De Dados/script.sql` no MySQL Workbench. Ele cria o banco `autopecas` com todas as tabelas.
2. **Crie um usuário para entrar no sistema:**
   ```sql
   INSERT INTO autopecas.usuarios (login, senha, tipo) VALUES ('admin', 'troque-esta-senha', 'admin');
   ```
3. **Informe o acesso ao MySQL.** Copie `config.exemplo.properties` para `config.properties`, na raiz do projeto, e preencha usuário e senha. Esse arquivo não vai para o GitHub.
4. **Abra o projeto no NetBeans e rode** (F6). A classe principal é `Controller.AutoPecas`.

Os arquivos `Banco De Dados/atualizacao_*.sql` só servem para atualizar um banco criado por uma versão antiga do `script.sql`. Em banco novo, não rode.

## Atalhos do PDV

| Tecla | O que faz |
| --- | --- |
| Enter | Adiciona o produto digitado ou escolhido na lista |
| `3*código` | Adiciona 3 unidades (aceita fração, como `1,5*12`) |
| F2 | Abre o pagamento |
| F5 a F8 | No pagamento: dinheiro, Pix, débito, crédito |
| F4 | Cancela a venda |
| F6 | Volta o cursor para o leitor |
| Delete | Remove o item selecionado |

## Organização do código

| Pasta | Conteúdo |
| --- | --- |
| `src/Model` | Cliente, Vendedor, Produto, Venda, ItemVenda, Usuario |
| `src/DAO` | Acesso ao banco, um DAO por tabela, e a `Conexao` |
| `src/View` | Telas: login, janela principal, cadastros e PDV |
| `src/UI` | Kit visual das telas: tema, botões, campos e efeito de vidro |
| `src/Service` | Consulta de CEP e CNPJ, tratamento de foto e de valores |
| `Banco De Dados` | Script de criação, atualizações e o modelo do MySQL Workbench |

As telas são escritas à mão, sem os arquivos `.form` do editor visual do NetBeans.

## Limitações conhecidas

- A senha dos usuários do sistema é guardada em texto puro no banco.
- O PDV aceita uma forma de pagamento por venda e não controla estoque.
- Não há tela para consultar vendas já feitas.
