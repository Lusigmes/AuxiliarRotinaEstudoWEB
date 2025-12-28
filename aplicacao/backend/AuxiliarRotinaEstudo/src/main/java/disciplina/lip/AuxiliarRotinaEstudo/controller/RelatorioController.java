package disciplina.lip.AuxiliarRotinaEstudo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstatisticaDisciplinaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstudoDiarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.RelatorioResumoDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.RevisaoStatusDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.service.RelatorioService;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    
    @Autowired
    private RelatorioService relatorioService;


    @CrossOrigin
    @GetMapping("/resumo")
    public ResponseEntity<RelatorioResumoDTO> gerarResumo(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(required=false) String inicio,
        @RequestParam(required=false) String fim){
            LocalDate dataInicio = inicio != null ? parseDate(inicio) : LocalDate.now().minusDays(30);
            LocalDate dataFim = fim != null ? parseDate(fim) : LocalDate.now();
            RelatorioResumoDTO resumo = relatorioService.gerarResumoPeriodo(usuario, dataInicio, dataFim);
           
            return ResponseEntity.ok(resumo);
        }
        
    @CrossOrigin
    @GetMapping("/disciplinas")
    public ResponseEntity<List<EstatisticaDisciplinaDTO>> gerarEstatisticasDisciplina(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(required=false) String inicio,
        @RequestParam(required=false) String fim){
            LocalDate dataInicio = inicio != null ? parseDate(inicio) : LocalDate.now().minusDays(30);
            LocalDate dataFim = fim != null ? parseDate(fim) : LocalDate.now();
          
            List<EstatisticaDisciplinaDTO> estatisticas = relatorioService.gerarEstatisticasDisciplinas(usuario, dataInicio, dataFim);
            
            return ResponseEntity.ok(estatisticas);
        }



    @CrossOrigin
    @GetMapping("/estudosDiario")
    public ResponseEntity<List<EstudoDiarioDTO>> gerarEstudoDiario(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(required=false) String inicio,
        @RequestParam(required=false) String fim){
            LocalDate dataInicio = inicio != null ? parseDate(inicio) : LocalDate.now().minusDays(30);
            LocalDate dataFim = fim != null ? parseDate(fim) : LocalDate.now();
          
            List<EstudoDiarioDTO> estudosDiarios = relatorioService.gerarEstudoDiario(usuario, dataInicio, dataFim);
            
            return ResponseEntity.ok(estudosDiarios);
        }
        
        
        @CrossOrigin
        @GetMapping("/statusRevisoes")
    public ResponseEntity<List<RevisaoStatusDTO>> gerarStatusRevisoes(
        @AuthenticationPrincipal Usuario usuario){
          
            List<RevisaoStatusDTO> status = relatorioService.gerarStatusRevisoes(usuario);
            
            return ResponseEntity.ok(status);
        }


    @CrossOrigin
    @GetMapping("/tempoDisciplina")
    public ResponseEntity<Map<String, Integer>> gerarTempoPorDisciplina(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(required=false) String inicio,
        @RequestParam(required=false) String fim){
            LocalDate dataInicio = inicio != null ? parseDate(inicio) : LocalDate.now().minusDays(30);
            LocalDate dataFim = fim != null ? parseDate(fim) : LocalDate.now();
            
            Map<String, Integer> tempoPorDisciplina = relatorioService.gerarTempoPorDisciplina(usuario, dataInicio, dataFim);
            
            return ResponseEntity.ok(tempoPorDisciplina);
        }
        
    @CrossOrigin
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
        @AuthenticationPrincipal Usuario usuario){
            LocalDate dataInicio = LocalDate.now().minusDays(7);
            // LocalDate dataInicio = LocalDate.now().minusDays(30);
            LocalDate dataFim = LocalDate.now();
            
            RelatorioResumoDTO resumo = relatorioService.gerarResumoPeriodo(usuario, dataInicio, dataFim);
            List<RevisaoStatusDTO> statusRevisoes = relatorioService.gerarStatusRevisoes(usuario);
            List<EstatisticaDisciplinaDTO>  topDisciplinas = relatorioService.gerarEstatisticasDisciplinas(usuario, dataInicio, dataFim)
                .stream().limit(5).collect(Collectors.toList());
            Map<String, Object> dashb = Map.of(
                "resumo", resumo,
                "statusRevisoes", statusRevisoes,
                "topDisciplinas", topDisciplinas
            );
                    
            return ResponseEntity.ok(dashb);
    }        
















    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de data inválido. Use dd/MM/yyyy");
        }
    }

}
