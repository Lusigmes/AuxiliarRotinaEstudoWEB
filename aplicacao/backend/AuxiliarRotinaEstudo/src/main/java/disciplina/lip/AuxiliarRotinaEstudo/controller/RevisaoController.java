package disciplina.lip.AuxiliarRotinaEstudo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import disciplina.lip.AuxiliarRotinaEstudo.dto.RevisaoResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.RevisaoUpdateDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Revisao;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.RevisaoRepository;
import disciplina.lip.AuxiliarRotinaEstudo.service.RevisaoService;


@RestController
@RequestMapping("/revisoes")
public class RevisaoController {
    
    @Autowired
    private RevisaoService revisaoService;
    
    @Autowired
    private RevisaoRepository revisaoRepository;

    @CrossOrigin
    @PutMapping("/{idRevisao}/concluir")
    public ResponseEntity<RevisaoResponseDTO> concluirRevisao(@PathVariable Long idRevisao) {     
        Revisao revisao = revisaoService.concluirStatausRevisao(idRevisao);
        return ResponseEntity.ok(revisaoService.revisaoToDTO(revisao));
    }
    
    @CrossOrigin
    @PutMapping("/{idRevisao}/nova_data")
    public ResponseEntity<RevisaoResponseDTO> alterarDataRevisao(
        @PathVariable Long idRevisao,
        @RequestBody RevisaoUpdateDTO data) {     
        Revisao revisao = revisaoService.alterarDataRevisao(idRevisao, data);
        return ResponseEntity.ok(revisaoService.revisaoToDTO(revisao));
    }
    
    @CrossOrigin
    @GetMapping
    public ResponseEntity<List<RevisaoResponseDTO>> visualizarRevisoes(@AuthenticationPrincipal Usuario usuario){
        List<Revisao> revisoes = revisaoService.listarRevisoes(usuario);
        List<RevisaoResponseDTO> responseDTOs = revisoes.stream()
            .map(revisao -> revisaoService.revisaoToDTO(revisao))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDTOs);
    }
    
    @CrossOrigin
    @GetMapping("/pendentes")
    public ResponseEntity<List<RevisaoResponseDTO>> visualizarPendentes(@AuthenticationPrincipal Usuario usuario){
        List<Revisao> revisoesPendentes = revisaoService.listarRevisoesPendentes(usuario);
        List<RevisaoResponseDTO> responseDTOs = revisoesPendentes.stream()
            .map(revisao -> revisaoService.revisaoToDTO(revisao))
            .collect(Collectors.toList());
       
        return ResponseEntity.ok(responseDTOs);
    }
    
    @CrossOrigin
    @GetMapping("/atrasadas")
    public ResponseEntity<List<RevisaoResponseDTO>> visualizarAtrasadas(@AuthenticationPrincipal Usuario usuario){
        List<Revisao> revisoesAtrasadas = revisaoService.listarRevisoesAtrasadas(usuario);
        List<RevisaoResponseDTO> responseDTOs = revisoesAtrasadas.stream()
            .map(revisao -> revisaoService.revisaoToDTO(revisao))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDTOs);
    }

    @CrossOrigin
    @DeleteMapping("/{idRevisao}")
    public ResponseEntity<Revisao> deletar(@PathVariable Long idRevisao){
        revisaoRepository.deleteById(idRevisao);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
