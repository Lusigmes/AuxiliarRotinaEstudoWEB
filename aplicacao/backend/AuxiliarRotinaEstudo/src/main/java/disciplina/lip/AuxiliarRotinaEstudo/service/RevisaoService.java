package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.dto.RevisaoResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Estudo;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Revisao;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.RevisaoRepository;

@Service
public class RevisaoService {

    @Autowired
    private RevisaoRepository revisaoRepository;

    @Transactional
    public void salvar(Estudo estudo, LocalDate dataRevisao){
        Revisao revisao = new Revisao();
        revisao.setConcluida(false);
        revisao.setDataRevisao(dataRevisao);
        revisao.setEstudo(estudo);

        revisaoRepository.save(revisao);
    }
    
    public void controleDeRevisoes(Estudo estudo){ // criar uma revisao para n+1 n+7 n+14 dias
        LocalDate diaDoEstudo = estudo.getDiaDoEstudo();

        salvar(estudo, diaDoEstudo.plusDays(1));
        salvar(estudo, diaDoEstudo.plusDays(7));
        salvar(estudo, diaDoEstudo.plusDays(14));
    }

    @Transactional
    public void recalcularRevisoes(Estudo estudo) { // durante edição do dia do estudo

        List<Revisao> revisoesExistentes = revisaoRepository.findByEstudoId(estudo.getId());
        
        revisaoRepository.deleteAll(revisoesExistentes);
        controleDeRevisoes(estudo);
    }

    @Transactional
    public Revisao concluirStatausRevisao(Long idRevisao){ // alterar concluida para true
        Revisao revisao = revisaoRepository.findRevisaoById(idRevisao);
        if (revisao == null) {
            throw new IllegalArgumentException("Revisão não encontrada com ID: " + idRevisao);
        }
        revisao.setConcluida(true);
        Revisao revisaoSalva = revisaoRepository.save(revisao);

        return revisaoSalva;
    }

    // buscar revisoes por id do estudo

    public List<Revisao> listarRevisoesPendentes(Usuario usuario){
        return revisaoRepository.findRevisoesPendentesByUsuario(usuario, LocalDate.now());
    }
    public List<Revisao> listarRevisoes(Usuario usuario){
        return revisaoRepository.findRevisoesByUsuario(usuario);
    }
    
    public List<Revisao> listarRevisoesAtrasadas(Usuario usuario){
        return revisaoRepository.findRevisoesAtrasadasByUsuario(usuario, LocalDate.now());
    }

    public RevisaoResponseDTO revisaoToDTO(Revisao revisao){
        return new RevisaoResponseDTO(
            revisao.getId(), revisao.getDataRevisao(), revisao.getConcluida(), revisao.getEstudoId());
    }

    // alterar data das revisoes
}
