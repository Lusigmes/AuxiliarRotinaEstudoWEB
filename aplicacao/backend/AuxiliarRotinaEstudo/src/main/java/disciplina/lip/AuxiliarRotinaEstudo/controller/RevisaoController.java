package disciplina.lip.AuxiliarRotinaEstudo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/pendentes/page")
    public ResponseEntity<Page<RevisaoResponseDTO>> visualizarPendentesPaginado(@AuthenticationPrincipal Usuario usuario,Pageable pageable) {
        return ResponseEntity.ok(revisaoService.listarRevisoesPendentesPaginado(usuario, pageable));
    }
    
    @CrossOrigin
    @GetMapping("/pendentes/contar")
    public ResponseEntity<Long> contarPendentes(@AuthenticationPrincipal Usuario usuario){
        long count = revisaoService.contarRevisoesPendentes(usuario);
        return ResponseEntity.ok(count);
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
    @GetMapping("/atrasadas/page")
    public ResponseEntity<Page<RevisaoResponseDTO>> visualizarAtrasadasPaginado(@AuthenticationPrincipal Usuario usuario, Pageable pageable) {
        return ResponseEntity.ok(revisaoService.listarRevisoesAtrasadasPaginado(usuario, pageable));
    }

    @CrossOrigin
    @GetMapping("/atrasadas/contar")
    public ResponseEntity<Long> contarAtrasadas(@AuthenticationPrincipal Usuario usuario){
        long count = revisaoService.contarRevisoesAtrasadas(usuario);
        return ResponseEntity.ok(count);
    }

    @CrossOrigin
    @DeleteMapping("/{idRevisao}")
    public ResponseEntity<Revisao> deletar(@PathVariable Long idRevisao){
        revisaoRepository.deleteById(idRevisao);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }    

    @CrossOrigin
    @GetMapping("/concluidas")
    public ResponseEntity<List<RevisaoResponseDTO>> visualizarConcluidas(@AuthenticationPrincipal Usuario usuario) {
        List<Revisao> revisoesConcluidas = revisaoService.listarRevisoesConcluidas(usuario);
        List<RevisaoResponseDTO> responseDTOs = revisoesConcluidas.stream()
            .map(revisao -> revisaoService.revisaoToDTO(revisao))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDTOs);
    }

    @CrossOrigin
    @GetMapping("/concluidas/page")
    public ResponseEntity<Page<RevisaoResponseDTO>> visualizarConcluidasPaginado(
        @AuthenticationPrincipal Usuario usuario, 
        Pageable pageable
    ) {
        return ResponseEntity.ok(revisaoService.listarRevisoesConcluidasPaginado(usuario, pageable));
    }

    @CrossOrigin
    @GetMapping("/concluidas/contar")
    public ResponseEntity<Long> contarConcluidas(@AuthenticationPrincipal Usuario usuario) {
        long count = revisaoService.contarRevisoesConcluidas(usuario);
        return ResponseEntity.ok(count);
    }
}
