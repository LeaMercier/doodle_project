package fr.istic.tlc.poll.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import fr.istic.tlc.poll.entity.Poll;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class PollRepository implements PanacheRepository<Poll> {
    
	public Poll findBySlug(String slug){
		return find("slug", slug).firstResult();	
	}
	public Poll findByAdminSlug(String slug){
		return find("slugAdmin", slug).firstResult();	
	}
	/*public List<User> findAllUser4Poll(long id){
		return this.getEntityManager().createQuery("select distinct c.users from Poll p join p.pollChoices as c where p.id = ?1").setParameter(1, id).getResultList();	
	}*/
}
