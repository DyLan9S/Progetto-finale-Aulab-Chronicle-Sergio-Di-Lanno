package it.aulab.aulab_chronicle.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;

@Service
public class CareerRequestServiceImpl implements CareerRequestService {

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest) {
        // Recupera tutti gli ID utente che hanno fatto richieste
        List<Long> allUserIds = careerRequestRepository.findAllUserIds();

        // Controlla se l'ID dell'utente corrente è nella lista degli utenti che hanno già fatto richieste
        if (!allUserIds.contains(user.getId())) {
            return false;
        }

        // Recupera le richieste già inviate dall'utente corrente
        List<Long> requests = careerRequestRepository.findByUserId(user.getId());

        // Controlla se tra le richieste dell'utente ne esiste già una per il ruolo richiesto
        return requests.stream().anyMatch(roleId -> roleId.equals(careerRequest.getRole().getId()));
    }

    public void save(CareerRequest careerRequest, User user) {
        // Associa l'utente (autore della richiesta) alla richiesta
        careerRequest.setUser(user);

        // Imposta il flag isChecked a false per la nuova richiesta
        careerRequest.setIsChecked(false);

        // Imposta il flag isRejected a false per la nuova richiesta (servirà per il controllo dell'admin sulle richieste)
        careerRequest.setIsRejected(false);

        careerRequestRepository.save(careerRequest);

        emailService.sendSimpleEmail("aulabchronicle@administration.com", "Request for role: " + careerRequest.getRole().getName(), "There is a new request for collaboration from " + user.getUsername() + ".");
    }

    @Override
    public void careerAccept(Long requestId) {
        // Recupera la richiesta tramite ID
        CareerRequest request = careerRequestRepository.findById(requestId).get();

        // Estrae l'utente e il ruolo dalla richiesta
        User user = request.getUser(); 
        Role role = request.getRole();

        // Recupera i ruoli attuali dell'utente e aggiunge quello nuovo
        List<Role> rolesUser = user.getRoles();
        Role newRole = roleRepository.findByName(role.getName());
        rolesUser.add(newRole);

        // Salva le modifiche ai ruoli dell'utente
        user.setRoles(rolesUser);
        userRepository.save(user);
        // Marca la richiesta come controllata (accettata)
        request.setIsChecked(true);

        careerRequestRepository.save(request);

        // Invia email all'utente: Destinatario: email dell'utente, Oggetto dell'email, Corpo dell'email
        emailService.sendSimpleEmail( user.getEmail(), "Role enabled",   "Hello, your collaboration request has been accepted by our administration");
    }

    @Override
    public void careerReject(Long requestId) {
        // Recupera la richiesta tramite ID
        CareerRequest request = careerRequestRepository.findById(requestId).get();
        // Imposta il campo isRejected a true per marcare la richiesta come rifiutata
        request.setIsRejected(true);
        // Imposta il campo isChecked a true per marcare la richiesta come processata, anche se è stata rifiutata, è stata controllata dall'admin.
        request.setIsChecked(true);

        careerRequestRepository.save(request);

        // Invia email all'utente per informarlo del rifiuto
        User user = request.getUser();
        emailService.sendSimpleEmail( user.getEmail(), "Request rejected",   "Hello, we regret to inform you that your request for collaboration has been rejected by our administration.");
    }

    @Override
    public CareerRequest find(Long id) {
        return careerRequestRepository.findById(id).get();
    }

}