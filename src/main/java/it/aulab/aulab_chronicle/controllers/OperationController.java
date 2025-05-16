package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;
import it.aulab.aulab_chronicle.services.CareerRequestService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/operations")
public class OperationController {
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CareerRequestService careerRequestService;

    // Rotta per la creazione di una richiesta di collaborazione
    @GetMapping("/career/request")
    public String careerRequestCreatq(Model viewModel) {
        // Imposta il titolo della pagina
        viewModel.addAttribute("title", "Insert your request");
        // Crea un nuovo oggetto CareerRequest vuoto per il form
        viewModel.addAttribute("careerRequest", new CareerRequest());

        // Recupera tutti i ruoli disponibili dal repository
        List<Role> roles = roleRepository.findAll();
        // Elimina la possibilità di scegliere il ruolo user nella select del form
        roles.removeIf(e -> e.getName().equals("ROLE_USER"));
        // Aggiunge la lista filtrata di ruoli al modello
        viewModel.addAttribute("roles", roles);

        return "career/requestForm";
    }
    
    // Rotta per il salvataggio di una richiesta di ruolo
    @PostMapping("/career/request/save")
    public String careerRequestStore(@ModelAttribute("careerRequest") CareerRequest careerRequest, Principal principal, RedirectAttributes redirectAttributes) {

        // Recupera l'utente loggato tramite Principal
        User user = userRepository.findByEmail(principal.getName());

        // Controlla se l'utente ha già il ruolo richiesto o ha già inviato la richiesta per quel ruolo
        if (careerRequestService.isRoleAlreadyAssigned(user, careerRequest)) {
            // Se il ruolo è già assegnato o la richiesta già inviata, aggiunge un messaggio di errore flash e reindirizza
            redirectAttributes.addFlashAttribute("errorMessage", "You are already assigned to this role");
            return "redirect:/";
        }

        // Se il controllo passa, salva la richiesta di collaborazione
        careerRequestService.save(careerRequest, user);

        // Aggiunge un messaggio di successo flash e reindirizza
        redirectAttributes.addFlashAttribute("successMessage", "Request sent successfully");

        return "redirect:/";
    }

    // Rotta per il dettaglio di una richiesta
    @GetMapping("/career/request/detail/{id}")
    public String careerRequestDetail(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Request detail");
        viewModel.addAttribute("request", careerRequestService.find(id));
        return "career/requestDetail";
    }
    

    // Rotta per l'accettazione di una richiesta
    @PostMapping("/career/request/accept/{requestId}")
    public String careerRequestAccept(@PathVariable("requestId") Long requestId, RedirectAttributes redirectAttributes) {

        careerRequestService.careerAccept(requestId);
        redirectAttributes.addFlashAttribute("successMessage", "Role enabled for user");

        return "redirect:/admin/dashboard";
    }

    // Rotta per il rifiuto di una richiesta
    @PostMapping("/career/request/reject/{requestId}")
    public String careerRequestReject(@PathVariable("requestId") Long requestId, RedirectAttributes redirectAttributes) {

        careerRequestService.careerReject(requestId);
        redirectAttributes.addFlashAttribute("successMessage", "Request successfully rejected");

        return "redirect:/admin/dashboard";
    }
}
