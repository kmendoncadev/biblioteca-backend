package br.com.kaua.biblioteca.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.kaua.biblioteca.dto.CriarEmprestimoDTO;
import br.com.kaua.biblioteca.entity.Cliente;
import br.com.kaua.biblioteca.entity.Emprestimo;
import br.com.kaua.biblioteca.entity.ItemEmprestimo;
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

        if (emprestimoRepository.existsByClienteIdAndStatusIn(cliente.getId(), List.of(StatusEmprestimo.ATRASADO))) {
            throw new BusinessException("O cliente possui empréstimo(s) em atraso e não pode realizar novos empréstimos até regularizá-los.");
        }

        if (cliente.getStatus() == StatusCliente.SUSPENSO) {
            LocalDate hoje = LocalDate.now();
            if (cliente.getDataFimSuspensao() != null) {
                if (hoje.isBefore(cliente.getDataFimSuspensao())) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    throw new BusinessException("Cliente suspenso até " + cliente.getDataFimSuspensao().format(fmt) + " devido a atraso na devolução.");
                } else {
                    cliente.setStatus(StatusCliente.ATIVO);
                    cliente.setDataFimSuspensao(null);
                    clienteRepository.save(cliente);
                }
            } else {
                throw new BusinessException("Apenas clientes com status ATIVO podem realizar empréstimos.");
            }
        } else if (cliente.getStatus() != StatusCliente.ATIVO) {
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
        emprestimo.setDataEmprestimo(agora);
        emprestimo.setDataDevolucaoPrevista(agora.plusDays(dias));
        emprestimo.setStatus(StatusEmprestimo.ATIVO);

        List<ItemEmprestimo> itens = new ArrayList<>();
        for (Livro livro : livrosEmprestimo) {
            ItemEmprestimo item = new ItemEmprestimo();
            item.setEmprestimo(emprestimo);
            item.setLivro(livro);
            itens.add(item);
        }
        emprestimo.setItens(itens);

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
        return devolver(id, null);
    }

    @Transactional
    public Emprestimo devolver(UUID id, List<UUID> livrosIds) {
        Emprestimo emprestimo = buscarPorId(id);

        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new BusinessException("Este empréstimo já foi totalmente devolvido.");
        }

        List<ItemEmprestimo> itensPendentes = emprestimo.getItens().stream()
                .filter(i -> !i.isDevolvido())
                .toList();

        if (itensPendentes.isEmpty()) {
            throw new BusinessException("Não há livros pendentes para devolução neste empréstimo.");
        }

        Set<UUID> idsParaDevolver;
        if (livrosIds == null || livrosIds.isEmpty()) {
            idsParaDevolver = itensPendentes.stream()
                    .map(i -> i.getLivro().getId())
                    .collect(Collectors.toSet());
        } else {
            idsParaDevolver = new HashSet<>(livrosIds);
        }

        List<ItemEmprestimo> itensADevolver = itensPendentes.stream()
                .filter(i -> idsParaDevolver.contains(i.getLivro().getId()))
                .toList();

        if (itensADevolver.isEmpty()) {
            throw new BusinessException("Nenhum dos livros informados está pendente de devolução neste empréstimo.");
        }

        LocalDateTime agora = LocalDateTime.now();

        for (ItemEmprestimo item : itensADevolver) {
            item.setDataDevolucao(agora);
            Livro livro = item.getLivro();
            livro.setQuantidade(livro.getQuantidade() + 1);
            livroRepository.save(livro);
        }

        if (agora.isAfter(emprestimo.getDataDevolucaoPrevista())) {
            long diasAtraso = ChronoUnit.DAYS.between(
                    emprestimo.getDataDevolucaoPrevista().toLocalDate(),
                    agora.toLocalDate()
            );
            if (diasAtraso > 0) {
                long diasSuspensao = diasAtraso * 2;
                Cliente cliente = emprestimo.getCliente();
                cliente.setStatus(StatusCliente.SUSPENSO);

                LocalDate dataBase = (cliente.getDataFimSuspensao() != null && cliente.getDataFimSuspensao().isAfter(agora.toLocalDate()))
                        ? cliente.getDataFimSuspensao()
                        : agora.toLocalDate();

                cliente.setDataFimSuspensao(dataBase.plusDays(diasSuspensao));
            }
        }

        boolean possuiItensRestantes = emprestimo.getItens().stream()
                .anyMatch(i -> !i.isDevolvido());

        if (!possuiItensRestantes) {
            emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
            emprestimo.setDataDevolucaoReal(agora);
        } else {
            if (agora.isAfter(emprestimo.getDataDevolucaoPrevista())) {
                emprestimo.setStatus(StatusEmprestimo.ATRASADO);
            } else {
                emprestimo.setStatus(StatusEmprestimo.PARCIALMENTE_DEVOLVIDO);
            }
        }

        Emprestimo emprestimoSalvo = emprestimoRepository.saveAndFlush(emprestimo);

        UUID clienteId = emprestimoSalvo.getCliente().getId();
        boolean possuiLivrosPendentes = emprestimoRepository.existsPendingItemsByClienteId(clienteId);

        Cliente cliente = emprestimoSalvo.getCliente();
        cliente.setSituacao(possuiLivrosPendentes ? SituacaoCliente.COM_LIVRO : SituacaoCliente.SEM_LIVRO);
        clienteRepository.save(cliente);

        return emprestimoSalvo;
    }

    private void atualizarStatusSeAtrasado(Emprestimo emprestimo, LocalDateTime agora) {
        if ((emprestimo.getStatus() == StatusEmprestimo.ATIVO || emprestimo.getStatus() == StatusEmprestimo.PARCIALMENTE_DEVOLVIDO)
                && agora.isAfter(emprestimo.getDataDevolucaoPrevista())) {
            emprestimo.setStatus(StatusEmprestimo.ATRASADO);
            emprestimoRepository.save(emprestimo);
        }
    }
}
