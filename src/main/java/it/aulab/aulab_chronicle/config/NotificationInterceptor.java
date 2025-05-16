package it.aulab.aulab_chronicle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class NotificationInterceptor implements HandlerInterceptor {

    @Autowired
    CareerRequestRepository careerRequestRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Override
    // Questo metodo viene eseguito dopo che un handler del controller ha gestito la richiesta, ma prima che la vista venga renderizzata
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        // Controlla se modelAndView non è nullo e se l'utente ha il ruolo ROLE_ADMIN
        // NOTA: userInRole("ROLE_ADMIN") è un metodo che non fa parte di HttpServletRequest.userInRole standard. Questo suggerisce l'uso di Spring Security. Assumiamo un modo per controllare il ruolo dell'utente loggato.
        if (modelAndView != null && request.isUserInRole("ROLE_ADMIN")) {

            // Recupera il numero di richieste non controllate
            int careerCount = careerRequestRepository.findByIsCheckedFalseAndIsRejectedFalse().size();

            // Aggiunge il conteggio al modello con il nome "careerRequests"
            modelAndView.addObject("careerRequests", careerCount);
        }

        // Controlla se modelAndView non è nullo e se l'utente ha il ruolo ROLE_REVISOR
        if(modelAndView != null && request.isUserInRole("ROLE_REVISOR")) {
            int revisedCount = articleRepository.findByIsAcceptedIsNull().size();
            modelAndView.addObject("articlesToBeRevised", revisedCount);
        }
    }

} 