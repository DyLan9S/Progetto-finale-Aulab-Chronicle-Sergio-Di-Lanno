package it.aulab.aulab_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.aulab_chronicle.models.CareerRequest;

public interface CareerRequestRepository extends CrudRepository<CareerRequest, Long> {
    
    // Trova richieste dove isChecked è falso
    List<CareerRequest> findByIsCheckedFalse();

    // Trova richieste dove isChecked e isRejected sono false per il conteggio della campanellina
    List<CareerRequest> findByIsCheckedFalseAndIsRejectedFalse();

    @Query(value = "SELECT user_id FROM users_roles", nativeQuery = true)
    List<Long> findAllUserIds();

    @Query(value = "SELECT role_id FROM users_roles WHERE user_id = :id", nativeQuery = true)
    List<Long> findByUserId(@Param("id") Long id);
}
