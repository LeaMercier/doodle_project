package fr.istic.tlc.poll.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.istic.tlc.poll.entity.Comment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CommentRepository implements PanacheRepository<Comment> {
}
