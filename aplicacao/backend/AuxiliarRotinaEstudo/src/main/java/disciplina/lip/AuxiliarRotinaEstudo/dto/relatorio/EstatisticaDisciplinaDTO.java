package disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio;

public record EstatisticaDisciplinaDTO(
    String disciplina,
    long totalEstudos,
    long totalTempo,
    double mediaTempoEstudo,
    long totalRevisoesConcluidas
) {

}
