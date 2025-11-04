package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;


public record RevisaoResponseDTO(
    long id,
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate dataRevisao,
    Boolean concluida,
    long idEstudo

) {

}
