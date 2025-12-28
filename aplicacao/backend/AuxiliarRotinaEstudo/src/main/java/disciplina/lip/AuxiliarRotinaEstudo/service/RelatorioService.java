package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstatisticaDisciplinaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstudoDiarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.RelatorioResumoDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.RevisaoStatusDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.RelatorioRepository;

@Service
public class RelatorioService {

    @Autowired
    private RelatorioRepository relatorioRepository;

    public RelatorioResumoDTO gerarResumoPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        long totalEstudos = relatorioRepository.countEstudosPeriodo(usuario, inicio, fim);
        long totalRevisoesConcluidas = relatorioRepository.countRevisoesConcluidasPeriodo(usuario, inicio, fim);
        long totalRevisoesPendentes = relatorioRepository.countRevisoesPendentesPeriodo(usuario, inicio, fim);
        long totalRevisoesAtrasadas = relatorioRepository.countRevisoesAtrasadasPeriodo(usuario, inicio, fim);
        double mediaTempoDiario = relatorioRepository.findMediaTempoDiarioPeriodo(usuario, inicio, fim);
        
        List<String> disciplinasMaisEstudadas = relatorioRepository.findDisciplinaMaisEstudadaPeriodo(usuario, inicio, fim);
        String disciplinaMaisEstudada = disciplinasMaisEstudadas.isEmpty() ? "Nenhuma" : disciplinasMaisEstudadas.get(0);
        
        int tempoTotal = relatorioRepository.findTempoTotalPeriodo(usuario, inicio, fim);

        return new RelatorioResumoDTO(
            inicio,
            fim,
            (int) totalEstudos,
            (int) totalRevisoesConcluidas,
            (int) totalRevisoesPendentes,
            (int) totalRevisoesAtrasadas,
            mediaTempoDiario,
            disciplinaMaisEstudada,
            tempoTotal
        );
    }

    public List<EstatisticaDisciplinaDTO> gerarEstatisticasDisciplinas(Usuario usuario, LocalDate inicio, LocalDate fim) {
        return relatorioRepository.findEstatisticasPorDisciplina(usuario, inicio, fim);
    }

    public List<EstudoDiarioDTO> gerarEstudoDiario(Usuario usuario, LocalDate inicio, LocalDate fim) {
        return relatorioRepository.findEstudosPorDia(usuario, inicio, fim);
    }

    public List<RevisaoStatusDTO> gerarStatusRevisoes(Usuario usuario) {
        return relatorioRepository.findStatusRevisoes(usuario);
    }

    public Map<String, Integer> gerarTempoPorDisciplina(Usuario usuario, LocalDate inicio, LocalDate fim) {
        List<Object[]> results = relatorioRepository.findTempoPorDisciplina(usuario, inicio, fim);
        
        return results.stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> ((Long) result[1]).intValue()
            ));
    }
}