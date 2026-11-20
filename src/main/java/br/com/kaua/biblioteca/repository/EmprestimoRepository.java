package br.com.kaua.biblioteca.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.kaua.biblioteca.entity.Emprestimo;
import br.com.kaua.biblioteca.enumeration.StatusEmprestimo;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, UUID> {

    List<Emprestimo> findAllByOrderByDataEmprestimoDesc();

    boolean existsByClienteIdAndStatusIn(UUID clienteId, Collection<StatusEmprestimo> statuses);

    boolean existsByClienteIdAndStatusInAndIdNot(UUID clienteId, Collection<StatusEmprestimo> statuses, UUID id);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM ItemEmprestimo i WHERE i.livro.id = :livroId AND i.dataDevolucao IS NULL AND i.emprestimo.status IN :statuses")
    boolean existsByLivroIdAndStatusIn(@Param("livroId") UUID livroId, @Param("statuses") Collection<StatusEmprestimo> statuses);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM ItemEmprestimo i WHERE i.emprestimo.cliente.id = :clienteId AND i.dataDevolucao IS NULL AND i.emprestimo.status != br.com.kaua.biblioteca.enumeration.StatusEmprestimo.DEVOLVIDO")
    boolean existsPendingItemsByClienteId(@Param("clienteId") UUID clienteId);
}
