package br.com.kaua.biblioteca.util;

import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusEmprestimoConverter implements AttributeConverter<StatusEmprestimo, Integer> {

    @Override
    public Integer convertToDatabaseColumn(StatusEmprestimo statusEmprestimo) {
        return statusEmprestimo != null ? statusEmprestimo.getCodigo() : null;
    }

    @Override
    public StatusEmprestimo convertToEntityAttribute(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (StatusEmprestimo status : StatusEmprestimo.values()) {
            if (status.getCodigo() == codigo) {
                return status;
            }
        }

        throw new IllegalArgumentException(
            "Status de empréstimo inválido: " + codigo
        );
    }
}
