package disciplina.lip.AuxiliarRotinaEstudo.dto;

import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemCronogramaDTO(
    @NotNull
    DiaSemana diaSemana,
    
    @NotBlank
    String disciplina
) {

}
