package disciplina.lip.AuxiliarRotinaEstudo.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import disciplina.lip.AuxiliarRotinaEstudo.dto.LoginUsuarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.RegistroUsuarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.UsuarioResponseDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.model.enums.RoleUsuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioAuthService {

    private final BCryptPasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    private final AuthenticationManager authManager;


    public Usuario logarUsuario(LoginUsuarioDTO dto){
        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                dto.email(), 
                dto.senha())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        return (Usuario) authentication.getPrincipal();
        // return usuarioRepository.findByEmailWithRelations(dto.email())
        //     .orElseThrow();
    }

    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setRole(RoleUsuario.ALUNO);
        // usuario.setRole(dto.role() != null ? dto.role() : RoleUsuario.ALUNO);

        usuario = usuarioRepository.save(usuario);
        return usuario;
    }

    // para cadastro
    public UsuarioResponseDTO usuarioToDTO(Usuario usuario){
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getRole().name()
        );
    }
}