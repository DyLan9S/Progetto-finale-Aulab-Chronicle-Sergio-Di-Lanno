package it.aulab.aulab_chronicle.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.UserDto;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUser(UserDto userDto, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        User user = new User();
        user.setUsername(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Assegna un ruolo di default ("ROLE_USER") all'utente
        Role role = roleRepository.findByName("ROLE_USER");
        user.setRoles(List.of(role));

        userRepository.save(user);

        // Chiama il metodo sotto per autenticare l'utente e impostare la sessione
        authenticateUserAndSetSession(user, userDto, request);
    }

    // Metodo per autenticare l'utente e impostare la sessione
    public void authenticateUserAndSetSession(User user, UserDto userDto, HttpServletRequest request) {

        try {
            // Carica i dettagli utente usando il CustomUserDetailsService
            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user.getEmail());

            // Crea un token di autenticazione - Utilizza l'username da userDetails (l'email) e la password dal DTO
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDto.getPassword());

            // Autentica il token usando l'AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Imposta l'oggetto Authentication nel SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Ottiene o crea una sessione HTTP
            HttpSession session = request.getSession(true);
            // Imposta esplicitamente il SecurityContext nella sessione HTTP
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        } catch (AuthenticationException e) {
            // Stampa lo stack trace in caso di errore di autenticazione
            e.printStackTrace();
        }
    }

    @Override
    public User find(Long id) {
        return userRepository.findById(id).get();
    }
}
