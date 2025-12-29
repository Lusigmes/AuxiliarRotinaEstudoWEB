package disciplina.lip.AuxiliarRotinaEstudo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import disciplina.lip.AuxiliarRotinaEstudo.dto.AuthResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.LoginTokenResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.LoginUsuarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.RefreshTokenRequestDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.RegistroUsuarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.UsuarioResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.UsuarioRepository;
import disciplina.lip.AuxiliarRotinaEstudo.service.JwtAuthService;
import disciplina.lip.AuxiliarRotinaEstudo.service.UsuarioAuthService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class UsuarioAuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private UsuarioAuthService usuarioService;

    @Autowired
    private JwtAuthService jwtService;
    
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioAutenticado(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioAtual = (Usuario) auth.getPrincipal();

        usuarioAtual = usuarioRepository.findByEmailWithRelations(usuarioAtual.getEmail());

        if(usuarioAtual == null){
            throw new RuntimeException("Usuário não encontrado");
        }
      
        return ResponseEntity.ok(usuarioService.usuarioToDTO(usuarioAtual));
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody RegistroUsuarioDTO dto){
        Usuario usuarioRegistro = usuarioService.registrarUsuario(dto);
        return ResponseEntity.ok(usuarioService.usuarioToDTO(usuarioRegistro));
    }
    

    @PostMapping("/login")
    public ResponseEntity<LoginTokenResponseDTO> logar(@RequestBody LoginUsuarioDTO dto){
        try{
            Usuario usuarioAuth = usuarioService.logarUsuario(dto);
            
            String jwtToken = jwtService.generateToken(usuarioAuth);
            String refreshToken = jwtService.generateRefreshToken(usuarioAuth);
            
            LoginTokenResponseDTO loginResponse = new LoginTokenResponseDTO()
                .setToken(jwtToken)
                .setRefreshToken(refreshToken)
                .setExpiresIn(jwtService.getExpirationTime());
            
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest){
        try {
            AuthResponseDTO response = usuarioService.refreshToken(refreshTokenRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponseDTO(null, null, e.getMessage()));
        }
    }
}