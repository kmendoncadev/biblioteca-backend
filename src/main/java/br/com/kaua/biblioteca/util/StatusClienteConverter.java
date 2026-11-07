package br.com.kaua.biblioteca.util;

import br.com.kaua.biblioteca.enumeration.StatusCliente;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusClienteConverter
        implements AttributeConverter<StatusCliente, Integer> {

    @Override
    public Integer convertToDatabaseColumn(StatusCliente statusCliente) {
        return statusCliente != null ? statusCliente.getCodigo() : null;
    }

    @Override
    public StatusCliente convertToEntityAttribute(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (StatusCliente status : StatusCliente.values()) {
            if (status.getCodigo() == codigo) {
                return status;
            }
        }

        throw new IllegalArgumentException(
            "Status de cliente inválido: " + codigo
        );
    }
}
