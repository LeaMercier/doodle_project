package fr.istic.tlc.planning.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.istic.tlc.planning.entity.Choice;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ChoiceRepository implements PanacheRepository<Choice> {}
