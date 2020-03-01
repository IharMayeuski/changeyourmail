package by.maevskiy.springangular.changemymail.service;

import javax.mail.Folder;
import java.util.List;

public interface MailService {
    List<String> getAllFolders(String userName, String password, String protocol);
}
