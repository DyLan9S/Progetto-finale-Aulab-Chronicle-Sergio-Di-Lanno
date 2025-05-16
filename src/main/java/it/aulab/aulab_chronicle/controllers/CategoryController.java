package it.aulab.aulab_chronicle.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CategoryService;
import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ModelMapper modelMapper;


    // Rotta per la ricerca dell'articolo in base alla categoria
    @GetMapping("/search/{id}")
    public String categorySearch(@PathVariable("id") Long id, Model viewModel) {
        CategoryDto category = categoryService.read(id);

        viewModel.addAttribute("title", "All articles found by category " + category.getName());

        List<ArticleDto> articles = articleService.searchByCategory(modelMapper.map(category, Category.class));

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }

    // Rotta per la creazione di una categoria
    @GetMapping("create")
    public String categoryCreate(Model viewModel) {
        viewModel.addAttribute("title", "Create Category");
        viewModel.addAttribute("category", new Category());

        return "category/create";
    }

    // Rotta per la memorizzazione di una categoria
    @PostMapping
    public String categoryStore(@Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model viewModel) {
        

        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Create Category");
            viewModel.addAttribute("category", category);
            return "category/create";
        }

        categoryService.create(category, null, null);
        redirectAttributes.addFlashAttribute("successMessage", "Category added successfully");

        return "redirect:/admin/dashboard";
    }

    // Rotta per la modifica di una categoria
    @GetMapping("/edit/{id}")
    public String categoryEdit(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Edit Category");
        viewModel.addAttribute("category", categoryService.read(id));

        return "category/update";
    }

    // Rotta per la memorizzazione delle modifiche di una categoria
    @PostMapping("/update/{id}")
    public String categoryUpdate(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model viewModel) {

        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Update category");
            viewModel.addAttribute("category", category);
            return "category/update";        
        }

        categoryService.update(id, category, null);
        redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");

        return "redirect:/admin/dashboard";
    }

    // Rotta per l'eliminazione di una categoria
    @GetMapping("/delete/{id}")
    public String categoryDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully");

        return "redirect:/admin/dashboard";
    }
    
}