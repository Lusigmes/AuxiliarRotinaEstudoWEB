package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import disciplina.lip.AuxiliarRotinaEstudo.dto.RevisaoResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.RevisaoUpdateDTO;
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
    
    @Transactional
    public Revisao alterarDataRevisao(Long idRevisao, RevisaoUpdateDTO data){
        Revisao revisao = revisaoRepository.findRevisaoById(idRevisao);
        
        if (revisao == null) {
            throw new IllegalArgumentException("Revisão não encontrada com ID: " + idRevisao);
        }

        LocalDate novaData = data.getDataRevisao();
 
        
        if (revisao.getEstudo() != null) {
            LocalDate diaEstudo = revisao.getEstudo().getDiaDoEstudo();
            if (novaData.isBefore(diaEstudo)) {
                throw new IllegalArgumentException("Data da revisão não pode ser antes do estudo responsável");
            }
        }
        
        revisao.setDataRevisao(novaData);
        
        return revisaoRepository.save(revisao);

    }

    public List<Revisao> listarRevisoesConcluidas(Usuario usuario) {
        return revisaoRepository.findRevisoesConcluidasByUsuario(usuario);
    }

    public Page<RevisaoResponseDTO> listarRevisoesConcluidasPaginado(Usuario usuario, Pageable pageable) {
        return revisaoRepository.findRevisoesConcluidasByUsuarioPage(usuario, pageable)
            .map(this::revisaoToDTO);
    }

    public long contarRevisoesConcluidas(Usuario usuario) {
        return revisaoRepository.countRevisoesConcluidasByUsuario(usuario);
    }

    public List<Revisao> listarRevisoesPendentes(Usuario usuario){
        return revisaoRepository.findRevisoesPendentesByUsuario(usuario, LocalDate.now());
    }

    public Page<RevisaoResponseDTO> listarRevisoesPendentesPaginado(Usuario usuario,Pageable pageable) {
        return revisaoRepository.findRevisoesPendentesByUsuarioPage(usuario, LocalDate.now(), pageable)
            .map(this::revisaoToDTO);
    }

    public List<Revisao> listarRevisoes(Usuario usuario){
        return revisaoRepository.findRevisoesByUsuario(usuario);
    }
    
    public List<Revisao> listarRevisoesAtrasadas(Usuario usuario){
        return revisaoRepository.findRevisoesAtrasadasByUsuario(usuario, LocalDate.now());
    }
    
    public Page<RevisaoResponseDTO> listarRevisoesAtrasadasPaginado(Usuario usuario,Pageable pageable) {
        return revisaoRepository.findRevisoesAtrasadasByUsuarioPage(usuario, LocalDate.now(), pageable)
            .map(this::revisaoToDTO);
    }
    
    public RevisaoResponseDTO revisaoToDTO(Revisao revisao){
        return new RevisaoResponseDTO(
            revisao.getId(), revisao.getDataRevisao(), revisao.getConcluida(), revisao.getEstudoId());
    }

    public long contarRevisoesPendentes(Usuario usuario) {
        return revisaoRepository.countRevisoesPendentesByUsuario(usuario, LocalDate.now());
    }

    public long contarRevisoesAtrasadas(Usuario usuario) {
        return revisaoRepository.countRevisoesAtrasadasByUsuario(usuario, LocalDate.now());
    }
}
