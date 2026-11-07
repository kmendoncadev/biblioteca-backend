package br.com.kaua.biblioteca.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarEmprestimoDTO {

    @NotNull(message = "O ID do cliente é obrigatório.")
    private UUID clienteId;

    @NotEmpty(message = "Informe ao menos um livro para o empréstimo.")
    private List<UUID> livrosIds;

    private Integer diasParaDevolucao;
}
