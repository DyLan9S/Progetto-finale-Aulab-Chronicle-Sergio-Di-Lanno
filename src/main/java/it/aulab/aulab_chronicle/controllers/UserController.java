package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.UserDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CategoryService;
import it.aulab.aulab_chronicle.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@Controller
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper; 

    // Rotta Home
    @GetMapping("/")
    public String home( Model viewModel) {

        // Mostra soltanto gli articoli accettati
        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }
        
        // Ordina gli articoli in modo decrescente per data di pubblicazione
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());

        // Seleziona solo i primi tre articoli dopo l'ordinamento
        List<ArticleDto> lastThreeArticles = articles.stream().limit(3).collect(Collectors.toList());

        // Aggiunge la lista dei tre articoli più recenti al modello
        viewModel.addAttribute("articles", lastThreeArticles);

        return "home";
    }

    // Rotta per la registrazione
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }

    // Rotta per il login
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // Rotta per il salvataggio della registrazione
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                HttpServletResponse response
                                ) {

        // Controlla se esiste già un utente con l'email fornita
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        // Aggiunge un errore al campo email se già esistente
        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null, "There is already an account registered with the same email");
        }

        // Se ci sono errori di validazione
        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "auth/register";
        }

        // Se non ci sono errori, salva l'utente tramite il service
        userService.saveUser(userDto, redirectAttributes, request, response);

        // Aggiunge un attributo flash per mostrare il messaggio di successo dopo la redirect
        redirectAttributes.addFlashAttribute("successMessage", "Registration successful");

        // Reindirizza alla homepage
        return "redirect:/";
    }

    // Rotta per la ricerca degli articoli in base all'utente (Autore)
    @GetMapping("/search/{id}")
    public String userArticlesSearch(@PathVariable("id") Long id, Model viewModel) {
        User user = userService.find(id);
        viewModel.addAttribute("title", "all articles found by user " + user.getUsername());

        List<ArticleDto> articles = articleService.searchByAuthor(user);

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }

    // Rotta per la dashboard dell'admin
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model viewModel) {
        viewModel.addAttribute("title", "Requests received");
        viewModel.addAttribute("requests", careerRequestRepository.findByIsCheckedFalse());
        viewModel.addAttribute("categories", categoryService.readAll());
        
        return "admin/dashboard";
    }

    // Rotta per la dashboard del revisore
    @GetMapping("/revisor/dashboard")
    public String revisorDashboard(Model viewModel) {
        viewModel.addAttribute("title", "Article to review");
        viewModel.addAttribute("articles", articleRepository.findByIsAcceptedIsNull());
        return "revisor/dashboard";
    }

    // Rotta per la dashboard del writer
    @GetMapping("/writer/dashboard")
    public String writerDashboard(Model viewModel, Principal principal) {
        
        viewModel.addAttribute("title", "Your articles");

        List<ArticleDto> userArticles = articleService.readAll()
                                                    .stream()
                                                    .filter(article -> article.getUser().getEmail().equals(principal.getName()))
                                                    .toList();

        viewModel.addAttribute("articles", userArticles);

        return "writer/dashboard";
    }
    
}
