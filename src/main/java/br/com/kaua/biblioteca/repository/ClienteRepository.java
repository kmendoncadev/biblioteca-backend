package br.com.kaua.biblioteca.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.kaua.biblioteca.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
	boolean existsByMatricula(String matricula);
	boolean existsByEmail(String email);
}
