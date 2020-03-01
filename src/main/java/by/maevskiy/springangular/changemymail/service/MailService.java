package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.MailFolder;

import java.util.List;

public interface MailService {
    List<MailFolder> getAllFolders(String userName, String password, String protocol);
}
