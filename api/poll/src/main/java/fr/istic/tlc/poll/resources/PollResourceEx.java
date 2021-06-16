package fr.istic.tlc.poll.resources;

import java.util.*;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import fr.istic.tlc.poll.entity.Poll;
import fr.istic.tlc.poll.repository.PollRepository;
import io.quarkus.panache.common.Sort;
//import net.gjerull.etherpad.client.EPLiteClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import java.util.concurrent.Future;

import static fr.istic.tlc.poll.Utils.generateSlug;

import java.io.ByteArrayOutputStream;
import java.net.URI;



@RestController
@RequestMapping("/api")
public class PollResourceEx {

	@Autowired
	PollRepository pollRepository;

	

	@GetMapping("/polls")
	public ResponseEntity<List<Poll>> retrieveAllpolls() {
		// On récupère la liste de tous les poll qu'on trie ensuite par titre
		List<Poll> polls = pollRepository.findAll(Sort.by("title", Sort.Direction.Ascending)).list();
		return new ResponseEntity<>(polls, HttpStatus.OK);
	}

	@GetMapping("/polls/{slug}")
	public ResponseEntity<Poll> retrievePoll(@PathVariable String slug, @RequestParam(name = "token",required = false) String token) {
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// Si un token est donné, on vérifie qu'il soit bon
		if (token != null && !poll.getSlugAdmin().equals(token)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		poll.setSlugAdmin("");
		return new ResponseEntity<>(poll, HttpStatus.OK);
	}

	@GetMapping("/polls/{slug}/pad")
	public ResponseEntity<String> retrievePadURL(@PathVariable("slug") String slug) {
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(poll.getPadURL(), HttpStatus.OK);
	}

	@GetMapping("/polls/{slug}/mealpreferences")
	public ResponseEntity<List<Long>> getMealPreferences(String slug) {
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(poll.getPollMealPreferences(), HttpStatus.OK);
	}

	@PutMapping("polls/{slug}/mealpreferences/{idMp}")
	@Transactional
	public ResponseEntity<Object> addMealPreferenceToPoll(@PathVariable("slug") String slug, @PathVariable("idMp") Long idMp) {
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		poll.addMealPreference(idMp);
		pollRepository.getEntityManager().merge(poll);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}


	@DeleteMapping("/polls/{slug}")
	@Transactional
	public ResponseEntity<Object> deletePoll(@PathVariable("slug") String slug, @RequestParam("token") String token) 
	throws Exception{
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// On vérifie que le token soit bon
		
		if (!poll.getSlugAdmin().equals(token)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		

		// On supprime tous les choix du poll
		poll.setPollChoices(null);

		// On supprime tous les commentaires du poll
		// Fait automatiquement par le cascade type ALL

		// On supprime le pad
		//TODO
		long padId = poll.getPadId();

		String url = "http://pad:8085/api/pads/" + padId;

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
		
		// On supprime le poll de la bdd
		pollRepository.deleteById(poll.getId());
		return new ResponseEntity<>(HttpStatus.OK);
	}


	
	@PostMapping("/polls")
	@Transactional
	public ResponseEntity<Poll> createPoll(@Valid @RequestBody Poll poll)
	throws Exception {
		pollRepository.persist(poll);
		// On cree le pad
		String pollTitle = poll.getTitle();
		String pollLocation = poll.getLocation();
		String pollDescription = poll.getDescription();
		
		
		URI uri = new URI("http",
						"pad:8085",
						"/api/pads", 
						"pollLocalisation="+ pollLocation +
						"&pollTitle=" + pollTitle +
						"&pollDescription=" + pollDescription, null);
		String url = uri.toASCIIString();
		
		CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpPost request = new HttpPost(url);

		Future<HttpResponse> future = client.execute(request, null);
		String responseString = "";
		try {
			HttpResponse response = future.get();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			responseString = out.toString();

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		client.close();
		
		//On recupere l'id et l'url du pad et on l'ajout au poll
		JSONParser parser = new JSONParser();
		try {
			
			Object obj = parser.parse(responseString);

			JSONObject obj2 = (JSONObject) obj;
			poll.setPadId((Long) obj2.get("id"));
			poll.setPadURL((String) obj2.get("padURL"));
			
		} catch (ParseException pe) {

			System.err.println("erreur de parsing à la position: " + pe.getPosition());
		}

		pollRepository.persist(poll);
		return new ResponseEntity<>(poll, HttpStatus.CREATED);
	}

	@PutMapping("/polls/{slug}")
	@Transactional
	public ResponseEntity<Object> updatePoll(@Valid @RequestBody Poll poll, @PathVariable String slug,
			@RequestParam String token) throws Exception {
		// On vérifie que le poll existe
		Poll optionalPoll = pollRepository.findBySlug(slug);
		if (optionalPoll == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		// On vérifie que le token soit bon************************************
		if (!optionalPoll.getSlugAdmin().equals(token)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		// On met au poll le bon id et les bons slugs
		Poll ancientPoll = optionalPoll;
		// On se connecte au pad
		//TODO
		//String padId = getPadId(ancientPoll);

		// On sauvegarde les anciennes données pour mettre à jour le pad
		String title = ancientPoll.getTitle();
		String location = ancientPoll.getLocation();
		String description = ancientPoll.getDescription();

		// On met à jour l'ancien poll
		if (poll.getTitle() != null) {
			ancientPoll.setTitle(poll.getTitle());
		}
		if (poll.getLocation() != null) {
			ancientPoll.setLocation(poll.getLocation());
		}
		if (poll.getDescription() != null) {
			ancientPoll.setDescription(poll.getDescription());
		}
		ancientPoll.setHas_meal(poll.isHas_meal());
		
		// On update le pad
		long padId = poll.getPadId();
		String pollTitle = poll.getTitle();
		String pollLocation = poll.getLocation();
		String pollDescription = poll.getDescription();


		URI uri = new URI("http", "pad:8085", "/api/pads"+padId,
				"pollLocalisation=" + pollLocation + "&pollTitle=" + pollTitle + "&pollDescription=" + pollDescription,
				null);
		String url = uri.toASCIIString();

		CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpPut request = new HttpPut(url);

		Future<HttpResponse> future = client.execute(request, null);

		try {
			HttpResponse response = future.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		client.close();

		// On enregistre le poll dans la bdd
		Poll updatedPoll = pollRepository.getEntityManager().merge(ancientPoll);
		return new ResponseEntity<>(HttpStatus.OK);
	}


	@GetMapping("/polls/{slug}/users")
	public ResponseEntity<List<String>> getAllUserFromPoll(@PathVariable String slug) throws Exception{
		Set<Long> userIds = new LinkedHashSet<Long>();
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// On parcours les choix du poll pour récupérer les users ayant voté
		else if (!poll.getPollChoices().isEmpty()) {
			List<String> users = poll.retrieveAllUsers();

			return new ResponseEntity<>(users, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/polls/{slug}/aslug")
	public ResponseEntity<String> getAdminSlug(@PathVariable String slug) {
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(poll.getSlugAdmin(), HttpStatus.OK);
	}


	public static List<String> getAllUserEmail(String slug) throws Exception{
		Set<Long> userIds;
		// On vérifie que le poll existe
		PollResourceEx pr = new PollResourceEx();
		Poll poll =pr.getP(slug);
		if (poll== null) {
			return null;
		}
		// On parcours les choix du poll pour récupérer les users ayant voté
		else if (!poll.getPollChoices().isEmpty()) {
			//On recupère les userIds
			userIds = poll.retrieveAllUserIds();
			
			List<String> emails = new ArrayList<String>();

			//On recupere les emails des users
			for(Long userId : userIds){
				String url = "http://participant:8081/api/users/"+userId+"/email";

				CloseableHttpAsyncClient client2 = HttpAsyncClients.createDefault();
				client2.start();
				HttpGet request = new HttpGet(url);
		
				Future<HttpResponse> future = client2.execute(request, null);
				HttpResponse response = future.get();
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				emails.add(out.toString());
				
				out.close();
				client2.close();
			}
            
			return emails;
		}
		else {
			return null;
		}
	} 

	@GetMapping("/polls/{slug}/users/emails")
	public ResponseEntity<List<String>> getEmails(@PathVariable String slug)
	throws Exception {
		Poll poll = pollRepository.findByAdminSlug(slug);
		if(poll == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		List<String> emails = getAllUserEmail(slug);
		return new ResponseEntity<>(emails, HttpStatus.OK);
	}


	public Poll getP(String slug){
		return pollRepository.findBySlug(slug);
	}




	//fonctions recupérés du choice ressource/////////////////////////////

	// private static HttpResponse getByHttp(String url) throws Exception {

	// 	CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
	// 	client.start();
	// 	HttpGet request = new HttpGet(url);

	// 	Future<HttpResponse> future = client.execute(request, null);
	// 	HttpResponse response = future.get();

	// 	client.close();

	// 	return response;
	// }

	// public static void removeUserbyHttp(String url) throws Exception {
	// 	//String url = "http://participant:8081/api/users/" + userId + "/deleteChoice/" + choiceId;
		
	// 	CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
	// 	client.start();
	// 	HttpDelete request = new HttpDelete(url);

	// 	Future<HttpResponse> future = client.execute(request, null);
	// 	try {
	// 		HttpResponse response = future.get();
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}

	// 	client.close();
	// }


	@DeleteMapping("/polls/{slug}/choices")
    public ResponseEntity<?> deleteChoiceFromPoll(@RequestBody HashMap<String, List<Long>> choices, @PathVariable String slug, @RequestParam("token") String token) 
            throws Exception {
        // On vérifie que le poll existe
        List<Long> idchoices = choices.get("choices");
        Poll poll = pollRepository.findBySlug(slug);
        if (poll == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le token soit bon
        if(!poll.getSlugAdmin().equals(token)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // On enlève les choix du poll
        for (Long idChoice: idchoices) {
            // On vérifie que le choice existe
			//Pas besoin de verifier etant donné que les requetes viendront de planning
			// String url = "http://planning:8082/api/planning/"+idChoice;
			// HttpResponse response = getByHttp(url);

            // if (response.getStatusLine().getStatusCode() == 200) {
                // On remove le choice du poll
                poll.removeChoice(idChoice);
                pollRepository.getEntityManager().merge(poll);
                
				//on recupère les usersids du choice
				//Fait dans Planning
                // url = "http://planning:8082/api/planning/"+idChoice+"/userids";
				// response = getByHttp(url);

				// ByteArrayOutputStream out = new ByteArrayOutputStream();
				// response.getEntity().writeTo(out);
				// String responseString = out.toString().substring(1, out.toString().length() - 1);
				// List<String> tmpList = Arrays.asList(responseString.split(","));
				// List<Long> choiceUserIds = new ArrayList<Long>();
				// for (String tmp : tmpList) {
				// 	if (!tmp.isEmpty())
				// 		choiceUserIds.add(Long.parseLong(tmp));
				// }

				// out.close();
				// // On remove le choices des utilisateurs
                // for (Long userId : choiceUserIds)) {
				// 	url = "http://participant:8081/api/users/" + userId + "/deleteChoice/" + idChoice;
                //     removeUserbyHttp(url);
                // }
                // On supprime le choice
				//Fait dans le microservice Planning
				//url = "http://planning:8082/api/planning/"+idChoice;
				//removeUserbyHttp(url);
           // }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PutMapping("/polls/{slug}/choices")
    public ResponseEntity<List<Long>> createChoices(@RequestBody String choiceString, @PathVariable String slug) {
        // On vérifie que le poll existe
        Poll poll = pollRepository.findBySlug(slug);
		System.out.println("slug : "+slug);
		System.out.println("choicestring : "+choiceString);
		choiceString = choiceString.substring(1, choiceString.length()-1);
		System.out.println("choicestring : " + choiceString);
		List<String> tmpList = Arrays.asList(choiceString.split(","));
		List<Long> choices = new ArrayList<Long>();
		for (String tmp : tmpList) {
			if(tmp.length()>1)
				tmp = tmp.substring(1);
			if (!tmp.isEmpty())
				choices.add(Long.parseLong(tmp));
		}
		System.out.println("choices : " + choices);
        if (poll == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
       
        // On ajoute chaque choix au poll et vice versa
        for (long choice:choices) {
        	//this.choiceRepository.persist(choice);
			if(!poll.getPollChoices().contains(choice)) {
            	poll.addChoice(choice);            
            	pollRepository.getEntityManager().merge(poll);
			}
        }
        return new ResponseEntity<>(choices, HttpStatus.CREATED);
    }

    @Transactional
    @PutMapping("/polls/{slug}/choices/{idChoice}")
    public ResponseEntity<Object> updateChoice(@Valid @RequestParam("newchoice") long newIdChoice, @PathVariable String slug, @PathVariable long idChoice, @RequestParam("token") String token) {
        // On vérifie que le poll et le choix existent
        Poll poll = pollRepository.findBySlug(slug);
        //Choice choice = choiceRepository.findById(idChoice);
        if (poll == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le choix appartienne bien au poll
        if(!poll.getPollChoices().contains(idChoice)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le token soit bon
        if(!poll.getSlugAdmin().equals(token)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // On met à jour l'ancien choix
        // Choice ancientChoice = choice;
        // if (choice1.getstartDate()!=null){
        //     ancientChoice.setstartDate(choice1.getstartDate());
        // }
        // if (choice1.getendDate()!=null){
        //     ancientChoice.setendDate(choice1.getendDate());
        // }
        // On update la bdd
        //Choice updatedChoice = (Choice) choiceRepository.getEntityManager().merge(ancientChoice);
		List<Long> choices = poll.getPollChoices();
		choices.remove(idChoice);
		choices.add(newIdChoice);
		poll.setPollChoices(choices);
		pollRepository.getEntityManager().merge(poll);
        return new ResponseEntity<>( HttpStatus.OK);
    }

	@GetMapping("/polls/{slug}/choices")
	public ResponseEntity<List<Long>> retrieveAllChoicesFromPoll(@PathVariable String slug) throws Exception {
		// On vérifie que le choix existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		List<Long> choices = poll.getPollChoices();
		return new ResponseEntity<>(choices, HttpStatus.OK);
	}

	/**
	 * On récupère le nombre de votes par choix
	 * 
	 * @param slug
	 * @param idChoice
	 * @return
	 */
	@GetMapping("/polls/{slug}/choices/{idChoice}/count")
	public ResponseEntity<Integer> numberOfVoteForChoice(@PathVariable String slug, @PathVariable long idChoice) 
	throws Exception{
		// On vérifie que le poll et choix existent
		//Choice choice = choiceRepository.findById(idChoice);
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			// On vérifie que le choix appartienne bien au poll
			if (!poll.getPollChoices().contains(idChoice)) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		// On compte le nombre de vote pour le choix
		List<Long> votes = poll.retrieveChoiceUsers(idChoice);
		return new ResponseEntity<>(votes.size(), HttpStatus.OK);
	}

}
