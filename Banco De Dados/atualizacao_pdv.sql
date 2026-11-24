-- Atualiza um banco "autopecas" que já existe para o PDV.
-- Rode este arquivo UMA vez, no MySQL Workbench, DEPOIS de atualizacao_vendedores_produtos.sql.
-- Não apaga nenhum dado: só acrescenta duas colunas em Produtos e cria as tabelas de venda.

USE `autopecas`;

-- Produtos: código de barras (único) e foto guardada no próprio banco
ALTER TABLE `Produtos`
  ADD COLUMN `codigo_barras` VARCHAR(20) NULL AFTER `nome`,
  ADD COLUMN `foto` MEDIUMBLOB NULL AFTER `status`,
  ADD UNIQUE INDEX `codigo_barras_UNIQUE` (`codigo_barras` ASC);

-- -----------------------------------------------------
-- Table `Vendas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Vendas` (
  `id_venda` INT NOT NULL AUTO_INCREMENT,
  `data_venda` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_cliente` INT NULL,
  `id_vendedor` INT NULL,
  `id_usuario` INT NULL,
  `forma_pagamento` VARCHAR(20) NOT NULL,
  `total` DECIMAL(10,2) NOT NULL,
  `valor_recebido` DECIMAL(10,2) NULL,
  `troco` DECIMAL(10,2) NULL,
  PRIMARY KEY (`id_venda`),
  INDEX `fk_Vendas_Clientes_idx` (`id_cliente` ASC),
  INDEX `fk_Vendas_Vendedores_idx` (`id_vendedor` ASC),
  INDEX `fk_Vendas_usuarios_idx` (`id_usuario` ASC),
  CONSTRAINT `fk_Vendas_Clientes`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `Clientes` (`id_cliente`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Vendas_Vendedores`
    FOREIGN KEY (`id_vendedor`)
    REFERENCES `Vendedores` (`id_vendedor`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Vendas_usuarios`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `usuarios` (`id_usuario`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ItensVenda`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ItensVenda` (
  `id_item` INT NOT NULL AUTO_INCREMENT,
  `id_venda` INT NOT NULL,
  `id_produto` INT NOT NULL,
  `descricao` VARCHAR(150) NOT NULL,
  `quantidade` DECIMAL(10,3) NOT NULL,
  `preco_unitario` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_item`),
  INDEX `fk_ItensVenda_Vendas_idx` (`id_venda` ASC),
  INDEX `fk_ItensVenda_Produtos_idx` (`id_produto` ASC),
  CONSTRAINT `fk_ItensVenda_Vendas`
    FOREIGN KEY (`id_venda`)
    REFERENCES `Vendas` (`id_venda`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ItensVenda_Produtos`
    FOREIGN KEY (`id_produto`)
    REFERENCES `Produtos` (`id_produto`)
    ON DELETE RESTRICT
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


