package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;
import disciplina.lip.AuxiliarRotinaEstudo.repository.ItemCronogramaDiarioRepository;
import jakarta.transaction.Transactional;

@Service
public class ItemCronogramaService {
    @Autowired
    private ItemCronogramaDiarioRepository itemCronogramaRepository;

   @Transactional
    public ItemCronogramaDiario atualizar(Long idItem, Usuario usuario, ItemCronogramaDTO dto){
        ItemCronogramaDiario item = itemCronogramaRepository.findItemById(idItem, usuario.getId());
        
        if(!item.getIdUsuarioDoCronograma().equals(usuario.getId())){
            throw new SecurityException("Acesso negado");
        }

        item.setDiaSemana(dto.diaSemana());
        item.setNomeDisciplina(dto.nomeDisciplina());
        if (dto.ordem() != null) {
            item.setOrdem(dto.ordem());
        }

        return itemCronogramaRepository.save(item);
    }

    @Transactional
    public void reordenarItensDia(DiaSemana diaSemana, List<Long> idsOrdenados, Usuario usuario) {
        List<Long> idsExistentes = itemCronogramaRepository
            .findIdsByCronogramaUsuarioAndDiaSemana(usuario, diaSemana);
        
        Set<Long> idsExistentesSet = new HashSet<>(idsExistentes);
        
        for (Long id : idsOrdenados) {
            if (!idsExistentesSet.contains(id)) {
                throw new IllegalArgumentException("Item com ID " + id + " não encontrado ou não pertence ao usuário/dia");
            }
        }
        
        // ATUALIZA A ORDEM DE CADA ITEM
        for (int i = 0; i < idsOrdenados.size(); i++) {
            Long idItem = idsOrdenados.get(i);
            ItemCronogramaDiario item = itemCronogramaRepository.findById(idItem)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + idItem));
            
            if (!item.getIdUsuarioDoCronograma().equals(usuario.getId())) {
                throw new SecurityException("Acesso negado para o item: " + idItem);
            }
            
            if (item.getDiaSemana() != diaSemana) {
                throw new IllegalArgumentException("Item " + idItem + " não pertence ao dia " + diaSemana);
            }
            
            item.setOrdem(i);
            itemCronogramaRepository.save(item);
        }
    }

    public ItemCronogramaResponseDTO itemToDTO(ItemCronogramaDiario item){
        return new ItemCronogramaResponseDTO(
            item.getId(),
            item.getDiaSemana(),
            item.getNomeDisciplina(),
            item.getOrdem() 
        );
    }

}
