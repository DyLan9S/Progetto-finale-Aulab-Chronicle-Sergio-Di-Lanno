package it.aulab.aulab_chronicle.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ArticleDto> readAll() {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findAll()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @Override
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if (optArticle.isPresent()) {
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article id-" + key + " not found");
        }
    }

    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        String url = "";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId())).get();
            article.setUser(user);
        }

        if (!file.isEmpty()) {
            try {
                CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                url = futureUrl.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        article.setIsAccepted(null);

        ArticleDto dto = modelMapper.map(articleRepository.save(article), ArticleDto.class);

        if (!file.isEmpty()) {
            imageService.saveImageOnDB(url, article);
        }

        return dto;
    }


    //METODO DELLA DOCUMENTAZIONE NON FUNZIONANOTE
    // @Override
    // public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
    //     String url="";

    //     // Controlla l'esistenza dell'articolo in base al suo id
    //     if (articleRepository.existsById(key)) {
    //         // Assegna all'articolo proveniente dal form lo stesso id dell'articolo originale
    //         updatedArticle.setId(key);
    //         // Recuper l'articolo originale non modificato
    //         Article article = articleRepository.findById(key).get();
    //         // Imposta l'utente dell'articolo del form con quello originale
    //         updatedArticle.setUser(article.getUser());

    //         // Controlla la presenza o meno del file del form per capire se modificare o no l'immagine
    //         if(!file.isEmpty()) {
    //             try {
    //                 // Verifica se l'articolo ha un'immagine prima di tentare di eliminarla
    //                 if (article.getImage() != null) {
    //                     // Elimina l'immagine precedente
    //                     imageService.deleteImage(article.getImage().getPath());
    //                 }
    //                 try {
    //                     // Salva la nuova immagine
    //                     CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
    //                     url = futureUrl.get();
    //                 } catch (Exception e) {
    //                     e.printStackTrace();
    //                 }
    //                 // Salva il nuovo path nel DB
    //                 imageService.saveImageOnDB(url, updatedArticle);

    //                 // Essendo l'immagine modificata, l'articolo torna in revisione
    //                 updatedArticle.setIsAccepted(null);
    //                 return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }    
    //         } else if(article.getImage() == null) { //Se l'articolo originale e quello modificato non hanno un immagine
    //             updatedArticle.setIsAccepted(article.getIsAccepted());
    //         } else {
    //             // Se l'immagine non è stata modificata imposta la stessa immagine
    //             updatedArticle.setImage(article.getImage());

    //             // Se l'immagine non è stata cambiata effettua un check su tutti gli altri campi e, se modificati, manda l'articolo in revisione
    //             if(updatedArticle.equals(article) == false) {
    //                 updatedArticle.setIsAccepted(null);
    //             } else {
    //                 updatedArticle.setIsAccepted(article.getIsAccepted());
    //             }

    //             return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
    //         }
    //     } else {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    //     }
    //     return null;
    // }

    @Override
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
        // Controlla l'esistenza dell'articolo in base al suo id
        if (!articleRepository.existsById(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Assegna all'articolo proveniente dal form lo stesso id dell'articolo originale
        updatedArticle.setId(key);
        // Recupera l'articolo originale non modificato
        Article article = articleRepository.findById(key).get();
        // Imposta l'utente dell'articolo del form con quello originale
        updatedArticle.setUser(article.getUser());

        try {
            // Gestione dell'immagine
            if (!file.isEmpty()) {
                // C'è una nuova immagine caricata

                // Elimina l'immagine precedente se esiste
                if (article.getImage() != null) {
                    try {
                        imageService.deleteImage(article.getImage().getPath());
                    } catch (Exception e) {
                        // Logga l'errore ma continua
                        e.printStackTrace();
                    }
                }

                // Salva la nuova immagine
                String url = "";
                try {
                    CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                    url = futureUrl.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading image");
                }

                // Salva il nuovo path nel DB
                imageService.saveImageOnDB(url, updatedArticle);

                // Essendo l'immagine modificata, l'articolo torna in revisione
                updatedArticle.setIsAccepted(null);
            } else {
                // Non c'è una nuova immagine caricata

                // Se l'articolo originale ha un'immagine, la manteniamo
                if (article.getImage() != null) {
                    updatedArticle.setImage(article.getImage());
                }

                // Verifica se ci sono state modifiche che richiedono revisione
                if (!updatedArticle.equals(article)) {
                    updatedArticle.setIsAccepted(null);  // Manda in revisione
                } else {
                    updatedArticle.setIsAccepted(article.getIsAccepted());  // Mantiene lo stato precedente
                }
            }

            // Salva sempre l'articolo alla fine e restituisci il DTO
            return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating article");
        }
    }

    @Override
    public void delete(Long key) {
        if(articleRepository.existsById(key)) {
            Article article = articleRepository.findById(key).get();

            try {
                String path = article.getImage().getPath();
                article.getImage().setPath(null);
                imageService.deleteImage(path);
            } catch (Exception e) {
                e.printStackTrace();
            }

            articleRepository.deleteById(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    // Metodo per cercare articoli per categoria
    public List<ArticleDto> searchByCategory(Category category) {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findByCategory(category)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    // Metodo per cercare articoli per autore
    public List<ArticleDto> searchByAuthor(User user){
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for(Article article: articleRepository.findByUser(user)){
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }

        return dtos;
    }

    // Metodo per impostare lo stato di accettazione di un articolo
    public void setIsAccepted(Boolean result, Long id) {
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    // Metodo search che utilizza la query del repository
    public List<ArticleDto> search(String keyword) {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.search(keyword)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
}
