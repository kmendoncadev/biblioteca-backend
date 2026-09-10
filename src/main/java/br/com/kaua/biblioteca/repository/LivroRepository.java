package br.com.kaua.biblioteca.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.kaua.biblioteca.entity.Livro;

public interface LivroRepository extends JpaRepository<Livro, UUID> {
	boolean existsByIsbn(String isbn);
}