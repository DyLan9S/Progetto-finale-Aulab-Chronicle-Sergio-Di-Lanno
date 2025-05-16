package it.aulab.aulab_chronicle.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ExceptionHandlingController {
    
    // Rotta per la gestione e cattura di errori
    @GetMapping("/error/{number}") // Mappa le richieste GET a /error/ seguito da un numero (PathVariable)
    public String accessDenied(@PathVariable int number, Model model) { // Riceve il numero dall'URI e un oggetto Model
    // Controlla se il numero dell'errore è 403 
        if (number == 403) {
            // Se l'errore è 403, reindirizza alla root con un parametro 'notAuthorized'
            return "redirect:/?notAuthorized";
        }
        // Per tutti gli altri errori catturati da questa rotta, reindirizza alla home senza parametro 'notAuthorized'
        return "redirect:/";
    }
}
