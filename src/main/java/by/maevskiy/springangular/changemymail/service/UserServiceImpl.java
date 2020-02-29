package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.domain.User;
import by.maevskiy.springangular.changemymail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);
        for (User user: users) {
            System.out.println(user.login + "-" + user.password);
        }
        return users;
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }
}
