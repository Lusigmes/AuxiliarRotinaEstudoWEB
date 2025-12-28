package disciplina.lip.AuxiliarRotinaEstudo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.query.Param;

import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstatisticaDisciplinaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstudoDiarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.RevisaoStatusDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;

public interface RelatorioRepository {
    
    List<EstatisticaDisciplinaDTO> findEstatisticasPorDisciplina(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    List<EstudoDiarioDTO> findEstudosPorDia(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    long countEstudosPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    long countRevisoesConcluidasPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    long countRevisoesPendentesPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    long countRevisoesAtrasadasPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    Double findMediaTempoDiarioPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    List<String> findDisciplinaMaisEstudadaPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    Integer findTempoTotalPeriodo(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    
    List<Object[]> findTempoPorDisciplina(
        @Param("usuario") Usuario usuario,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
    

    List<RevisaoStatusDTO> findStatusRevisoes(Usuario usuario);

}