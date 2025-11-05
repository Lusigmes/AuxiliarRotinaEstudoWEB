package disciplina.lip.AuxiliarRotinaEstudo.dto;

import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;

public record ItemCronogramaResponseDTO(
    long id,
    DiaSemana diaSemana,
    String nomeDisciplina
) {
    
}
