package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.Pojo.MailFolderDTO;
import by.maevskiy.springangular.changemymail.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MailController {
    private MailService mailService;

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping(value = "/folders")
    public List<MailFolderDTO> findAll(@RequestBody Map<String, String> namePass) {
        List<MailFolder> mailFolders = mailService.getAllFolders(
                namePass.get("name"),
                namePass.get("pass"),
                "pop3"
        );
        List<MailFolderDTO> mailFolderDTOS = new ArrayList<>();
        if (nonNull(mailFolders)) {
            for (MailFolder mailFolder : mailFolders) {
                mailFolderDTOS.add(new MailFolderDTO(mailFolder.getFolder().getName(), mailFolder.getMailQuantity()));
            }
        }
        return mailFolderDTOS;
    }
}
