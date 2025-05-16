package it.aulab.aulab_chronicle.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async // Esegue questo metodo in un thread separato
    public void sendSimpleEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();

        // Imposta il mittente della mail
        message.setFrom("aulabchronicle@administration.com");

        // Imposta il destinatario, l'oggetto e il testo della mail usando i parametri
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        // Invia la mail usando il JavaMailSender iniettato
        mailSender.send(message);
    }
}