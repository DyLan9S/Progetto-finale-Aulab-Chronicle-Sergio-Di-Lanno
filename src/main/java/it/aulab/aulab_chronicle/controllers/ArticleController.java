package it.aulab.aulab_chronicle.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CrudService;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


@Controller
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    @Qualifier("categoryService")
    private CrudService<CategoryDto, Category, Long> categoryService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Rotta index degli articoli
    @GetMapping
    public String articlesIndex(Model viewModel) {
        viewModel.addAttribute("title", "All Articles");

        // Recupera soltanto gli articoli accettati
        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }

        // Ordina gli articoli per data di pubblicazione decrescente
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        // Aggiunge la lista ordinata di articoli al modello
        viewModel.addAttribute("articles", articles);

        return "article/articles";
    }

    //Rotta per la creazione di un articolo
    @GetMapping("/create")
    public String articleCreate(Model viewModel) { // Metodo handler per mostrare il form di creazione
        viewModel.addAttribute("title", "Create Article");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll()); // Aggiunge tutte le categorie al modello
        return "article/create";
    }

    @PostMapping
    public String articleStore(@Valid @ModelAttribute("article") Article article, // Riceve i dati del form e li lega all'oggetto Article
                               BindingResult result, // Contiene il risultato della validazione
                               RedirectAttributes redirectAttributes, // Per aggiungere attributi flash per redirect
                               Principal principal, // Per accedere all'utente autenticato
                               MultipartFile file, // Per ricevere il file caricato
                               Model viewModel) { // Modello per la vista (usato in caso di errori)

        //Controllo degli errori con validazioni (come da screenshot attuale)
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Create Article"); // Aggiunge attributi per ri-mostrare il form con errori
            viewModel.addAttribute("article", article); // Ri-aggiunge l'oggetto Article (con errori)
            viewModel.addAttribute("categories", categoryService.readAll()); // Ri-aggiunge le categorie
            return "article/create"; // Ritorna al template di creazione
        }

        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Article added successfully");

        return "redirect:/";
    }

    // Rotta di dettaglio articolo
    @GetMapping("detail/{id}")
    public String detailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "article/detail";
    }

    // Rotta di modifica di un articolo
    @GetMapping("edit/{id}")
    public String editArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article update");
        viewModel.addAttribute("article", articleService.read(id));
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    // Rotta di memorizzazione delle modifiche di un articolo
    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("article") Article article,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Principal principal,
                                MultipartFile file,
                                Model viewModel) {

        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Article update");
            article.setImage(articleService.read(id).getImage());
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }

        articleService.update(id, article, file);
        redirectAttributes.addFlashAttribute("successMessage", "Article updated successfully");

        return "redirect:/articles";
    }

    // Rotta per la cancellazione di un articolo
    @GetMapping("/delete/{id}")
    public String articleDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Article deleted successfully");

        return "redirect:/writer/dashboard";
    }

    // Rotta dettaglio di un articolo per il revisore
    @GetMapping("/revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "revisor/detail";
    }

    // Rotta per le azioni del revisore
    @PostMapping("/accept")
    public String articleSetAccepted(@RequestParam("action") String action, @RequestParam("articleId") Long articleId, RedirectAttributes redirectAttributes) {
        
        if(action.equals("accept")) {
            articleService.setIsAccepted(true, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Article accepted successfully");
        } else if (action.equals("reject")) {
            articleService.setIsAccepted(false, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Article rejected");
        } else {
            redirectAttributes.addFlashAttribute("resultMessage", "Invalid action");
        }
        
        return "redirect:/revisor/dashboard";
    }

    // Rotta di ricerca di un articolo
    @GetMapping("/search")
    public String articleSearch(@RequestParam("keyword") String keyword, Model viewModel) {
        viewModel.addAttribute("title", "All articles found");

        List<ArticleDto> articles = articleService.search(keyword);

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }
    
}
