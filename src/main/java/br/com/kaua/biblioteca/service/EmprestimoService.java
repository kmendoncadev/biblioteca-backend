package br.com.kaua.biblioteca.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.kaua.biblioteca.dto.CriarEmprestimoDTO;
import br.com.kaua.biblioteca.entity.Cliente;
import br.com.kaua.biblioteca.entity.Emprestimo;
import br.com.kaua.biblioteca.entity.Livro;
import br.com.kaua.biblioteca.enumeration.SituacaoCliente;
import br.com.kaua.biblioteca.enumeration.StatusCliente;
import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;
import br.com.kaua.biblioteca.exception.BusinessException;
import br.com.kaua.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.kaua.biblioteca.repository.ClienteRepository;
import br.com.kaua.biblioteca.repository.EmprestimoRepository;
import br.com.kaua.biblioteca.repository.LivroRepository;

@Service
public class EmprestimoService {

    private static final int PRAZO_PADRAO_DIAS = 14;

    private final EmprestimoRepository emprestimoRepository;
    private final ClienteRepository clienteRepository;
    private final LivroRepository livroRepository;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            ClienteRepository clienteRepository,
            LivroRepository livroRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.clienteRepository = clienteRepository;
        this.livroRepository = livroRepository;
    }

    @Transactional
    public Emprestimo criar(CriarEmprestimoDTO dto) {
        if (dto.getClienteId() == null) {
            throw new BusinessException("O cliente é obrigatório.");
        }

        if (dto.getLivrosIds() == null || dto.getLivrosIds().isEmpty()) {
            throw new BusinessException("Informe ao menos um livro para o empréstimo.");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));

        if (cliente.getStatus() != StatusCliente.ATIVO) {
            throw new BusinessException("Apenas clientes com status ATIVO podem realizar empréstimos.");
        }

        Set<UUID> idsLivrosUnicos = new LinkedHashSet<>(dto.getLivrosIds());
        List<Livro> livrosEmprestimo = new ArrayList<>();

        for (UUID livroId : idsLivrosUnicos) {
            Livro livro = livroRepository.findById(livroId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado com ID: " + livroId));

            if (livro.getQuantidade() <= 0) {
                throw new BusinessException("O livro '" + livro.getTitulo() + "' não possui exemplares disponíveis no acervo.");
            }

            livro.setQuantidade(livro.getQuantidade() - 1);
            livroRepository.save(livro);
            livrosEmprestimo.add(livro);
        }

        cliente.setSituacao(SituacaoCliente.COM_LIVRO);
        clienteRepository.save(cliente);

        LocalDateTime agora = LocalDateTime.now();
        int dias = (dto.getDiasParaDevolucao() != null && dto.getDiasParaDevolucao() > 0)
                ? dto.getDiasParaDevolucao()
                : PRAZO_PADRAO_DIAS;

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setCliente(cliente);
        emprestimo.setLivros(livrosEmprestimo);
        emprestimo.setDataEmprestimo(agora);
        emprestimo.setDataDevolucaoPrevista(agora.plusDays(dias));
        emprestimo.setStatus(StatusEmprestimo.ATIVO);

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public List<Emprestimo> listar() {
        List<Emprestimo> emprestimos = emprestimoRepository.findAllByOrderByDataEmprestimoDesc();
        LocalDateTime agora = LocalDateTime.now();

        for (Emprestimo emprestimo : emprestimos) {
            atualizarStatusSeAtrasado(emprestimo, agora);
        }

        return emprestimos;
    }

    @Transactional
    public Emprestimo buscarPorId(UUID id) {
        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado."));

        atualizarStatusSeAtrasado(emprestimo, LocalDateTime.now());

        return emprestimo;
    }

    @Transactional
    public Emprestimo devolver(UUID id) {
        Emprestimo emprestimo = buscarPorId(id);

        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new BusinessException("Este empréstimo já foi devolvido.");
        }

        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimo.setDataDevolucaoReal(LocalDateTime.now());

        for (Livro livro : emprestimo.getLivros()) {
            livro.setQuantidade(livro.getQuantidade() + 1);
            livroRepository.save(livro);
        }

        Emprestimo emprestimoSalvo = emprestimoRepository.saveAndFlush(emprestimo);

        UUID clienteId = emprestimoSalvo.getCliente().getId();
        boolean possuiOutrosEmprestimos = emprestimoRepository.existsByClienteIdAndStatusInAndIdNot(
                clienteId,
                List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO),
                id
        );

        if (!possuiOutrosEmprestimos) {
            Cliente cliente = emprestimoSalvo.getCliente();
            cliente.setSituacao(SituacaoCliente.SEM_LIVRO);
            clienteRepository.save(cliente);
        }

        return emprestimoSalvo;
    }

    private void atualizarStatusSeAtrasado(Emprestimo emprestimo, LocalDateTime agora) {
        if (emprestimo.getStatus() == StatusEmprestimo.ATIVO
                && agora.isAfter(emprestimo.getDataDevolucaoPrevista())) {
            emprestimo.setStatus(StatusEmprestimo.ATRASADO);
            emprestimoRepository.save(emprestimo);
        }
    }
}
