package fr.istic.tlc.planning.resources;

import java.util.*;

import javax.transaction.Transactional;
import javax.validation.Valid;

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

import fr.istic.tlc.planning.entity.Choice;
import fr.istic.tlc.planning.repository.ChoiceRepository;
import fr.istic.tlc.planning.dto.ChoiceUser;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import java.util.concurrent.Future;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;


@RestController
@RequestMapping("/api")
public class ChoiceRessource {

	@Autowired
	ChoiceRepository choiceRepository;

	/**
	 * 
	 * @param idUser
	 * @return
	 * @throws Exception
	 */
	private List<Long> getUserChoices(long idUser) throws Exception {
		String url = "http://participant:8081/api/users/" + idUser + "/choices";
		CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpGet request = new HttpGet(url);

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

		int tmp = responseString.length() - 1;
		responseString = responseString.substring(1, tmp);

		List<String> choices = Arrays.asList(responseString.split(","));
		List<Long> choiceIds = new ArrayList<Long>();
		// on verifie si le resultat est vide et le cas echeant on ne demarre pas de
		// boucle for
		if (choices.isEmpty()) {
			return choiceIds;
		}
		for (String id : choices) {
			if (!id.isEmpty())
				choiceIds.add(Long.parseLong(id));
		}

		return choiceIds;
	}
	
	/**
	 * 
	 * @param slug
	 * @return
	 * @throws Exception
	 */
	private List<Long> getPollChoices(String slug) throws Exception {
		String url = "http://poll:8083/api/polls/" + slug + "/choices";
		CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpGet request = new HttpGet(url);

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

		int tmp = responseString.length() - 1;
		responseString = responseString.substring(1, tmp);

		List<String> choices = Arrays.asList(responseString.split(","));
		List<Long> choiceIds = new ArrayList<Long>();
		// on verifie si le resultat est vide et le cas echeant on ne demarre pas de
		// boucle for
		if (choices.isEmpty()) {
			return choiceIds;
		}
		for (String id : choices) {
			if (!id.isEmpty())
				choiceIds.add(Long.parseLong(id));
		}

		return choiceIds;
	}
	
