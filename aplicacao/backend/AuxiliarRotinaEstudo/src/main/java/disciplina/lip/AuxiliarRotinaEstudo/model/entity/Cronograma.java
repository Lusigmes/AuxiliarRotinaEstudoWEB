package disciplina.lip.AuxiliarRotinaEstudo.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cronograma")
@Entity
public class Cronograma {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonManagedReference("cronograma_itens")
    @OneToMany(mappedBy = "cronograma", cascade = CascadeType.ALL, orphanRemoval = true) //, fetch=FetchType.LAZY
    private List<ItemCronogramaDiario> itemDoDia;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable=false)
    @JsonBackReference("usuario_cronograma")
    private Usuario usuario;

    public Long getUsuarioId(){
        return this.usuario.getId();
    }

    public long getUsuarioIdPrimitivo(){ // alternativa para if em adicionarNovoItemCronograma
        return this.usuario.getId();
    }
}
