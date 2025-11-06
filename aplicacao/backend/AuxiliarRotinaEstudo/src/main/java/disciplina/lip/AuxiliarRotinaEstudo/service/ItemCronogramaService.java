package disciplina.lip.AuxiliarRotinaEstudo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
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

        return itemCronogramaRepository.save(item);
    }
    public ItemCronogramaResponseDTO itemToDTO(ItemCronogramaDiario item){
        return new ItemCronogramaResponseDTO(
            item.getId(),
            item.getDiaSemana(),
            item.getNomeDisciplina()
        );
    }
}
