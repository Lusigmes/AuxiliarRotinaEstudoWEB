package disciplina.lip.AuxiliarRotinaEstudo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Estudo;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;

@RepositoryRestResource(collectionResourceRel="estudo",path="estudo")
public interface EstudoRepository extends JpaRepository<Estudo, Long> {

    @Query("SELECT e FROM Estudo e WHERE e.usuario = :usuario")
    List<Estudo> findEstudoByUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT e FROM Estudo e LEFT JOIN e.usuario WHERE e.id = :idEstudo")
    Estudo findEstudoById(@Param("idEstudo") Long idEstudo);

}
