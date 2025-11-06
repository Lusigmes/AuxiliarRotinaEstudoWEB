package disciplina.lip.AuxiliarRotinaEstudo.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import disciplina.lip.AuxiliarRotinaEstudo.model.enums.DiaSemana;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item_cronograma")
@Entity
public class ItemCronogramaDiario {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)   
    private DiaSemana diaSemana;
    
    @Column(nullable=false)
    private String nomeDisciplina;
    
    @ManyToOne
    @JoinColumn(name="cronograma_id")
    @JsonBackReference("cronograma_itens")
    private Cronograma cronograma;

    public Long getIdUsuarioDoCronograma(){
        return this.cronograma.getUsuarioId();   
    }
    
}
