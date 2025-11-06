package disciplina.lip.AuxiliarRotinaEstudo.dto;

import lombok.Data;


@Data
public class LoginTokenResponseDTO {
    private String token;
    private Long expiresIn;


    public LoginTokenResponseDTO setToken(String token) {
        this.token = token;
        return this;
    }
    
    public LoginTokenResponseDTO setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }
}