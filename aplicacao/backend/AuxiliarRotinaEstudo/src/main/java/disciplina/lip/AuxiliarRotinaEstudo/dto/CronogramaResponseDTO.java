package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.util.List;

public record CronogramaResponseDTO(
    long id,
    long idUsuario,
    List<ItemCronogramaResponseDTO> itensDoDia
) {
    
}
