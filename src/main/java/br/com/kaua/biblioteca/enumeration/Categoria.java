package br.com.kaua.biblioteca.enumeration;

public enum Categoria {

    ROMANCE(1),
    FANTASIA(2),
    TERROR(3),
    MISTERIO(4),
    BIOGRAFIA(5),
    HISTORIA(6),
    TECNICO(7),
    OUTROS(8);

    private final int codigo;

    Categoria(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
