package disciplina.lip.AuxiliarRotinaEstudo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Estudo;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;

@RepositoryRestResource(collectionResourceRel="estudo",path="estudo")
public interface EstudoRepository extends JpaRepository<Estudo, Long> {

    @Query("SELECT e FROM Estudo e WHERE e.usuario = :usuario ORDER BY e.id DESC")
    List<Estudo> findEstudoByUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT e FROM Estudo e LEFT JOIN e.usuario WHERE e.id = :idEstudo")
    Estudo findEstudoById(@Param("idEstudo") Long idEstudo);
    
    
    @Query("SELECT e.nomeDisciplina FROM Estudo e WHERE e.id = :idEstudo")
    String findEstudoNomeDisciplinaById(@Param("idEstudo") Long idEstudo);
    
    
    @Query("SELECT e FROM Estudo e WHERE e.usuario = :usuario ORDER BY e.id DESC")
    Page<Estudo> findEstudoByUsuarioPage(@Param("usuario") Usuario usuario, Pageable pageable);

}
