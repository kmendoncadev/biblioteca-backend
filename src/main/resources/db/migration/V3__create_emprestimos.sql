CREATE TABLE TB_EMPRESTIMO (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL,
    data_emprestimo TIMESTAMP NOT NULL,
    data_devolucao_prevista TIMESTAMP NOT NULL,
    data_devolucao_real TIMESTAMP,
    status INTEGER NOT NULL,

    CONSTRAINT fk_emprestimo_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES TB_CLIENTE(id)
);

CREATE TABLE TB_EMPRESTIMO_LIVRO (
    emprestimo_id UUID NOT NULL,
    livro_id UUID NOT NULL,

    CONSTRAINT pk_emprestimo_livro
        PRIMARY KEY (emprestimo_id, livro_id),

    CONSTRAINT fk_emprestimo_livro_emprestimo
        FOREIGN KEY (emprestimo_id)
        REFERENCES TB_EMPRESTIMO(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emprestimo_livro_livro
        FOREIGN KEY (livro_id)
        REFERENCES TB_LIVRO(id)
);

CREATE INDEX idx_emprestimos_cliente
    ON TB_EMPRESTIMO(cliente_id);

CREATE INDEX idx_emprestimos_status
    ON TB_EMPRESTIMO(status);

CREATE INDEX idx_emprestimo_livro_livro
    ON TB_EMPRESTIMO_LIVRO(livro_id);
