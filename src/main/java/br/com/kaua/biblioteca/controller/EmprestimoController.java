package br.com.kaua.biblioteca.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kaua.biblioteca.dto.CriarEmprestimoDTO;
import br.com.kaua.biblioteca.dto.DevolucaoDTO;
import br.com.kaua.biblioteca.entity.Emprestimo;
import br.com.kaua.biblioteca.service.EmprestimoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping
    public ResponseEntity<Emprestimo> criar(@Valid @RequestBody CriarEmprestimoDTO dto) {
        Emprestimo emprestimoSalvo = emprestimoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(emprestimoSalvo);
    }

    @GetMapping
    public ResponseEntity<List<Emprestimo>> listar() {
        return ResponseEntity.ok(emprestimoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(emprestimoService.buscarPorId(id));
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<Emprestimo> devolver(
            @PathVariable UUID id,
            @RequestBody(required = false) DevolucaoDTO dto) {
        List<UUID> livrosIds = (dto != null) ? dto.getLivrosIds() : null;
        return ResponseEntity.ok(emprestimoService.devolver(id, livrosIds));
    }
}
