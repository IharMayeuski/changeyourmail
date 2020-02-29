package by.maevskiy.springangular.changemymail.service;


import by.maevskiy.springangular.changemymail.domain.User;

import java.util.List;

public interface UserService {
    List<User> findAll();

    void save(User user);

}
