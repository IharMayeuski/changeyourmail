package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.service.MailService;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonString;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MailController {
    private MailService mailService;

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping(value = "/folders")
    public List<MailFolder> findAll(@RequestBody Map<String, String> namePass) {
        return mailService.getAllFolders(namePass.get("name"), namePass.get("pass"), "imap");
    }
}
