package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.util.List;

public interface MailService {
    List<MailFolder> getAllFolders(Store store);

    List <Message> getAllMessages(List<MailFolder> folders);

    void saveFiles(List<Message> messages, String filePath, String fileNamePattern, String action, String move, String pathToCredential);

    Session getSession(String email, String protocol);

    Store getStore(Session session, String email, String password, String protocol);

    void closeSessionStoreFolder(Session session, Store store, List<MailFolder> mailFolders);

    String getProtocol(String action);
}
