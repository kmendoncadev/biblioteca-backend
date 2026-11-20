package br.com.kaua.biblioteca.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TB_EMPRESTIMO_LIVRO")
@AllArgsConstructor
@NoArgsConstructor
public class ItemEmprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emprestimo_id", nullable = false)
    @JsonIgnore
    private Emprestimo emprestimo;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;

    public ItemEmprestimo(Emprestimo emprestimo, Livro livro) {
        this.emprestimo = emprestimo;
        this.livro = livro;
    }

    public boolean isDevolvido() {
        return dataDevolucao != null;
    }
}
