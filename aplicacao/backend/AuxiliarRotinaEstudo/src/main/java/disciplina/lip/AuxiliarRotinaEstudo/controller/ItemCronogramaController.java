package disciplina.lip.AuxiliarRotinaEstudo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import disciplina.lip.AuxiliarRotinaEstudo.service.ItemCronogramaService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.ItemCronogramaDiarioRepository;


@RestController
@RequestMapping("itens")
public class ItemCronogramaController {
    
    @Autowired
    private ItemCronogramaService itemCronogramaService;
    
    @Autowired
    private ItemCronogramaDiarioRepository itemCronogramaRepository;

    @PutMapping("/{idItem}")
    public ResponseEntity<ItemCronogramaResponseDTO> atualizar(
        @PathVariable Long idItem,
        @RequestBody ItemCronogramaDTO dto,
        @AuthenticationPrincipal Usuario usuario) {
        ItemCronogramaDiario item = itemCronogramaService.atualizar(idItem, usuario, dto);
        return ResponseEntity.ok(itemCronogramaService.itemToDTO(item));
    }

    @CrossOrigin
    @DeleteMapping("/{idItem}")
    public ResponseEntity<ItemCronogramaDiario> deletar(@PathVariable Long idItem){
        itemCronogramaRepository.deleteById(idItem);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
