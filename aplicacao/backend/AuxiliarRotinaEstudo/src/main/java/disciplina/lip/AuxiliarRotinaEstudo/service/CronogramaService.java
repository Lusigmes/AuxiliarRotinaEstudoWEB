package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;
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
        
        Map<DiaSemana, Integer> contadorOrdem = new HashMap<>();
        
        for(ItemCronogramaDTO dtoItem : dto.itensDoDia()){
        
            ItemCronogramaDiario item = new ItemCronogramaDiario();
            item.setDiaSemana(dtoItem.diaSemana());
            item.setNomeDisciplina(dtoItem.nomeDisciplina());
            item.setCronograma(cronogramaSalvo);
            
            if (dtoItem.ordem() != null) {
                item.setOrdem(dtoItem.ordem());
            } else {
                int ordemAtual = contadorOrdem.getOrDefault(dtoItem.diaSemana(), 0);
                item.setOrdem(ordemAtual);
                contadorOrdem.put(dtoItem.diaSemana(), ordemAtual + 1);
            }
            
            ItemCronogramaDiario itemSalvo = itemCronogramaDiarioRepository.save(item);
            cronogramaSalvo.getItemDoDia().add(itemSalvo);
        }

        return cronogramaSalvo;
    }

    @Transactional
    public Cronograma adicionarItensNoCronograma(Long idCronograma, List<ItemCronogramaDTO> novos_dtos, Usuario usuario){
        Cronograma cronograma = cronogramaRepository.findByIdCronograma(idCronograma);

        if(cronograma == null ){
            throw new IllegalArgumentException("Cronograma não encontrado com ID: " + idCronograma);
        }

        if(cronograma.getUsuarioIdPrimitivo() != usuario.getIdPrimitivo()){
            throw new SecurityException("Acesso negado");
        }

        Map<DiaSemana, Integer> contadorItensPorDia = new HashMap<>();
        
        for (ItemCronogramaDiario itemExistente : cronograma.getItemDoDia()) {
            DiaSemana dia = itemExistente.getDiaSemana();
            contadorItensPorDia.put(dia, contadorItensPorDia.getOrDefault(dia, 0) + 1);
        }

        for(ItemCronogramaDTO dtoItem : novos_dtos){
            ItemCronogramaDiario item = new ItemCronogramaDiario();
            item.setDiaSemana(dtoItem.diaSemana());
            item.setNomeDisciplina(dtoItem.nomeDisciplina());
            item.setCronograma(cronograma);
            
            if (dtoItem.ordem() != null) {
                item.setOrdem(dtoItem.ordem());
            } else {
                DiaSemana dia = dtoItem.diaSemana();
                int ordemAtual = contadorItensPorDia.getOrDefault(dia, 0);
                item.setOrdem(ordemAtual);
                contadorItensPorDia.put(dia, ordemAtual + 1);
            }
            
            ItemCronogramaDiario itemSalvo = itemCronogramaDiarioRepository.save(item);
            cronograma.getItemDoDia().add(itemSalvo);
        }

        return cronogramaRepository.save(cronograma);
    }
   
    @Transactional
    public Cronograma atualizar(CronogramaDTO dto, Usuario usuario){
        Cronograma cronograma = cronogramaRepository.findByUsuarioId(usuario.getId());

        if(cronograma == null){
            throw new IllegalStateException("Cronograma não encontrado");
        }

        itemCronogramaDiarioRepository.deleteAll(cronograma.getItemDoDia());
        cronograma.getItemDoDia().clear();

        Map<DiaSemana, Integer> contadorOrdem = new HashMap<>();
        
        for(ItemCronogramaDTO dtoItem : dto.itensDoDia()){
                
            ItemCronogramaDiario item = new ItemCronogramaDiario();
            item.setDiaSemana(dtoItem.diaSemana());
            item.setNomeDisciplina(dtoItem.nomeDisciplina());
            item.setCronograma(cronograma);
            
            if (dtoItem.ordem() != null) {
                item.setOrdem(dtoItem.ordem());
            } else {
                int ordemAtual = contadorOrdem.getOrDefault(dtoItem.diaSemana(), 0);
                item.setOrdem(ordemAtual);
                contadorOrdem.put(dtoItem.diaSemana(), ordemAtual + 1);
            }
            
            ItemCronogramaDiario itemSalvo = itemCronogramaDiarioRepository.save(item);
            cronograma.getItemDoDia().add(itemSalvo);
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
                item.getNomeDisciplina(),
                item.getOrdem()
            )).collect(Collectors.toList());
            return new CronogramaResponseDTO(
                cronograma.getId(),
                cronograma.getUsuarioId(),
                itensDTO
            );
    }
}
