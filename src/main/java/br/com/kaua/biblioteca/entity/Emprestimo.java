package br.com.kaua.biblioteca.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;
import br.com.kaua.biblioteca.util.StatusEmprestimoConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TB_EMPRESTIMO")
@NoArgsConstructor
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotEmpty
    @OneToMany(mappedBy = "emprestimo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEmprestimo> itens = new ArrayList<>();

    @NotNull
    private LocalDateTime dataEmprestimo;

    @NotNull
    private LocalDateTime dataDevolucaoPrevista;

    private LocalDateTime dataDevolucaoReal;

    @NotNull
    @Convert(converter = StatusEmprestimoConverter.class)
    private StatusEmprestimo status;

    public Emprestimo(UUID id, Cliente cliente, List<Livro> livros, LocalDateTime dataEmprestimo, LocalDateTime dataDevolucaoPrevista, LocalDateTime dataDevolucaoReal, StatusEmprestimo status) {
        this.id = id;
        this.cliente = cliente;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucaoReal = dataDevolucaoReal;
        this.status = status;
        this.itens = new ArrayList<>();
        if (livros != null) {
            for (Livro livro : livros) {
                ItemEmprestimo item = new ItemEmprestimo(this, livro);
                if (status == StatusEmprestimo.DEVOLVIDO) {
                    item.setDataDevolucao(dataDevolucaoReal != null ? dataDevolucaoReal : LocalDateTime.now());
                }
                this.itens.add(item);
            }
        }
    }

    @JsonProperty("livros")
    public List<Livro> getLivros() {
        if (itens == null) {
            return Collections.emptyList();
        }
        return itens.stream()
                .map(ItemEmprestimo::getLivro)
                .toList();
    }
}
