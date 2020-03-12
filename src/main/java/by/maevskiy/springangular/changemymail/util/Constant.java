package by.maevskiy.springangular.changemymail.util;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    public final static Map<String, String> hosts = new HashMap<String, String>() {
        {
            put("list.ru", "mail.ru");
            put("mail.ru", "mail.ru");
            put("inbox.ru", "mail.ru");
            put("gmail.com", "gmail.com");
        }
    };
}