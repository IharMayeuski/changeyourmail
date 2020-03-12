package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;

import javax.mail.Message;
import java.util.List;

public interface MailService {
    List<MailFolder> getAllFolders(String userName, String password, String protocol);

    List <Message> getAllMessages(List<MailFolder> folders);

    void saveFile(List<Message> messages, String filePath, String fileNamePattern);
}
