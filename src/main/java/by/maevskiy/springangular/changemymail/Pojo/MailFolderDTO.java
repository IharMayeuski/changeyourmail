package by.maevskiy.springangular.changemymail.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.mail.Folder;

@Data
@AllArgsConstructor
public class MailFolderDTO {
    public String name;
    public Integer mailQuantity;
}
