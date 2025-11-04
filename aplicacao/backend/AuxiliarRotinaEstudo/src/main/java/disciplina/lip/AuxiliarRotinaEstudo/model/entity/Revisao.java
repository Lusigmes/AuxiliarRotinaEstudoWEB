package disciplina.lip.AuxiliarRotinaEstudo.model.entity;

import java.time.LocalDate;

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
    private long id;
    
    @Column(nullable=false)
    private LocalDate dataRevisao;
    
    @Column(nullable=false)
    private Boolean concluida;

    @ManyToOne
    @JoinColumn(name = "estudo_id", nullable = false)
    private Estudo estudo;

    public long getEstudoId(){
        return this.estudo.getId();
    }
}
