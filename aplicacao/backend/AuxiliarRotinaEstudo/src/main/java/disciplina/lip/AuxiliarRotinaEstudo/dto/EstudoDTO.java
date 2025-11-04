package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstudoDTO(
    @NotBlank
    String nomeDisciplina,
    
    @NotBlank
    String tema,
    
    @NotNull
    int tempoDeEstudo,

    @NotNull
    String diaDoEstudo
) {
    public LocalDate getDiaDoEstudo() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(this.diaDoEstudo, formatter);
        } catch (Exception e) {
            return LocalDate.now(); // fallback
        }
    } 
}
