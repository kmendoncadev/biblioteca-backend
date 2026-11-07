package br.com.kaua.biblioteca.enumeration;

public enum StatusCliente {

    ATIVO(1),
    INATIVO(2),
    SUSPENSO(3);

    private final int codigo;

    StatusCliente(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
