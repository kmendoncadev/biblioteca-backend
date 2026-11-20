package br.com.kaua.biblioteca.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.kaua.biblioteca.dto.CriarEmprestimoDTO;
import br.com.kaua.biblioteca.entity.Cliente;
import br.com.kaua.biblioteca.entity.Emprestimo;
import br.com.kaua.biblioteca.entity.Livro;
import br.com.kaua.biblioteca.enumeration.Categoria;
import br.com.kaua.biblioteca.enumeration.SituacaoCliente;
import br.com.kaua.biblioteca.enumeration.StatusCliente;
import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;
import br.com.kaua.biblioteca.exception.GlobalExceptionHandler;
import br.com.kaua.biblioteca.service.EmprestimoService;

@ExtendWith(MockitoExtension.class)
class EmprestimoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmprestimoService emprestimoService;

    @InjectMocks
    private EmprestimoController emprestimoController;

    private Emprestimo emprestimoMock;
    private UUID emprestimoId;
    private UUID livroId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(emprestimoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        emprestimoId = UUID.randomUUID();
        livroId = UUID.randomUUID();
        Cliente cliente = new Cliente(
                UUID.randomUUID(),
                "Lucas Santos",
                "MAT-2026",
                "lucas@email.com",
                "11988887777",
                StatusCliente.ATIVO,
                SituacaoCliente.COM_LIVRO
        );
        Livro livro = new Livro(
                livroId,
                "Domain-Driven Design",
                "9780321125217",
                "Eric Evans",
                Categoria.TECNICO,
                2003,
                5
        );

        emprestimoMock = new Emprestimo(
                emprestimoId,
                cliente,
                List.of(livro),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(14),
                null,
                StatusEmprestimo.ATIVO
        );
    }

    @Test
    void criar_DeveRetornar201() throws Exception {
        when(emprestimoService.criar(any(CriarEmprestimoDTO.class))).thenReturn(emprestimoMock);

        String json = """
                {
                    "clienteId": "%s",
                    "livrosIds": ["%s"],
                    "diasParaDevolucao": 14
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(emprestimoId.toString()))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.cliente.nome").value("Lucas Santos"))
                .andExpect(jsonPath("$.livros[0].titulo").value("Domain-Driven Design"));
    }

    @Test
    void listar_DeveRetornar200() throws Exception {
        when(emprestimoService.listar()).thenReturn(List.of(emprestimoMock));

        mockMvc.perform(get("/api/emprestimos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(emprestimoId.toString()));

        verify(emprestimoService).listar();
    }

    @Test
    void buscarPorId_DeveRetornar200() throws Exception {
        when(emprestimoService.buscarPorId(emprestimoId)).thenReturn(emprestimoMock);

        mockMvc.perform(get("/api/emprestimos/{id}", emprestimoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(emprestimoId.toString()));

        verify(emprestimoService).buscarPorId(emprestimoId);
    }

    @Test
    void devolver_SemBody_DeveRetornar200() throws Exception {
        emprestimoMock.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimoMock.setDataDevolucaoReal(LocalDateTime.now());

        when(emprestimoService.devolver(eq(emprestimoId), any())).thenReturn(emprestimoMock);

        mockMvc.perform(put("/api/emprestimos/{id}/devolver", emprestimoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(emprestimoId.toString()))
                .andExpect(jsonPath("$.status").value("DEVOLVIDO"));

        verify(emprestimoService).devolver(eq(emprestimoId), any());
    }

    @Test
    void devolver_ComBodyParcial_DeveRetornar200() throws Exception {
        emprestimoMock.setStatus(StatusEmprestimo.PARCIALMENTE_DEVOLVIDO);

        when(emprestimoService.devolver(eq(emprestimoId), eq(List.of(livroId)))).thenReturn(emprestimoMock);

        String json = """
                {
                    "livrosIds": ["%s"]
                }
                """.formatted(livroId);

        mockMvc.perform(put("/api/emprestimos/{id}/devolver", emprestimoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(emprestimoId.toString()))
                .andExpect(jsonPath("$.status").value("PARCIALMENTE_DEVOLVIDO"));

        verify(emprestimoService).devolver(eq(emprestimoId), eq(List.of(livroId)));
    }
}
