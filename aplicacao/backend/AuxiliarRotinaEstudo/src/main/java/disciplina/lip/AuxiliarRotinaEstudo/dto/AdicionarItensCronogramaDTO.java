package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record AdicionarItensCronogramaDTO(
    @NotEmpty
    List<ItemCronogramaDTO> itensDoDia
) {}