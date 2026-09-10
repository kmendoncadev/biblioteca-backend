package br.com.kaua.biblioteca.util;

import br.com.kaua.biblioteca.enumeration.Categoria;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CategoriaConverter
        implements AttributeConverter<Categoria, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Categoria categoria) {
        return categoria != null ? categoria.getCodigo() : null;
    }

    @Override
    public Categoria convertToEntityAttribute(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (Categoria categoria : Categoria.values()) {
            if (categoria.getCodigo() == codigo) {
                return categoria;
            }
        }

        throw new IllegalArgumentException(
            "Categoria inválida: " + codigo
        );
    }
}
