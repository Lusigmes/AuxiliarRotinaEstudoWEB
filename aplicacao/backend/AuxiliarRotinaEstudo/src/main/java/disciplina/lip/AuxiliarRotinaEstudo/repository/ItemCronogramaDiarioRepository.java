package disciplina.lip.AuxiliarRotinaEstudo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;

@RepositoryRestResource(collectionResourceRel="item_cronograma",path="item_cronograma")
public interface ItemCronogramaDiarioRepository extends JpaRepository<ItemCronogramaDiario, Long> {
    
    @Query("SELECT i FROM ItemCronogramaDiario i JOIN FETCH i.cronograma c WHERE i.id = :idItem AND c.usuario.id = :idUsuario")
    ItemCronogramaDiario findItemById(@Param("idItem") Long idItem, @Param("idUsuario") Long idUsuario);
}

