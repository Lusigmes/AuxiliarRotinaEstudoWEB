package disciplina.lip.AuxiliarRotinaEstudo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginTokenResponseDTO {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    
    public LoginTokenResponseDTO() {}
    
    public LoginTokenResponseDTO(String token, String refreshToken, Long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}