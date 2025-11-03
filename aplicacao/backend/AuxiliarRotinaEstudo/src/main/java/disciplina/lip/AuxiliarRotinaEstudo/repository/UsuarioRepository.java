package disciplina.lip.AuxiliarRotinaEstudo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;


@RepositoryRestResource(collectionResourceRel="usuario",path="usuario")
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u JOIN FETCH u.estudos WHERE u.email = :email ")
    Usuario findByEmailWithEstudos(@Param("email") String email);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.estudos WHERE u.id = :id ")
    Usuario findByIdWithEstudos(@Param("id") long id);

    @Query(" SELECT u FROM Usuario u JOIN FETCH u.cronograma WHERE u.email = :email")
    Usuario findByEmailWithCronograma(@Param("email") String email);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.estudos LEFT JOIN FETCH u.cronograma WHERE u.email = :email")
    Usuario findByEmailWithRelations(@Param("email") String email);

}
