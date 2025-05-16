package it.aulab.aulab_chronicle;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true) // Questa annotazione dice a Spring di cercare metodi annotati con @Async e di eseguirli in un pool di thread separato.
@EnableTransactionManagement //  Questa annotazione abilita il supporto alla gestione dichiarativa delle transazioni tramite @Transactional. Spring gestirà automaticamente l'inizio e il commit/rollback delle transazioni per i metodi annotati con @Transactional.
public class AulabChronicleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AulabChronicleApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
    public ModelMapper instanceModelMapper() {
        ModelMapper mapper = new ModelMapper();
        return mapper;
    }
}
