package by.maevskiy.springangular.changemymail.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.mail.Folder;

@Data
@AllArgsConstructor
public class MailFolder {
    public Folder folder;
    public Integer mailQuantity;
}
