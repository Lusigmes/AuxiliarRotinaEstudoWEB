package disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public record EstudoDiarioDTO(
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate data,
    long quantidadeEstudos,
    long tempoTotal                                                                            
) {
    
}
