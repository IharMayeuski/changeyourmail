package by.maevskiy.springangular.changemymail.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailFolderDTO {
    public String name;
    public Integer mailQuantity;
}
