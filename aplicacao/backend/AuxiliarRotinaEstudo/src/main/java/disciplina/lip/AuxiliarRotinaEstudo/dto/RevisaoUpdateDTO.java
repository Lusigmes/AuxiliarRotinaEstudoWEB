package disciplina.lip.AuxiliarRotinaEstudo.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public record RevisaoUpdateDTO(
    String dataRevisao
) {
    public LocalDate getDataRevisao() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(this.dataRevisao, formatter);
        } catch (Exception e) {
            return LocalDate.now(); 
        }
    } 
}
