package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record CronogramaDTO(
    @NotEmpty
    List<ItemCronogramaDTO> itensDoDia
    
)  {
    
}
