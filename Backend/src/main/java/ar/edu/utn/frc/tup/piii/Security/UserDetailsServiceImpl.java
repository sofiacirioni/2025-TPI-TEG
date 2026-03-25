package ar.edu.utn.frc.tup.piii.Security;

import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return usuarioRepository.findByCorreo(correo)
                .map(user -> User.builder()
                        .username(user.getCorreo())
                        .password(user.getContrasenia())
                        .authorities("ROLE_USER")
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + correo
                ));
    }
}
