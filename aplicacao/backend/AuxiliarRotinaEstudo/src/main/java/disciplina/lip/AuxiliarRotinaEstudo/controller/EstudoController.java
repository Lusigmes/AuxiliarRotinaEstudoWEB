package disciplina.lip.AuxiliarRotinaEstudo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import disciplina.lip.AuxiliarRotinaEstudo.service.EstudoService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import disciplina.lip.AuxiliarRotinaEstudo.dto.EstudoResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.EstudoDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.EstudoUpdateDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Estudo;


@RestController
@RequestMapping("/estudos")
public class EstudoController {
    
    @Autowired
    private EstudoService estudoService;

    @CrossOrigin
    @PostMapping
    public ResponseEntity<EstudoResponseDTO> salvar(
        @RequestBody EstudoDTO dto,
        @AuthenticationPrincipal Usuario usuario) {
        Estudo estudoSalvo = estudoService.salvar(dto, usuario);
        return ResponseEntity.ok(estudoService.estudoToDTO(estudoSalvo));
    }
    @CrossOrigin
    @GetMapping
    public ResponseEntity<List<EstudoResponseDTO>> listarEstudosDoUsuario(
        @AuthenticationPrincipal Usuario usuario) {
            List<EstudoResponseDTO> estudo = estudoService.listarEstudosDoUsuario(usuario);
            return ResponseEntity.ok(estudo);
        }
        
    @CrossOrigin
    @PutMapping("/{idEstudo}")
    public ResponseEntity<EstudoResponseDTO> atualizar(
        @PathVariable long idEstudo,
        @RequestBody EstudoUpdateDTO dto
    ){
        Estudo esudoAtualizado = estudoService.atualizar(dto, idEstudo);
        return ResponseEntity.ok(estudoService.estudoToDTO(esudoAtualizado));
    }
    
}
