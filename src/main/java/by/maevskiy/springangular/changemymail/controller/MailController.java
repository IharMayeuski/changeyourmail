package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.Folder;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MailController {
    private MailService mailService;

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping(value = "/folders")
    public List<MailFolder> findAll() {
        String mail = "*";
        String password = "*";
        return mailService.getAllFolders(mail, password, "imap");
    }
}
