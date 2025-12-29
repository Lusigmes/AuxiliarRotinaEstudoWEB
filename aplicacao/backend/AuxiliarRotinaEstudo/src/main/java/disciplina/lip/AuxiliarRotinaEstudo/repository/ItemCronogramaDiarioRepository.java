package disciplina.lip.AuxiliarRotinaEstudo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;

@RepositoryRestResource(collectionResourceRel="item_cronograma",path="item_cronograma")
public interface ItemCronogramaDiarioRepository extends JpaRepository<ItemCronogramaDiario, Long> {
    
    @Query("SELECT i FROM ItemCronogramaDiario i JOIN FETCH i.cronograma c WHERE i.id = :idItem AND c.usuario.id = :idUsuario")
    ItemCronogramaDiario findItemById(@Param("idItem") Long idItem, @Param("idUsuario") Long idUsuario);


    @Query("SELECT i.id FROM ItemCronogramaDiario i JOIN i.cronograma c WHERE c.usuario = :usuario AND i.diaSemana = :diaSemana")
    List<Long> findIdsByCronogramaUsuarioAndDiaSemana(
        @Param("usuario") Usuario usuario, 
        @Param("diaSemana") DiaSemana diaSemana
    );
    
    @Query("SELECT i FROM ItemCronogramaDiario i JOIN FETCH i.cronograma c WHERE i.id = :idItem")
    ItemCronogramaDiario findByIdWithCronograma(@Param("idItem") Long idItem);
    
    @Query("SELECT i FROM ItemCronogramaDiario i JOIN i.cronograma c WHERE c.usuario = :usuario AND i.diaSemana = :diaSemana ORDER BY i.ordem ASC")
    List<ItemCronogramaDiario> findByUsuarioAndDiaSemanaOrderByOrdem(
        @Param("usuario") Usuario usuario, 
        @Param("diaSemana") DiaSemana diaSemana
    );
    
    @Query("DELETE FROM ItemCronogramaDiario i WHERE i.cronograma.id = :cronogramaId")
    @Modifying
    @Transactional
    void deleteByCronogramaId(@Param("cronogramaId") Long cronogramaId);
}

