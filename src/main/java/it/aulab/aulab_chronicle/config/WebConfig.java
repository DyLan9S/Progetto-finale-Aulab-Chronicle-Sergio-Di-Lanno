package it.aulab.aulab_chronicle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private NotificationInterceptor notificationInterceptor;

    @Override
    // Questo metodo, implementato dall'interfaccia WebMvcConfigurer, viene chiamato da Spring per permettere di registrare gli interceptor.
    public void addInterceptors(InterceptorRegistry registry) {
        // Registra il nostro intercettore personalizzato, facendolo partecipare al ciclo di gestione delle richieste.
        registry.addInterceptor(notificationInterceptor);
    }
}