package disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public record RelatorioResumoDTO(
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate inicio,
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate fim,
    long totalEstudos,
    long totalRevisoesConcluidas,
    long totalRevisoesPendentes,
    long totalRevisoesAtrasadas,
    double mediaTempoDiario,
    String disciplinaMaisEstudada,
    long tempoTotal
) {
    
}
