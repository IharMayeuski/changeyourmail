package by.maevskiy.springangular.changemymail.repository;

import by.maevskiy.springangular.changemymail.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}