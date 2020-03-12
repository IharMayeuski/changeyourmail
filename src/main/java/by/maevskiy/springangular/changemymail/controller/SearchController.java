package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.Message;
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
        List<MailFolder> mailFolders = mailService.getAllFolders(
                namePassText.get("name"),
                namePassText.get("pass"),
                "imap"
        );
        List<Message> messages = mailService.getAllMessages(mailFolders);
        String fileNamePattern = namePassText.get("search");
        String destFilePath = "C:/Users/Maevskiy/Desktop/files/";
        mailService.saveFile(messages, destFilePath, fileNamePattern);


    }
}
