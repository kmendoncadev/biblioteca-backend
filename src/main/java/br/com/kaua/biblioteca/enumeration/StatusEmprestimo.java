package br.com.kaua.biblioteca.enumeration;

public enum StatusEmprestimo {

    ATIVO(1),
    DEVOLVIDO(2),
    ATRASADO(3),
    PARCIALMENTE_DEVOLVIDO(4);

    private final int codigo;

    StatusEmprestimo(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
