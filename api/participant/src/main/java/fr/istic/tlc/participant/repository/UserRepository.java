package fr.istic.tlc.participant.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.istic.tlc.participant.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
}
