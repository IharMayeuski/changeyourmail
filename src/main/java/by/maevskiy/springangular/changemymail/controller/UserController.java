package by.maevskiy.springangular.changemymail.controller;

import by.maevskiy.springangular.changemymail.domain.User;
import by.maevskiy.springangular.changemymail.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/greeting")
    public String helloController(
            @RequestParam(name = "name", required = false, defaultValue = "World") String name,
            Model model
    ) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping(value = "/users")
    public List<User> findAll() {
        System.out.println("!");
        return userService.findAll();
//        return "UserList";
    }
    @PostMapping("/users")
    void addUser(@RequestBody User user) {
        userService.save(user);
    }
}


