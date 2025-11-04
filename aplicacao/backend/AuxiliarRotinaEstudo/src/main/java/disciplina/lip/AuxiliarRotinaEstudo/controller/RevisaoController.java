package disciplina.lip.AuxiliarRotinaEstudo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import disciplina.lip.AuxiliarRotinaEstudo.dto.RevisaoResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Revisao;
import disciplina.lip.AuxiliarRotinaEstudo.service.RevisaoService;


@RestController
@RequestMapping("/revisoes")
public class RevisaoController {
    
    @Autowired
    private RevisaoService revisaoService;

    @CrossOrigin
    @PutMapping("/{idRevisao}")
    public ResponseEntity<RevisaoResponseDTO> concluirRevisao(@PathVariable long idRevisao) {     
        Revisao revisao = revisaoService.concluirStatausRevisao(idRevisao);
        return ResponseEntity.ok(revisaoService.revisaoToDTO(revisao));
    }
    
}
