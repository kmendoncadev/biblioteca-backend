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

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Emprestimo e JOIN e.livros l WHERE l.id = :livroId AND e.status IN :statuses")
    boolean existsByLivroIdAndStatusIn(@Param("livroId") UUID livroId, @Param("statuses") Collection<StatusEmprestimo> statuses);
}
