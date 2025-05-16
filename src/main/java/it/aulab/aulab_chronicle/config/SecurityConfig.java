package it.aulab.aulab_chronicle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import it.aulab.aulab_chronicle.services.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Configura l'autorizzazione delle richieste HTTP
            .authorizeHttpRequests(authorize ->
                // Permette l'accesso a questi percorsi a tutti (anche non autenticati)
                authorize.requestMatchers("/register/**").permitAll()
                .requestMatchers("/admin/dashboard", "/categories/create", "/categories/edit/{id}", "/categories/update/{id}", "/categories/delete/{id}").hasRole("ADMIN")
                .requestMatchers("/revisor/dashboard", "/revisor/detail/{id}", "/accept").hasRole("REVISOR")
                .requestMatchers("/writer/dashboard", "/articles/create", "/articles/edit/{id}", "/articles/update/{id}", "/articles/delete/{id}").hasRole("WRITER")
                .requestMatchers("/register", "/", "/articles", "/images/**", "/articles/detail/**", "/categories/search/{id}", "/search/{id}", "/articles/search").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Tutti gli altri percorsi richiedono autenticazione
                .anyRequest().authenticated()
            )
            // Configura la gestione del form di login
            .formLogin(form ->
                form.loginPage("/login")
                // Specifica l'URL a cui Spring Security invierà i dati del form (POST /login)
                .loginProcessingUrl("/login")
                // Specifica l'URL di reindirizzamento dopo un login riuscito
                .defaultSuccessUrl("/")
                // Permette a tutti di accedere alla pagina di login (GET /login)
                .permitAll()
            )
            // Configura la gestione del logout
            .logout(logout ->
                // Specifica l'URL per il logout
                logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                // Permette l'accesso all'URL di logout
                .permitAll()
            )
            // Specifica l'URL a cui reindirizzare in caso di accesso negato (errore 403)
            .exceptionHandling(exception -> exception.accessDeniedPage("/error/403"))
            // Configurazione sessionManagement
            .sessionManagement(session -> 
                // Imposta la strategia di creazione della sessione, se necessaria
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // Imposta il numero massimo di sessioni per utente
                .maximumSessions(1)
                // Questa opzione specifica l'URL a cui l'utente verrà reindirizzato se si superano le sessioni massime
                .expiredUrl("/login?session-expired=true")
            );

        // Costruisce e ritorna la catena di filtri di sicurezza
        return http.build();
    }

    // Metodo per configurare l'Authentication Manager Globale
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            // Specifica il nostro servizio UserDetailsService personalizzato
            .userDetailsService(customUserDetailsService)
            // Specifica il PasswordEncoder da utilizzare per verificare le password
            .passwordEncoder(passwordEncoder);
    }

    // Per l'AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
    throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
