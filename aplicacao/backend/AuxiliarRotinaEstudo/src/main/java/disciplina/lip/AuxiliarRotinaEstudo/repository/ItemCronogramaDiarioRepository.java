package disciplina.lip.AuxiliarRotinaEstudo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;

@RepositoryRestResource(collectionResourceRel="item_cronograma",path="item_cronograma")
public interface ItemCronogramaDiarioRepository extends JpaRepository<ItemCronogramaDiario, Long> {
    
}

