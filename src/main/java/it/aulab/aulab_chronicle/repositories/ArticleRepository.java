package it.aulab.aulab_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.User;


public interface ArticleRepository extends ListCrudRepository<Article, Long> {

    List<Article> findByCategory(Category category); // Metodo per trovare articoli per categoria

    List<Article> findByUser(User user); // Metodo per trovare articoli per utente

    List<Article> findByIsAcceptedTrue(); // Metodo per trovare articoli accettati

    List<Article> findByIsAcceptedFalse(); // Metodo per trovare articoli rifiutati

    List<Article> findByIsAcceptedIsNull(); // Metodo per trovare articoli in attesa di accettazione

    // Query JPQL per la ricerca full-text
    @Query("SELECT a FROM Article a WHERE " +
        "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.subtitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Article> search(@Param("searchTerm") String searchTerm);

}
