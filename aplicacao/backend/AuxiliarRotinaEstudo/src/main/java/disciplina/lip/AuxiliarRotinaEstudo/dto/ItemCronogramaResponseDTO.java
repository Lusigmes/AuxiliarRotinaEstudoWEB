package disciplina.lip.AuxiliarRotinaEstudo.dto;

import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;

public record ItemCronogramaResponseDTO(
    Long id,
    DiaSemana diaSemana,
    String nomeDisciplina
) {
    
}
