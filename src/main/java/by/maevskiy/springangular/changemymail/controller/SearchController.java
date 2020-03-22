package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
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
        String email = namePassText.get("name");
        String password =  namePassText.get("pass");
        String action =  namePassText.get("deleteTo");
        String moveTo =  namePassText.get("move");
        String destFilePath = System.getProperty("user.home") + "\\Downloads\\fileFrom!!!!\\";
        String fileNamePattern = namePassText.get("search");

        String protocol = mailService.getProtocol(action);
        Session session = mailService.getSession(email, protocol);
        Store store = mailService.getStore(session, email, password, protocol);
        List<MailFolder> mailFolders = mailService.getAllFolders(store);
        List<Message> messages = mailService.getAllMessages(mailFolders);
        mailService.saveFiles(messages, destFilePath, fileNamePattern, action, moveTo);
        mailService.closeSessionStoreFolder(session, store, mailFolders);
    }
}
