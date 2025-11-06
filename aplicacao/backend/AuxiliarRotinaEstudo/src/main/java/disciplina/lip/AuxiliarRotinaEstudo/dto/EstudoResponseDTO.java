package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public record EstudoResponseDTO(
    Long id,
    String nomeDisciplina,
    String tema,
    int tempoDeEstudo,
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate diaDoEstudo,
    Long idUsuario
) {
    
}
    
