package by.maevskiy.springangular.changemymail.domain;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column
    public String login;

    @Column
    public String password;

    @Column
    private String role;

}
