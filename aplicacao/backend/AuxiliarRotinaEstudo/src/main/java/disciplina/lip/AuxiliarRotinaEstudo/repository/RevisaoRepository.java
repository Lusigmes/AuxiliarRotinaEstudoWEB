package disciplina.lip.AuxiliarRotinaEstudo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Revisao;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;



@RepositoryRestResource(collectionResourceRel="revisao",path="revisao")
public interface RevisaoRepository extends JpaRepository<Revisao, Long> {
    @Query("SELECT r FROM Revisao r JOIN FETCH r.estudo e WHERE e.usuario = :usuario AND r.dataRevisao <= :hoje AND r.concluida = false")
    List<Revisao> findRevisoesPendentesByUsuario(@Param("usuario") Usuario usuario, @Param("hoje") LocalDate hoje);
    
    @Query("SELECT r FROM Revisao r JOIN FETCH r.estudo e WHERE e.usuario = :usuario AND r.dataRevisao < :hoje AND r.concluida = false")
    List<Revisao> findRevisoesAtrasadasByUsuario(@Param("usuario") Usuario usuario, @Param("hoje") LocalDate hoje);
    
    @Query("SELECT r FROM Revisao r JOIN FETCH r.estudo e WHERE e.id = :idEstudo")
    List<Revisao> findByEstudoId(@Param("idEstudo") long idEstudo);

    @Query("SELECT r FROM Revisao r JOIN FETCH r.estudo e WHERE e.usuario = :usuario AND r.concluida = :concluida")
    List<Revisao> findByEstudoUsuarioAndConcluida(@Param("usuario") Usuario usuario, @Param("concluida") boolean concluida);
    
    @Query("SELECT r FROM Revisao r JOIN FETCH r.estudo e JOIN FETCH e.usuario WHERE r.id = :id")
    Revisao findByIdWithEstudoAndUsuario(@Param("id") Long id);
    
    @Query("SELECT r FROM Revisao r WHERE r.id = :idRevisao")
    Revisao findRevisaoById(@Param("idRevisao") Long idRevisao);
    
}
