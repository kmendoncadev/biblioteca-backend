package br.com.kaua.biblioteca.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kaua.biblioteca.entity.Cliente;
import br.com.kaua.biblioteca.enumeration.SituacaoCliente;
import br.com.kaua.biblioteca.enumeration.StatusCliente;
import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;
import br.com.kaua.biblioteca.exception.BusinessException;
import br.com.kaua.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.kaua.biblioteca.repository.ClienteRepository;
import br.com.kaua.biblioteca.repository.EmprestimoRepository;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final EmprestimoRepository emprestimoRepository;

	public ClienteService(ClienteRepository clienteRepository, EmprestimoRepository emprestimoRepository) {
		this.clienteRepository = clienteRepository;
		this.emprestimoRepository = emprestimoRepository;
	}

	public Cliente criar(Cliente cliente) {

		if (cliente.getStatus() == null) {
			cliente.setStatus(StatusCliente.ATIVO);
		}

		if (cliente.getSituacao() == null) {
			cliente.setSituacao(SituacaoCliente.SEM_LIVRO);
		}

		if (clienteRepository.existsByMatricula(cliente.getMatricula())) {
			throw new BusinessException("Já existe um cliente cadastrado com esta matrícula.");
		}

		if (clienteRepository.existsByEmail(cliente.getEmail())) {
			throw new BusinessException("Já existe um cliente cadastrado com este e-mail.");
		}

		return clienteRepository.save(cliente);
	}

	public List<Cliente> listar() {
		return clienteRepository.findAll();
	}

	public Cliente buscarPorId(UUID id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
	}

	public Cliente atualizar(UUID id, Cliente dadosCliente) {

		Cliente cliente = buscarPorId(id);

		if (!cliente.getMatricula().equals(dadosCliente.getMatricula())
				&& clienteRepository.existsByMatricula(dadosCliente.getMatricula())) {
			throw new BusinessException("Já existe outro cliente cadastrado com esta matrícula.");
		}

		if (!cliente.getEmail().equalsIgnoreCase(dadosCliente.getEmail())
				&& clienteRepository.existsByEmail(dadosCliente.getEmail())) {
			throw new BusinessException("Já existe outro cliente cadastrado com este e-mail.");
		}

		cliente.setNome(dadosCliente.getNome());
		cliente.setMatricula(dadosCliente.getMatricula());
		cliente.setEmail(dadosCliente.getEmail());
		cliente.setTelefone(dadosCliente.getTelefone());

		if (dadosCliente.getStatus() != null) {
			cliente.setStatus(dadosCliente.getStatus());
		}

		if (dadosCliente.getSituacao() != null) {
			cliente.setSituacao(dadosCliente.getSituacao());
		}

		return clienteRepository.save(cliente);
	}

	public void excluir(UUID id) {

		Cliente cliente = buscarPorId(id);

		if (emprestimoRepository.existsByClienteIdAndStatusIn(id, List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO, StatusEmprestimo.PARCIALMENTE_DEVOLVIDO))) {
			throw new BusinessException("Não é possível excluir o cliente pois ele possui empréstimos pendentes.");
		}

		clienteRepository.delete(cliente);
	}
}
