package disciplina.lip.AuxiliarRotinaEstudo.dto;

public record AuthResponseDTO(
    String token,
    String refreshToken,
    String message
) {
    public AuthResponseDTO(String token, String refreshToken) {
        this(token, refreshToken, "Operação realizada com sucesso");
    }
}