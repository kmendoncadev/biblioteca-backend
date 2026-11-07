package br.com.kaua.biblioteca.util;

import br.com.kaua.biblioteca.enumeration.SituacaoCliente;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SituacaoClienteConverter
        implements AttributeConverter<SituacaoCliente, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SituacaoCliente situacaoCliente) {
        return situacaoCliente != null ? situacaoCliente.getCodigo() : null;
    }

    @Override
    public SituacaoCliente convertToEntityAttribute(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (SituacaoCliente situacao : SituacaoCliente.values()) {
            if (situacao.getCodigo() == codigo) {
                return situacao;
            }
        }

        throw new IllegalArgumentException(
            "Situação de cliente inválida: " + codigo
        );
    }
}
