package disciplina.lip.AuxiliarRotinaEstudo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import disciplina.lip.AuxiliarRotinaEstudo.service.ItemCronogramaService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.ItemCronogramaResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.ItemCronogramaDiario;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;


@RestController
@RequestMapping("itens")
public class ItemCronogramaController {
    
    @Autowired
    private ItemCronogramaService itemCronogramaService;

    @PutMapping("/{idItem}")
    public ResponseEntity<ItemCronogramaResponseDTO> aatualizar(@PathVariable Long idItem, @RequestBody ItemCronogramaDTO dto, @AuthenticationPrincipal Usuario usuario) {
        ItemCronogramaDiario item = itemCronogramaService.atualizar(idItem, usuario, dto);
        return ResponseEntity.ok(itemCronogramaService.itemToDTO(item));
    }

}
