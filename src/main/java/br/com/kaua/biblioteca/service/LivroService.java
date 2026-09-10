package br.com.kaua.biblioteca.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kaua.biblioteca.entity.Livro;
import br.com.kaua.biblioteca.exception.BusinessException;
import br.com.kaua.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.kaua.biblioteca.repository.LivroRepository;

@Service
public class LivroService {

	private final LivroRepository livroRepository;

	public LivroService(LivroRepository livroRepository) {
		this.livroRepository = livroRepository;
	}

	public Livro criar(Livro livro) {

		if (livroRepository.existsByIsbn(livro.getIsbn())) {
			throw new BusinessException("Já existe um livro cadastrado com este ISBN.");
		}

		return livroRepository.save(livro);
	}

	public List<Livro> listar() {
		return livroRepository.findAll();
	}

	public Livro buscarPorId(UUID id) {
		return livroRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));
	}

	public Livro atualizar(UUID id, Livro dadosLivro) {

		Livro livro = buscarPorId(id);

		if (!livro.getIsbn().equals(dadosLivro.getIsbn()) && livroRepository.existsByIsbn(dadosLivro.getIsbn())) {

			throw new BusinessException("Já existe outro livro cadastrado com este ISBN.");
		}

		livro.setTitulo(dadosLivro.getTitulo());
		livro.setIsbn(dadosLivro.getIsbn());
		livro.setAutor(dadosLivro.getAutor());
		livro.setCategoria(dadosLivro.getCategoria());
		livro.setAnoPublicacao(dadosLivro.getAnoPublicacao());
		livro.setQuantidade(dadosLivro.getQuantidade());

		return livroRepository.save(livro);
	}

	public void excluir(UUID id) {

		Livro livro = buscarPorId(id);

		livroRepository.delete(livro);
	}
}
