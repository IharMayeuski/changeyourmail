package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.Pojo.MailFolderDTO;
import by.maevskiy.springangular.changemymail.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.Session;
import javax.mail.Store;
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
//        final String PROTOCOL = "pop3";
        final String PROTOCOL = "imap";

        String password =  namePass.get("pass");
        String email = namePass.get("name");

        Session session = mailService.getSession(email, PROTOCOL);
        Store store = mailService.getStore(session, email, password, PROTOCOL);

        List<MailFolder> mailFolders = mailService.getAllFolders(store);
        List<MailFolderDTO> mailFolderDTOS = new ArrayList<>();
        if (nonNull(mailFolders)) {
            mailFolders.forEach(mailFolder ->
                    mailFolderDTOS.add(new MailFolderDTO(
                            mailFolder.getFolder().getName(),
                            mailFolder.getMailQuantity()
                    ))
            );
        }
        mailService.closeSessionStoreFolder(session, store, mailFolders);
        return mailFolderDTOS;
    }
}
