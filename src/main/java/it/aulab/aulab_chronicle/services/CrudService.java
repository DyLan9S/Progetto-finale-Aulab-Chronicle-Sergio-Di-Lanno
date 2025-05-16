package it.aulab.aulab_chronicle.services;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;


public interface CrudService<ReadDto, Model, Key> {

    List<ReadDto> readAll(); // Metodo per leggere tutti gli elementi

    ReadDto read(Key key); // Metodo per leggere un elemento tramite la sua chiave

    ReadDto create(Model model, Principal principal, MultipartFile file); // Metodo per creare un elemento

    ReadDto update(Key key, Model model, MultipartFile file); // Metodo per aggiornare un elemento

    void delete(Key key); // Metodo per eliminare un elemento tramite la sua chiave

}