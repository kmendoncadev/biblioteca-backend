package br.com.kaua.biblioteca.entity;

import java.util.UUID;

import br.com.kaua.biblioteca.enumeration.SituacaoCliente;
import br.com.kaua.biblioteca.enumeration.StatusCliente;
import br.com.kaua.biblioteca.util.SituacaoClienteConverter;
import br.com.kaua.biblioteca.util.StatusClienteConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TB_CLIENTE")
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(min = 3, max = 100)
	private String nome;

	@NotBlank
	@Size(min = 3, max = 20)
	@Column(unique = true, nullable = false)
	private String matricula;

	@NotBlank
	@Email
	@Size(max = 100)
	@Column(unique = true, nullable = false)
	private String email;

	@NotBlank
	@Size(min = 8, max = 20)
	private String telefone;

	@NotNull
	@Convert(converter = StatusClienteConverter.class)
	private StatusCliente status;

	@NotNull
	@Convert(converter = SituacaoClienteConverter.class)
	private SituacaoCliente situacao;

}
