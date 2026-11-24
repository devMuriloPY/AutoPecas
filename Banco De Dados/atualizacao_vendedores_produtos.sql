-- Atualiza um banco "autopecas" que já existe (script.sql só cria tabelas novas).
-- Rode este arquivo UMA vez, no MySQL Workbench, com o banco autopecas já criado.
--
-- ATENÇÃO: os campos fantasia e CNPJ de Vendedores serão apagados (junto com o que
-- estiver gravado neles). Produtos mantém os registros que já existem.

USE `autopecas`;

-- Vendedores: sem nome fantasia e sem CNPJ
ALTER TABLE `Vendedores`
  DROP COLUMN `fantasia`,
  DROP COLUMN `CNPJ`;

-- Produtos: novos campos (produtos antigos ficam com UN, custo 0, margem 0 e Ativo)
ALTER TABLE `Produtos`
  ADD COLUMN `unidade` VARCHAR(3) NOT NULL DEFAULT 'UN' AFTER `nome`,
  ADD COLUMN `marca` VARCHAR(80) NULL AFTER `unidade`,
  ADD COLUMN `preco_custo` DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER `marca`,
  ADD COLUMN `margem_lucro` DECIMAL(6,2) NOT NULL DEFAULT 0 AFTER `preco_custo`,
  ADD COLUMN `preco_venda` DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER `margem_lucro`,
  ADD COLUMN `status` VARCHAR(7) NOT NULL DEFAULT 'Ativo' AFTER `preco_venda`;
