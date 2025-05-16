package it.aulab.aulab_chronicle.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.Image;
import it.aulab.aulab_chronicle.models.User;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
public class ArticleDto {

    private Long id;
    private String title;
    private String subtitle;
    private String body;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    private Boolean isAccepted;
    private User user;
    private Category category;
    private Image image;
}
