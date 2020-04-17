package by.maevskiy.springangular.changemymail.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

@Data
@AllArgsConstructor
public class SessionStoreFolder {
    public Session session;
    public Store store;
    public Folder folder;
}
