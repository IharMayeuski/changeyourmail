package by.maevskiy.springangular.changemymail;

import by.maevskiy.springangular.changemymail.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChangemymailApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangemymailApplication.class, args);
    }

//    @Bean
//    CommandLineRunner init(UserRepository userRepository) {
//        return args -> {
//            userRepository.findAll().forEach(System.out::println);
//        };
//    }
}