	/**
	 * 
	 * @param choiceId
	 * @param slug
	 * @throws Exception
	 */
	public static void removePollbyHttp(List<Long> choices, String slug) throws Exception {
        String url = "http://poll:8083/api/polls/"+slug;// +"/"+choices;
        
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

	public static void removeChoiceFromUser(long choiceId, long userId) throws Exception {
	String url = "http://participant:8081/api/users/" + userId +
				"/deleteChoice/" + choiceId;

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
	
	/**
	 * 
	 * @param idUser
	 * @return
	 * @throws Exception
	 */
	private boolean doesUserExists(Long idUser) throws Exception{
        
        String url = "http://participant:8081/api/users/"+idUser;
        //ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class, idUser);

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpGet request = new HttpGet(url);

		Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();
        
		client.close();

        return (response.getStatusLine().getStatusCode() == 200);
    }
	
	/**
	 * 
	 * @param slug
	 * @return
	 * @throws Exception
	 */
	private boolean doesPollExists(String slug) throws Exception{
        
        String url = "http://poll:8083/api/polls/"+ slug ;
        //ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class, idUser);

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpGet request = new HttpGet(url);

		Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();
        
		client.close();

        return (response.getStatusLine().getStatusCode() == 200);
    }
	
	/**
	 * 
	 * @param idChoice
	 * @param idUser
	 * @throws Exception
	 */
	private static void addChoiceToUser(long idChoice, long idUser) throws Exception {
        String url = "http://participant:8081/api/users/"+idUser+"/choices/"+idChoice;

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        HttpPut request = new HttpPut(url);

        Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();

        client.close();
    }
	
	/**
	 * Permet de mettre à jour le choix correspondant à l'id dans le poll correspondant au slug
	 * @param idChoice
	 * @param slug
	 * @throws Exception
	 */
	private static void updateChoiceToPoll(long idChoice, long newIdChoice, String slug) throws Exception {
        String url = "http://poll:8083/api/polls/"+slug+
					"/choices/"+idChoice+
					"?newchoice="+newIdChoice;

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        HttpPut request = new HttpPut(url);

        Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();

        client.close();
    }
	
	/**
	 * 
	 * @param slug
	 */
	private static boolean getSlugAdmin(String slug, String token) 
	throws Exception{
        String url = "http://poll:8083/api/polls/"+slug+"/aslug";

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
		client.start();
		HttpGet request = new HttpGet(url);

		Future<HttpResponse> future = client.execute(request, null);
        String responseString="";
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
		String slugAdmTmp = responseString;
		
		return slugAdmTmp.equals(token);
	}
	
	/**
	 * 
	 * @param choices
	 * @throws Exception
	 */
	public static void addChoicesToPoll (List<Long> choices, String slug) throws Exception {
        String url = "http://poll:8083/api/polls/"+slug+"/choices";

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        HttpPut request = new HttpPut(url);
		String body = choices.toString();
		StringEntity stringEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
		//SerializableEntity serieEntity = new SerializableEntity(choices);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-Type", "application/json");
		request.setEntity(stringEntity);
        Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();
        client.close();
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Permet de récupérer tout les choix de Poll
	 * @param slug l'identifiant du poll que l'on essaye de joindre
	 * @return la list des choix si jamais nous les avons trouvés
	 * @throws Exception
	 */
	@GetMapping("/planning/polls/{slug}/choices")
	public ResponseEntity<List<Choice>> retrieveAllChoicesFromPoll(@PathVariable String slug)
            throws Exception {
        // On vérifie que le poll qu'on appelle existe
        if(!this.doesPollExists(slug)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            //on parse la reponse
            List<Long> choiceIds =  this.getPollChoices(slug);
            List<Choice> choices = new ArrayList<Choice>();
            if(choiceIds.isEmpty()){
                return new ResponseEntity<>(choices, HttpStatus.OK);
            }
            for (Long id : choiceIds) {
                choices.add(choiceRepository.findById(id));
            }
            return new ResponseEntity<>(choices, HttpStatus.OK);
        }
    }

	/**
	 * 
	 * @param idUser
	 * @return
	 * @throws Exception
	 */
	// @GetMapping("/users/{idUser}/choices")
    // public ResponseEntity<List<Choice>> retrieveAllChoicesFromUser(@PathVariable long idUser) 
    //         throws Exception {
    //     // On vérifie que l'utilisateur existe

    //     if(!this.doesUserExists(idUser)) {
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     } else {

    //         //on parse la reponse
    //         List<Long> choiceIds =  this.getUserChoices(idUser);
    //         List<Choice> choices = new ArrayList<Choice>();
    //         if(choiceIds.isEmpty()){
    //             return new ResponseEntity<>(choices, HttpStatus.OK);
    //         }
    //         for (Long id : choiceIds) {
    //             choices.add(choiceRepository.findById(id));
    //         }
    //         return new ResponseEntity<>(choices, HttpStatus.OK);
    //     }
    // }

	/**
	 * On récupère le nombre de votes par choix
	 * @param slug
	 * @param idChoice
	 * @return
	 */
    // @GetMapping("/polls/{slug}/choices/{idChoice}/count")
    // public ResponseEntity<Object> numberOfVoteForChoice(@PathVariable String slug, @PathVariable long idChoice){
    //     // On vérifie que le poll et choix existent
    //     Choice choice = choiceRepository.findById(idChoice);
    //     if (!this.doesPollExists(slug)|| choice == null){
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     } else {
    //         // On vérifie que le choix appartienne bien au poll
    //         if(!this.getPollChoices().contains(choice)){
    //             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //         }        	
    //     }
    //     // On compte le nombre de vote pour le choix
    //     return new ResponseEntity<>(choice.getUserIds().size(),HttpStatus.OK);
    // }
   
    /**
     * On récupère un choix correspondant à un ID se trouvant dans un poll spécifique
     * @param slug
     * @param idChoice
     * @return
     */
	//  @GetMapping("/polls/{slug}/choices/{idChoice}")
	//     public ResponseEntity<Choice> retrieveChoiceFromPoll(@PathVariable String slug, @PathVariable long idChoice) {
	//         // On vérifie que le choix et le poll existent
	//         Choice choice = choiceRepository.findById(idChoice);
	//         if (!this.doesPollExists(slug) || choice== null) {
	//             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	//         } else {
	// 	        // On vérifie que le choix appartienne bien au poll
	// 	        if(!this.getPollChoices().contains(choice)){
	// 	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	// 	        }	        	
	//         }
	//         return new ResponseEntity<>(choice, HttpStatus.OK);
	//     }
	 
	 /**
	  * On récupère un choix correspondant à un ID relié à un utilisateur spécifique
	  * @param idUser
	  * @param idChoice
	  * @return
	  * @throws Exception
	  */
	    // @GetMapping("/users/{idUser}/choices/{idChoice}")
	    // public ResponseEntity<Choice> retrieveChoiceFromUser(@PathVariable long idUser, @PathVariable long idChoice)
	    //         throws Exception {
	    //      // On vérifie que le choix et l'utilisateur existe
	    //     Choice choice = choiceRepository.findById(idChoice);

	    //     if(!this.doesUserExists(idUser) || choice == null){
	    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    //     } else {
	    //       // On vérifie que le choix appartienne bien à l'utilisateur
	    //         if(!choice.getUserIds().contains(idUser)){
	    //             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    //          } else {

	    //             return new ResponseEntity<>(choice, HttpStatus.OK);
	    //          }  
	    //     }
	    // }
	    
	    /**
	     * Permet de récupérer la list des IDs des utilisateurs reliés à un choix spécifique
	     * @param idChoice
	     * @return
	     * @throws Exception
	     */
	    @GetMapping("/planning/{idChoice}/userids")
	    public ResponseEntity<List<Long>> retrieveUsersId (@PathVariable long idChoice) throws Exception {
	        Choice choice = choiceRepository.findById(idChoice);
	    	if (choice == null) {
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);	    		
	    	}
	    	return new ResponseEntity<>(choice.getUserIds(), HttpStatus.OK);
	    }

	    /**
	     * Permet de récupérer un choix grâce à son id
	     * @param idChoice
	     * @return
	     * @throws Exception
	     */
	    @GetMapping("/planning/{idChoice}")
	    public ResponseEntity<Choice> retrieveChoiceById(@PathVariable long idChoice) throws Exception {
	        Choice choice = choiceRepository.findById(idChoice);
	    	if (choice == null) {
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);	    		
	    	}
	    	return new ResponseEntity<>(choice, HttpStatus.OK);
	    }
	    
	    /**
	     * 
	     * @param choices
	     * @param slug
	     * @param token
	     * @return
	     * @throws Exception
	     */
	    // @DeleteMapping("/polls/{slug}/choices")
	    // public ResponseEntity<?> deleteChoiceFromPoll(@RequestBody HashMap<String, List<Long>> choices, @PathVariable String slug, @RequestParam("token") String token) 
	    //         throws Exception {
	    //     // On vérifie que le poll existe
	    //     List<Long> idchoices = choices.get("choices");
	    //     if (!this.doesPollExists(slug)) {
	    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    //     }
	    //     // On vérifie que le token soit bon
	    //     if(!this.getSlugAdmin(slug,token)){
	    //         return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    //     }
	    //     this.removePollbyHttp(choices, slug);
	    //     return new ResponseEntity<>(HttpStatus.OK);
	    // }

	    /**
	     * 
	     * @param choices
	     * @param slug
	     * @param token
	     * @return
	     */
	    @Transactional
	    @PostMapping("/planning/polls/{slug}/choices")
	    public ResponseEntity<List<Choice>> createChoices(@RequestBody List<Choice> choices, @PathVariable String slug, @RequestParam("token") String token) 
		throws Exception{
	        // On vérifie que le poll existe
	        if (!this.doesPollExists(slug)){
	            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	        }
	        // On vérifie que le token soit bon
	        if(!getSlugAdmin(slug,token)){
	            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	        }
	        // On ajoute chaque choix au poll et vice versa
			List<Long> choiceIds = new ArrayList<Long>();
	        for (Choice choice:choices) {
	        	this.choiceRepository.persist(choice);
				choiceIds.add(choice.getId());
	        }
			addChoicesToPoll(choiceIds, slug);
	        return new ResponseEntity<>(choices, HttpStatus.CREATED);
	    }

	    @Transactional
	    @PutMapping("/planning/polls/{slug}/choices/{idChoice}")
	    public ResponseEntity<Choice> updateChoice(@Valid @RequestBody Choice choice1, @PathVariable String slug, @PathVariable long idChoice, @RequestParam("token") String token) 
		throws Exception {
	        // On vérifie que le poll et le choix existent
	        Choice choice = choiceRepository.findById(idChoice);
	        if (!this.doesPollExists(slug) || choice == null) {
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	        // On vérifie que le choix appartienne bien au poll
	        if(!this.getPollChoices(slug).contains(choice.getId())){
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	        // On vérifie que le token soit bon
	        if(!getSlugAdmin(slug,token)){
	            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	        }
	        // On met à jour l'ancien choix
	        Choice ancientChoice = choice;
	        if (choice1.getstartDate()!=null){
	            ancientChoice.setstartDate(choice1.getstartDate());
	        }
	        if (choice1.getendDate()!=null){
	            ancientChoice.setendDate(choice1.getendDate());
	        }
	        // On update la bdd
	        Choice updatedChoice = (Choice) choiceRepository.getEntityManager().merge(ancientChoice);
			//Etape inutile etant donné que l'id ne change pas
	        //updateChoiceToPoll(idChoice, updatedChoice.getId(), slug);
	        return new ResponseEntity<>( HttpStatus.OK);
	    }

	    @Transactional
	    @PutMapping("/planning/polls/{slug}/vote/{idUser}")
	    public ResponseEntity<Object> vote(@RequestBody HashMap<String, List<Long>> choices, @PathVariable String slug, @PathVariable long idUser) 
	             throws Exception {
	        // On vérifie que le poll et l'utilisateur existent
	        List<Long> idchoices = choices.get("choices");
	       
	        if (!this.doesPollExists(slug) || !this.doesUserExists(idUser)){
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	        for (Long choice : idchoices) {
	            // On vérifie que le choice existe
	            Choice optchoice = choiceRepository.findById(choice);
	            if (optchoice == null){
	                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	            }
	            // On vérifie que le choix appartienne bien au poll
				
	            if(!this.getPollChoices(slug).contains(optchoice.getId())){
	                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	            }
	            // On vérifie que le user n'ai pas déjà voté pour ce choix
	            List<Long> userChoiceIds = this.getUserChoices(idUser);
	            if( userChoiceIds.contains(optchoice.getId())){
	                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	            }
	            // On ajoute le choix à la liste de l'utilisateur et vice versa
	            optchoice.addUser(idUser);
	            addChoiceToUser( choice, idUser);
	            choiceRepository.getEntityManager().merge(optchoice);
	        }
	        return new ResponseEntity<>(HttpStatus.OK);
	    }

	    @Transactional
	    @PutMapping("/planning/polls/{slug}/choices/{idChoice}/removevote/{idUser}")
	    public ResponseEntity<Object> removeVote(@PathVariable String slug, @PathVariable long idChoice, @PathVariable long idUser)
	            throws Exception {
	        // On vérifie que le poll, le choix et l'utilisateur existent
	        Choice choice = choiceRepository.findById(idChoice);

	        if (!this.doesPollExists(slug) || choice == null || !this.doesUserExists(idUser)){
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	        // On vérifie que le choix appartienne bien au poll
	        if(!this.getPollChoices(slug).contains(choice.getId())){
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	        // On vérifie que le user ait bien voté pour ce choix
	        List<Long> choiceIds = this.getUserChoices(idUser);
	        if( choiceIds.isEmpty() || !choiceIds.contains(choice.getId())){
	            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	        }
	        // On retire le choix à la liste de l'utilisateur et vice versa
	        choice.removeUser(idUser);
	        removeChoiceFromUser(choice.getId(), idUser);
	        choiceRepository.getEntityManager().merge(choice);
	        

	        return new ResponseEntity<>(HttpStatus.OK);
	    }

	    // @Transactional
	    // @DeleteMapping("api/choices/{idChoice}/deleteUser/{idUser}")
	    // public ResponseEntity<?> deleteUser(@PathVariable Long idChoice, @PathVariable Long idUser) {
	    //     Choice choice = choiceRepository.findById(idChoice);
	    //     if(choice == null){
	    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    //     } else {
	    //         choice.removeUser(idUser);
	    //         return new ResponseEntity<>(HttpStatus.OK);
	    //     }
	    // }


		@Transactional
		@PostMapping("/planning/choiceuser/")
		public ResponseEntity<?> addChoiceUser(@RequestBody ChoiceUser userChoice) 
		throws Exception{
			//User u = this.userRep.find("mail", userChoice.getMail()).firstResult();
			Long userId = this.findUserBymail(userChoice.getMail());
			if (userId == null) {
				// u = new User();
				// u.setUsername(userChoice.getUsername());
				// u.setIcsurl(userChoice.getIcs());
				// u.setMail(userChoice.getMail());
				// this.userRep.persist(u);
				this.createUser(userChoice.getUsername(), userChoice.getMail(), userChoice.getMail());
			}

			if (userChoice.getPref() != null && !"".equals(userChoice.getPref())) {
				// MealPreference mp = new MealPreference();
				// mp.setContent(userChoice.getPref());
				// mp.setUser(u);
				// this.mealprefRep.persist(mp);
				this.addMealPreference(userId, userChoice.getPref());
			}
			for (Long choiceId : userChoice.getChoices()) {
				Choice c = this.choiceRepository.findById(choiceId);
				c.addUser(userId);
				this.choiceRepository.persistAndFlush(c);
				addChoiceToUser(choiceId, userId);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}

		public Long findUserBymail(String email)
		throws Exception{

			String url = "http://participant:8081/api/users/email/"+email;

			CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
			client.start();
			HttpGet request = new HttpGet(url);

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

			return Long.parseLong(responseString);
		}


		public void createUser(String username, String email, String ics)
		throws Exception {
			String url = "http://participant:8081/api/users";
			String body = "{\"username\":\""+username+
							"\",\"mail\":\""+email+
							"\",\"icsurl\":\""+ics+"\"}";

			StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
			CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
			client.start();
			HttpPost request = new HttpPost(url);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "application/json");
			request.setEntity(entity);

			Future<HttpResponse> future = client.execute(request, null);

			client.close();
		}

		public void addMealPreference(long userId, String mp)
		throws Exception {

			String url = "http://participant:8081/api/users/"+userId+"/createmealpreference/"+mp;
			CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
			client.start();
			HttpPost request = new HttpPost(url);

			Future<HttpResponse> future = client.execute(request, null);
			
			
			HttpResponse response = future.get();
			client.close();

		}

}
