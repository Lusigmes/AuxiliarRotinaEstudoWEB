package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public record RevisaoUpdateDTO(
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate dataRevisao
) {
}
