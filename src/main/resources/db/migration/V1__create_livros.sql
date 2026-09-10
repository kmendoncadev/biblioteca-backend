CREATE TABLE TB_LIVRO (
    id UUID PRIMARY KEY,
    titulo VARCHAR(50) NOT NULL,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    autor VARCHAR(50) NOT NULL,
    categoria INTEGER NOT NULL,
    ano_publicacao INTEGER NOT NULL,
    quantidade INTEGER NOT NULL,

    CONSTRAINT chk_livros_quantidade
        CHECK (quantidade >= 0)
);

CREATE INDEX idx_livros_categoria
    ON TB_LIVRO(categoria);