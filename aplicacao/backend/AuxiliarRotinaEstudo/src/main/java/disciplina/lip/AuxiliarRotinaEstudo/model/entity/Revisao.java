package disciplina.lip.AuxiliarRotinaEstudo.model.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "revisao")
public class Revisao {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private LocalDate dataRevisao;
    
    @Column(nullable=false)
    private Boolean concluida;

    @ManyToOne
    @JoinColumn(name = "estudo_id", nullable = false)
    @JsonBackReference("estudo_revisoes")
    private Estudo estudo;

    public Long getEstudoId(){
        return this.estudo.getId();
    }
}
