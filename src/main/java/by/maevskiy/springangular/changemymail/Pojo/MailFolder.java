package by.maevskiy.springangular.changemymail.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.mail.Folder;

@Data
@AllArgsConstructor
public class MailFolder {
    public Folder folder;
    public Integer mailQuantity;
}
