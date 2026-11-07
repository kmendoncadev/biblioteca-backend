package br.com.kaua.biblioteca.enumeration;

public enum SituacaoCliente {

    SEM_LIVRO(1),
    COM_LIVRO(2);

    private final int codigo;

    SituacaoCliente(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
