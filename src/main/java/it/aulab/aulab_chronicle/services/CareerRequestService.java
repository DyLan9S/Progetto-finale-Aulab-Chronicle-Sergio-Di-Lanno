package it.aulab.aulab_chronicle.services;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.User;

public interface CareerRequestService {

    // Metodo per verificare se il ruolo è già assegnato o la richiesta è già stata fatta
    boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest);

    // Metodo per salvare la richiesta di collaborazione
    void save(CareerRequest careerRequest, User user);

    // Metodo per accettare una richiesta di collaborazione
    void careerAccept(Long requestId);

    // Metodo per rifiutare una richiesta di collaborazione
    void careerReject(Long requestId);

    // Metodo per trovare una richiesta per ID
    CareerRequest find(Long id);

}