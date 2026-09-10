package br.com.kaua.biblioteca.entity;

import java.util.UUID;

import br.com.kaua.biblioteca.enumeration.Categoria;
import br.com.kaua.biblioteca.util.CategoriaConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TB_LIVRO")
@AllArgsConstructor
@NoArgsConstructor
public class Livro {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(min = 3, max = 50)
	private String titulo;

	@NotBlank
	@Size(min = 13, max = 13)
	@Column(unique = true, nullable = false)
	private String isbn;

	@NotBlank
	@Size(min = 5, max = 50)
	private String autor;

	@NotNull
	@Convert(converter = CategoriaConverter.class)
	private Categoria categoria;

	@NotNull
	private Integer anoPublicacao;

	@NotNull
	@Min(0)
	private Integer quantidade;

}
