package disciplina.lip.AuxiliarRotinaEstudo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Cronograma;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;

@RepositoryRestResource(collectionResourceRel="cronograma",path="cronograma")
public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {
    
    @Query("SELECT c FROM Cronograma c LEFT JOIN FETCH c.itemDoDia WHERE c.usuario = :usuario")
    Cronograma findCronogramaByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT c FROM Cronograma c LEFT JOIN FETCH c.itemDoDia WHERE c.usuario.id = :idUsuario")
    Cronograma findByUsuarioId(@Param("idUsuario") Long idUsuario);
    
    @Query("SELECT c FROM Cronograma c LEFT JOIN FETCH c.itemDoDia WHERE c.id = :idCronograma")
    Cronograma findByIdCronograma(@Param("idCronograma") Long idCronograma);

    @Modifying
    @Transactional
    @Query("DELETE FROM Cronograma c WHERE c.id = :id AND c.usuario = :usuario")
    int deleteByIdAndUsuario(@Param("id") Long id, @Param("usuario") Usuario usuario);
}
