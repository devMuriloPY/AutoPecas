create database locadora;

use locadora;
create table cliente(
	codigo int(5) auto_increment,
    nome varchar(45),
    endereco varchar(45),
    bairro varchar(45),
    
    PRIMARY KEY (codigo)
);


create table filme(
	codigo int(5) auto_increment,
    titulp varchar(45),
    ano varchar(4),
    
    PRIMARY KEY (codigo)
);