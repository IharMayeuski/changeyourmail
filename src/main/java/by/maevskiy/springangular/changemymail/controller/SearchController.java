package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.Pojo.SessionStoreFolder;
import by.maevskiy.springangular.changemymail.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class SearchController {
    private MailService mailService;

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping(value = "/search")
    public void saveFilesFromMail(@RequestBody Map<String, String> namePassText) {
//        final String PROTOCOL = "pop3";
        final String PROTOCOL = "imap";

        String email = namePassText.get("name");
        String password =  namePassText.get("pass");
        String destFilePath = mailService.converPath(namePassText.get("path"));
        String fileNamePattern = namePassText.get("search");

        Session session = mailService.getSession(email, PROTOCOL);
        Store store = mailService.getStore(session, email, password, PROTOCOL);

        List<MailFolder> mailFolders = mailService.getAllFolders(store);
        List<Message> messages = mailService.getAllMessages(mailFolders);

        mailService.saveFile(messages, destFilePath, fileNamePattern);

        mailService.closeSessionStoreFolder(session, store, mailFolders);

        System.out.println("All is finished");


    }
}
