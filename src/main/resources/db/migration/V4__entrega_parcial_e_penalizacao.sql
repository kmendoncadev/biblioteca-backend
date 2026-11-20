-- Adiciona id e campos para entrega parcial e penalização
ALTER TABLE TB_EMPRESTIMO_LIVRO ADD COLUMN id UUID;
UPDATE TB_EMPRESTIMO_LIVRO SET id = gen_random_uuid() WHERE id IS NULL;
ALTER TABLE TB_EMPRESTIMO_LIVRO ALTER COLUMN id SET NOT NULL;
ALTER TABLE TB_EMPRESTIMO_LIVRO DROP CONSTRAINT pk_emprestimo_livro;
ALTER TABLE TB_EMPRESTIMO_LIVRO ADD CONSTRAINT pk_emprestimo_livro PRIMARY KEY (id);
ALTER TABLE TB_EMPRESTIMO_LIVRO ADD CONSTRAINT uk_emprestimo_livro UNIQUE (emprestimo_id, livro_id);

ALTER TABLE TB_EMPRESTIMO_LIVRO ADD COLUMN data_devolucao TIMESTAMP;

-- Atualiza empréstimos já devolvidos anteriormente (se houver)
UPDATE TB_EMPRESTIMO_LIVRO el
SET data_devolucao = COALESCE(e.data_devolucao_real, CURRENT_TIMESTAMP)
FROM TB_EMPRESTIMO e
WHERE el.emprestimo_id = e.id AND e.status = 2 AND el.data_devolucao IS NULL;

-- Adiciona campo de data de fim de suspensão no cliente
ALTER TABLE TB_CLIENTE ADD COLUMN data_fim_suspensao DATE;

-- Garante que clientes sem pendências ativas fiquem com situação 1 (SEM_LIVRO)
UPDATE TB_CLIENTE c
SET situacao = 1
WHERE NOT EXISTS (
    SELECT 1 FROM TB_EMPRESTIMO_LIVRO el
    JOIN TB_EMPRESTIMO e ON el.emprestimo_id = e.id
    WHERE e.cliente_id = c.id AND e.status != 2 AND el.data_devolucao IS NULL
);
