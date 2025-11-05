package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.dto.CronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.CronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Cronograma;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.CronogramaRepository;
import disciplina.lip.AuxiliarRotinaEstudo.repository.ItemCronogramaDiarioRepository;

@Service
public class CronogramaService {

    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Autowired
    private ItemCronogramaDiarioRepository itemCronogramaDiarioRepository;


    @Transactional
    public Cronograma salvar(CronogramaDTO dto, Usuario usuario){
        
        if(cronogramaRepository.findByUsuarioId(usuario.getId()) != null){
            throw new IllegalStateException("O usuário já possui um cronograma cadastrado.");
        }

        Cronograma novoCronograma = new Cronograma();
        novoCronograma.setUsuario(usuario);
        novoCronograma.setItemDoDia(new ArrayList<>());

        Cronograma cronogramaSalvo =  cronogramaRepository.save(novoCronograma);
        
        for(ItemCronogramaDTO dtoItem : dto.itensDoDia()){
           
            ItemCronogramaDiario item = new ItemCronogramaDiario();
            item.setDiaSemana(dtoItem.diaSemana());
            item.setNomeDisciplina(dtoItem.nomeDisciplina());
            item.setCronograma(cronogramaSalvo);
            
            
            ItemCronogramaDiario itemSalavo = itemCronogramaDiarioRepository.save(item);
            cronogramaSalvo.getItemDoDia().add(itemSalavo);
        }

        return cronogramaSalvo;
    }

    /* VERIFICAR SITUAÇÃO: QUANDO O USUARIO FOR EDITAR UM CRONOGRAMA CRIAD, OS ITENS CRIADOS ANTES DA EDIÇÃO PERMANECEM OU SÃO EXCLUIDOS? */
    @Transactional
    public Cronograma atualizar(CronogramaDTO dto, Usuario usuariao){
        Cronograma cronograma = cronogramaRepository.findByUsuarioId(usuariao.getId());

        if(cronograma == null){
            throw new IllegalStateException("Cronograma não encontrado");
        }

        itemCronogramaDiarioRepository.deleteAll(cronograma.getItemDoDia());
        cronograma.getItemDoDia().clear();

        for(ItemCronogramaDTO dtoItem : dto.itensDoDia()){
                
            ItemCronogramaDiario item = new ItemCronogramaDiario();
            item.setDiaSemana(dtoItem.diaSemana());
            item.setNomeDisciplina(dtoItem.nomeDisciplina());
            item.setCronograma(cronograma);
            
            
            ItemCronogramaDiario itemSalavo = itemCronogramaDiarioRepository.save(item);
            cronograma.getItemDoDia().add(itemSalavo);
        }

        return cronogramaRepository.save(cronograma);
    }

    public Cronograma buscarCronogramaDoUsuario(Usuario usuario){
        return cronogramaRepository.findCronogramaByUsuario(usuario);
    }
    
    public Boolean usuarioTemCronograma(Usuario usuario){
        return cronogramaRepository.findCronogramaByUsuario(usuario) != null;
    }

    public CronogramaResponseDTO cronogramaToDTO(Cronograma cronograma){
        List<ItemCronogramaResponseDTO> itensDTO = cronograma.getItemDoDia().stream()
            .map(item -> new ItemCronogramaResponseDTO(
                item.getId(),
                item.getDiaSemana(),
                item.getNomeDisciplina()
            )).collect(Collectors.toList());
            return new CronogramaResponseDTO(
                cronograma.getId(),
                cronograma.getUsuarioId(),
                itensDTO
            );
    }
}
