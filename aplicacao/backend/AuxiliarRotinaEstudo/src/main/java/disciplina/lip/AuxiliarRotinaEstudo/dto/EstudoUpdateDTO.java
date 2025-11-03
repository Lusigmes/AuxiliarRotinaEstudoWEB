package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record EstudoUpdateDTO(
    String nomeDisciplina,
    String tema,
    int tempoDeEstudo,
    String diaDoEstudo
) {
    public LocalDate getDiaDoEstudo() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(this.diaDoEstudo, formatter);
        } catch (Exception e) {
            return LocalDate.now(); 
        }
    } 
}