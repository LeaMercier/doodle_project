package fr.istic.tlc.poll.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import fr.istic.tlc.poll.SendEmail;
import fr.istic.tlc.poll.entity.Comment;
import fr.istic.tlc.poll.entity.Poll;
import fr.istic.tlc.poll.repository.CommentRepository;
import fr.istic.tlc.poll.repository.PollRepository;

@Path("api/poll")
public class NewPollResourceEx {

	@Inject
	PollRepository pollRep;

	@Inject
	CommentRepository commentRep;
	
	
	@Inject
	SendEmail sendmail;


	@Path("/slug/{slug}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Poll getPollBySlug(@PathParam("slug") String slug) {
		Poll p = pollRep.findBySlug(slug);
		if (p != null)
			p.getPollComments().clear();
		p.setSlugAdmin("");
		return p;
	}

	@Path("/aslug/{aslug}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Poll getPollByASlug(@PathParam("aslug") String aslug) {
		return pollRep.findByAdminSlug(aslug);
	}

	@Path("/comment/{slug}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public Comment createComment4Poll(@PathParam("slug") String slug, Comment c) {
		this.commentRep.persist(c);
		Poll p = pollRep.findBySlug(slug);
		p.addComment(c);
		this.pollRep.persistAndFlush(p);
		return c;

	}

	@PUT
	@Path("/update1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public Poll updatePoll(Poll p) throws Exception{
		System.err.println( "p " + p);
		Poll p1 = pollRep.findById(p.getId());

		List<Long> choicesToRemove = new ArrayList<Long>();
		for (long c : p1.getPollChoices()) {
			if (!p.getPollChoices().contains(c)) {

				choicesToRemove.add(c);
				System.err.println("toremove " + c);
			}

		}
		
		for (long c : choicesToRemove) {
			if (c == p1.getSelectedChoice()) {
				p.setSelectedChoice(null);
				p1.setSelectedChoice(null);
				p.setClos(false);
			}
			Set<Long> userIds = p1.retrieveAllUserIds();
			for (Long userId : userIds) {
				String 	url = "http://participant:8081/api/users/" + userId + "/deleteChoice/" + c;
                CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
				client.start();
				HttpDelete request = new HttpDelete(url);

				Future<HttpResponse> future = client.execute(request, null);
				try {
				HttpResponse response = future.get();
				} catch (Exception e) {
					e.printStackTrace();
				}

				client.close();
                }

		}

		Poll p2 = this.pollRep.getEntityManager().merge(p);
		return p2;

	}

	/*@Path("/choiceuser")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public User addChoiceUser(ChoiceUser userChoice) {
		User u = this.userRep.find("mail", userChoice.getMail()).firstResult();
		if (u == null) {
			u = new User();
			u.setUsername(userChoice.getUsername());
			u.setIcsurl(userChoice.getIcs());
			u.setMail(userChoice.getMail());
			this.userRep.persist(u);
		}
		

		if (userChoice.getPref() != null && !"".equals(userChoice.getPref())) {
			MealPreference mp = new MealPreference();
			mp.setContent(userChoice.getPref());
			mp.setUser(u);
			this.mealprefRep.persist(mp);
		}
		for (Long choiceId : userChoice.getChoices()) {
			Choice c = this.choiceRep.findById(choiceId);
			c.addUser(u);
			this.choiceRep.persistAndFlush(c);
		}
		return u;
	}*/

	@Path("/selectedchoice/{choiceid}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void closePoll(@PathParam("choiceid") long choiceid) throws Exception {
		
		Poll p = this.pollRep.find("select p from Poll as p join p.pollChoices as c where c.id= ?1", choiceid)
				.firstResult();
		p.setClos(true);
		p.setSelectedChoice(choiceid);
		this.pollRep.persist(p);
		
		this.sendmail.sendASimpleEmail(p);

	}

	@GET()
	@Path("/{slug}/comments")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Comment> getAllCommentsFromPoll(@PathParam("slug") String slug) {
		Poll p = this.pollRep.findBySlug(slug);
		if (p!= null)
			return p.getPollComments();
		return null;
	}

}
