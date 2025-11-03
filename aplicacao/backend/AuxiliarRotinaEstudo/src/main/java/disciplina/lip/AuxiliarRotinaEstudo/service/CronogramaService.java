package disciplina.lip.AuxiliarRotinaEstudo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.dto.CronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Cronograma;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.CronogramaRepository;

@Service
public class CronogramaService {

    @Autowired
    private CronogramaRepository cronogramaRepository;


    @Transactional
    public Cronograma salvar(CronogramaDTO dto, Usuario usuario){
       
        if(cronogramaRepository.findCronogramaByUsuario(usuario) != null){
            throw new IllegalStateException("O usuário já possui um cronograma cadastrado.");
        }

        Cronograma novoCronograma = new Cronograma();
        novoCronograma.setUsuario(usuario);


       
        return cronogramaRepository.save(novoCronograma);
    }
}
