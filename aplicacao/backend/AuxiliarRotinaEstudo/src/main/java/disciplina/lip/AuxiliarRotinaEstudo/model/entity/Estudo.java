package disciplina.lip.AuxiliarRotinaEstudo.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "estudo")
public class Estudo {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String nomeDisciplina;
    
    @Column(nullable=false)
    private String tema;
    
    @Column(nullable=false)
    private int tempoDeEstudo;
    
    @Column(nullable=false)
    private LocalDate diaDoEstudo;
    
    @OneToMany(mappedBy = "estudo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("estudo_revisoes")
    private List<Revisao> revisoes = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable=false)
    @JsonBackReference("usuario_estudos")
    private Usuario usuario;


    public Long getUsuarioId(){
        return this.usuario.getId();
    }
}
