package fr.istic.tlc.participant.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.istic.tlc.participant.entity.MealPreference;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class MealPreferenceRepository implements PanacheRepository<MealPreference> {
}
