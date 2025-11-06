package disciplina.lip.AuxiliarRotinaEstudo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import disciplina.lip.AuxiliarRotinaEstudo.dto.AdicionarItensCronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.CronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.CronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Cronograma;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.CronogramaRepository;
import disciplina.lip.AuxiliarRotinaEstudo.service.CronogramaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaDTO;

@RestController
@RequestMapping("/cronogramas")
public class CronogramaController {

    @Autowired
    private CronogramaService cronogramaService;
    @Autowired
    private CronogramaRepository cronogramaRepository;

    private static final Logger logger = LoggerFactory.getLogger(CronogramaController.class);

    @CrossOrigin
    @PostMapping
    public ResponseEntity<CronogramaResponseDTO> salvar(@AuthenticationPrincipal Usuario usuario, @RequestBody CronogramaDTO dto ){
        Cronograma cronogramaSalvo = cronogramaService.salvar(dto, usuario);
        return ResponseEntity.ok(cronogramaService.cronogramaToDTO(cronogramaSalvo));
    }

    @CrossOrigin
    @PostMapping("/{idCronograma}/adicionarItens")
    public ResponseEntity<CronogramaResponseDTO> adicionarNovosItensNoCronograma(
            @PathVariable Long idCronograma, 
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody AdicionarItensCronogramaDTO novos_itens_cronograma ){
        Cronograma cronogramaSalvo = cronogramaService.adicionarItensNoCronograma(idCronograma, novos_itens_cronograma.itensDoDia(), usuario);
        return ResponseEntity.ok(cronogramaService.cronogramaToDTO(cronogramaSalvo));
    }

    @CrossOrigin
    @PutMapping("/{idCronograma}")
    public ResponseEntity<CronogramaResponseDTO> atualizar(
        @PathVariable Long idCronograma,
        @AuthenticationPrincipal Usuario usuario,
        @RequestBody CronogramaDTO dto
    ){
        Cronograma cronogramaAtualizado = cronogramaService.atualizar(dto, usuario);
        return ResponseEntity.ok(cronogramaService.cronogramaToDTO(cronogramaAtualizado));
    }

    @CrossOrigin
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existeCronogramaDoUsuario(@AuthenticationPrincipal Usuario usuario){
        Boolean existe = cronogramaService.usuarioTemCronograma(usuario);
        return ResponseEntity.ok(existe);
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<CronogramaResponseDTO> visulizarCronogramaDoUsuario(@AuthenticationPrincipal Usuario usuario){
        Cronograma cronograma = cronogramaService.buscarCronogramaDoUsuario(usuario);
        return ResponseEntity.ok(cronogramaService.cronogramaToDTO(cronograma));
    }

    @CrossOrigin
    @DeleteMapping("/{idCronograma}")
    public ResponseEntity<Cronograma> deletar(@PathVariable Long idCronograma){
        cronogramaRepository.deleteById(idCronograma);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
