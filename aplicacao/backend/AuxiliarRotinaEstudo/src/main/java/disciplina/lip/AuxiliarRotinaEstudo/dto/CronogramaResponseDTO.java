package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.util.List;

public record CronogramaResponseDTO(
    Long id,
    Long idUsuario,
    List<ItemCronogramaResponseDTO> itensDoDia
) {
    
}
