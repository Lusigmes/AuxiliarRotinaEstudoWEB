package disciplina.lip.AuxiliarRotinaEstudo.dto;

import disciplina.lip.AuxiliarRotinaEstudo.model.enums.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioDTO(
    @NotBlank(message = "O nome é obrigatório")
    String nome,

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 4, max = 10, message = "Entre 4 e 10 caracteres")
    String senha,

    RoleUsuario role
    ) {}