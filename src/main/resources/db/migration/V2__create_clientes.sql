CREATE TABLE TB_CLIENTE (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL,
    status INTEGER NOT NULL,
    situacao INTEGER NOT NULL
);

CREATE INDEX idx_clientes_matricula
    ON TB_CLIENTE(matricula);

CREATE INDEX idx_clientes_email
    ON TB_CLIENTE(email);
