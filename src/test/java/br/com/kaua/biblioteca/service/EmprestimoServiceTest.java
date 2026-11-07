package br.com.kaua.biblioteca.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.kaua.biblioteca.dto.CriarEmprestimoDTO;
import br.com.kaua.biblioteca.entity.Cliente;
import br.com.kaua.biblioteca.entity.Emprestimo;
import br.com.kaua.biblioteca.entity.Livro;
import br.com.kaua.biblioteca.enumeration.Categoria;
import br.com.kaua.biblioteca.enumeration.SituacaoCliente;
import br.com.kaua.biblioteca.enumeration.StatusCliente;
import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;
import br.com.kaua.biblioteca.exception.BusinessException;
import br.com.kaua.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.kaua.biblioteca.repository.ClienteRepository;
import br.com.kaua.biblioteca.repository.EmprestimoRepository;
import br.com.kaua.biblioteca.repository.LivroRepository;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private EmprestimoService emprestimoService;

    private Cliente clienteAtivo;
    private Livro livro1;
    private Livro livro2;

    @BeforeEach
    void setUp() {
        clienteAtivo = new Cliente(
                UUID.randomUUID(),
                "Maria Oliveira",
                "MAT-100",
                "maria@email.com",
                "11999998888",
                StatusCliente.ATIVO,
                SituacaoCliente.SEM_LIVRO
        );

        livro1 = new Livro(
                UUID.randomUUID(),
                "Clean Code",
                "9780132350884",
                "Robert C. Martin",
                Categoria.TECNICO,
                2008,
                3
        );

        livro2 = new Livro(
                UUID.randomUUID(),
                "Refactoring",
                "9780201485677",
                "Martin Fowler",
                Categoria.TECNICO,
                1999,
                1
        );
    }

    @Test
    void criar_ComSucesso() {
        CriarEmprestimoDTO dto = new CriarEmprestimoDTO(clienteAtivo.getId(), List.of(livro1.getId(), livro2.getId()), 14);

        when(clienteRepository.findById(clienteAtivo.getId())).thenReturn(Optional.of(clienteAtivo));
        when(livroRepository.findById(livro1.getId())).thenReturn(Optional.of(livro1));
        when(livroRepository.findById(livro2.getId())).thenReturn(Optional.of(livro2));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo resultado = emprestimoService.criar(dto);

        assertNotNull(resultado);
        assertEquals(StatusEmprestimo.ATIVO, resultado.getStatus());
        assertEquals(2, resultado.getLivros().size());
        assertEquals(2, livro1.getQuantidade());
        assertEquals(0, livro2.getQuantidade());
        assertEquals(SituacaoCliente.COM_LIVRO, clienteAtivo.getSituacao());
        verify(clienteRepository).save(clienteAtivo);
        verify(livroRepository).save(livro1);
        verify(livroRepository).save(livro2);
    }

    @Test
    void criar_ClienteInativo_LancaException() {
        clienteAtivo.setStatus(StatusCliente.INATIVO);
        CriarEmprestimoDTO dto = new CriarEmprestimoDTO(clienteAtivo.getId(), List.of(livro1.getId()), 14);

        when(clienteRepository.findById(clienteAtivo.getId())).thenReturn(Optional.of(clienteAtivo));

        BusinessException exception = assertThrows(BusinessException.class, () -> emprestimoService.criar(dto));
        assertEquals("Apenas clientes com status ATIVO podem realizar empréstimos.", exception.getMessage());
        verify(emprestimoRepository, never()).save(any());
    }

    @Test
    void criar_LivroSemEstoque_LancaException() {
        livro1.setQuantidade(0);
        CriarEmprestimoDTO dto = new CriarEmprestimoDTO(clienteAtivo.getId(), List.of(livro1.getId()), 14);

        when(clienteRepository.findById(clienteAtivo.getId())).thenReturn(Optional.of(clienteAtivo));
        when(livroRepository.findById(livro1.getId())).thenReturn(Optional.of(livro1));

        BusinessException exception = assertThrows(BusinessException.class, () -> emprestimoService.criar(dto));
        assertEquals("O livro 'Clean Code' não possui exemplares disponíveis no acervo.", exception.getMessage());
    }

    @Test
    void devolver_ComSucesso_ClienteSemOutrosEmprestimos() {
        UUID emprestimoId = UUID.randomUUID();
        clienteAtivo.setSituacao(SituacaoCliente.COM_LIVRO);
        livro1.setQuantidade(2);

        Emprestimo emprestimo = new Emprestimo(
                emprestimoId,
                clienteAtivo,
                List.of(livro1),
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(9),
                null,
                StatusEmprestimo.ATIVO
        );

        when(emprestimoRepository.findById(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.saveAndFlush(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emprestimoRepository.existsByClienteIdAndStatusInAndIdNot(any(), any(), any())).thenReturn(false);

        Emprestimo devolvido = emprestimoService.devolver(emprestimoId);

        assertEquals(StatusEmprestimo.DEVOLVIDO, devolvido.getStatus());
        assertNotNull(devolvido.getDataDevolucaoReal());
        assertEquals(3, livro1.getQuantidade());
        assertEquals(SituacaoCliente.SEM_LIVRO, clienteAtivo.getSituacao());
        verify(clienteRepository).save(clienteAtivo);
        verify(livroRepository).save(livro1);
    }

    @Test
    void devolver_ComSucesso_ClienteComOutrosEmprestimos() {
        UUID emprestimoId = UUID.randomUUID();
        clienteAtivo.setSituacao(SituacaoCliente.COM_LIVRO);

        Emprestimo emprestimo = new Emprestimo(
                emprestimoId,
                clienteAtivo,
                List.of(livro1),
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(9),
                null,
                StatusEmprestimo.ATIVO
        );

        when(emprestimoRepository.findById(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.saveAndFlush(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emprestimoRepository.existsByClienteIdAndStatusInAndIdNot(any(), any(), any())).thenReturn(true);

        Emprestimo devolvido = emprestimoService.devolver(emprestimoId);

        assertEquals(StatusEmprestimo.DEVOLVIDO, devolvido.getStatus());
        assertEquals(SituacaoCliente.COM_LIVRO, clienteAtivo.getSituacao());
        verify(clienteRepository, never()).save(clienteAtivo);
    }

    @Test
    void devolver_JaDevolvido_LancaException() {
        UUID emprestimoId = UUID.randomUUID();
        Emprestimo emprestimo = new Emprestimo(
                emprestimoId,
                clienteAtivo,
                List.of(livro1),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2),
                StatusEmprestimo.DEVOLVIDO
        );

        when(emprestimoRepository.findById(emprestimoId)).thenReturn(Optional.of(emprestimo));

        BusinessException exception = assertThrows(BusinessException.class, () -> emprestimoService.devolver(emprestimoId));
        assertEquals("Este empréstimo já foi devolvido.", exception.getMessage());
    }

    @Test
    void listar_AtualizaParaAtrasado_QuandoPrazoVencido() {
        Emprestimo emprestimoAtrasado = new Emprestimo(
                UUID.randomUUID(),
                clienteAtivo,
                List.of(livro1),
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(6),
                null,
                StatusEmprestimo.ATIVO
        );

        when(emprestimoRepository.findAllByOrderByDataEmprestimoDesc()).thenReturn(List.of(emprestimoAtrasado));

        List<Emprestimo> lista = emprestimoService.listar();

        assertEquals(1, lista.size());
        assertEquals(StatusEmprestimo.ATRASADO, lista.get(0).getStatus());
        verify(emprestimoRepository).save(emprestimoAtrasado);
    }
}
