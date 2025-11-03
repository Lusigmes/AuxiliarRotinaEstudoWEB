package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.dto.EstudoDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.EstudoResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.EstudoUpdateDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Estudo;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.EstudoRepository;
import disciplina.lip.AuxiliarRotinaEstudo.repository.UsuarioRepository;

@Service
public class EstudoService {
    @Autowired
    private EstudoRepository estudoRepository;
    @Autowired
    private RevisaoService revisaoService;
    // @Autowired
    // private UsuarioRepository usuarioRepository;

    @Transactional
    public Estudo salvar(EstudoDTO dto, Usuario usuario){
        Estudo estudo = new Estudo();
        estudo.setNomeDisciplina(dto.nomeDisciplina());
        estudo.setTema(dto.tema());
        estudo.setTempoDeEstudo(dto.tempoDeEstudo());
        estudo.setDiaDoEstudo(dto.getDiaDoEstudo());
        estudo.setUsuario(usuario);
        // estudo.setDiaDoEstudo(LocalDate.now());
        Estudo estudoSalvo = estudoRepository.save(estudo);
        
        revisaoService.controleDeRevisoes(estudoSalvo);
        
        return estudoSalvo;
    }

    @Transactional
    public Estudo atualizar(EstudoUpdateDTO dto, long idEstudo){
        Estudo estudo = estudoRepository.findEstudoById(idEstudo);
        
        if(estudo == null){
            throw new RuntimeException("Estudo não encontrado");
        }
        
        boolean dataAlterada = !estudo.getDiaDoEstudo().equals(dto.diaDoEstudo());
        
        
        estudo.setNomeDisciplina(dto.nomeDisciplina());
        estudo.setTema(dto.tema());
        estudo.setTempoDeEstudo(dto.tempoDeEstudo( ));
        estudo.setDiaDoEstudo(dto.getDiaDoEstudo());

        Estudo estudoAtualizado = estudoRepository.save(estudo);

        if(dataAlterada){
            revisaoService.recalcularRevisoes(estudoAtualizado);
        }

        return estudoAtualizado;
    }

    public List<EstudoResponseDTO> listarEstudosDoUsuario(Usuario usuario){
        return estudoRepository.findEstudoByUsuario(usuario)
            .stream()
            .map(this::estudoToDTO)
            .toList();
    }

    public EstudoResponseDTO estudoToDTO(Estudo estudo){
        return new EstudoResponseDTO(
            estudo.getId(),
            estudo.getNomeDisciplina(),
            estudo.getTema(),
            estudo.getTempoDeEstudo(),
            estudo.getDiaDoEstudo(),
            estudo.getUsuarioId()
        );
    }
}
