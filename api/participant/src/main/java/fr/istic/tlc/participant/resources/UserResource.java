package fr.istic.tlc.participant.resources;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import fr.istic.tlc.participant.entity.MealPreference;
import fr.istic.tlc.participant.entity.User;
import fr.istic.tlc.participant.repository.MealPreferenceRepository;
import fr.istic.tlc.participant.repository.UserRepository;
import io.quarkus.panache.common.Sort;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import java.util.concurrent.Future;

@RestController
@RequestMapping("api/users")
public class UserResource {

	@Autowired
	UserRepository userRepository;
	@Autowired
	MealPreferenceRepository mealPreferenceRepository;

	@GetMapping("/")
	public ResponseEntity<List<User>> retrieveAllUsers() {
		// On récupère tous les utilisateurs qu'on trie ensuite par username
		List<User> users = userRepository.findAll(Sort.by( "username", Sort.Direction.Ascending)).list();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@GetMapping ("/{idUser}/choices")
	public ResponseEntity<List<Long>> getAllChoiceFromUser(@PathVariable Long idUser) {
		// On vérifie que l'utilisateur existe
		User user = userRepository.findById(idUser);
		if (user== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		List<Long> choices = user.getUserChoices();
		return new ResponseEntity<>(choices, HttpStatus.OK);
	}

	@GetMapping("/{idUser}")
	public ResponseEntity<User> retrieveUser(@PathVariable Long idUser) {
		// On vérifie que l'utilisateur existe
		User user = userRepository.findById(idUser);
		if (user== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	/*
	@GetMapping("/polls/{slug}/users")
	public ResponseEntity<List<User>> getAllUserFromPoll(@PathVariable String slug) {
		List<User> users = new ArrayList<>();
		// On vérifie que le poll existe
		Poll poll = pollRepository.findBySlug(slug);
		if (poll== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// On parcours les choix du poll pour récupérer les users ayant voté
		if (!poll.getPollChoices().isEmpty()) {
			for (Choice choice : poll.getPollChoices()) {
				if (!choice.getUsers().isEmpty()) {
					for (User user : choice.getUsers()) {
						// On vérifie que le user ne soit pas déjà dans la liste
						if (!users.contains(user)) {
							users.add(user);
						}
					}
				}
			}
		}
		return new ResponseEntity<>(users, HttpStatus.OK);
	}*/

	@Transactional
	@DeleteMapping("/{idUser}")
	public ResponseEntity<User> deleteUser(@PathVariable Long idUser) throws Exception{
		// On vérifie que l'utilisateur existe
		User user = userRepository.findById(idUser);
		if (user== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// On supprime l'utilisateur de la liste d'utilisateur de chaque choix
		for (Long idChoice : user.getUserChoices()) {
			String url = "http://poll:8083/api/choices/"+idChoice+"/deleteUser/"+idUser;

			CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
			client.start();
			HttpDelete request = new HttpDelete(url);

			Future<HttpResponse> future = client.execute(request, null);

            HttpResponse response = future.get();

			client.close();
        	
		}
		// On supprime les commentaires de l'utilisateurs
		// Fait automatiquement par le cascade type ALL

		// On supprime l'utilisateur de la bdd
		userRepository.deleteById(idUser);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/email/{email}")
	public ResponseEntity<Long> getUserByMail(@PathVariable String email) {
		User user = userRepository.find("mail", email).firstResult();
		if(user == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Long>(user.getId(), HttpStatus.OK);
	}

	@Transactional
	@PostMapping("/")
	public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
		// On sauvegarde l'utilisateur dans la bdd
		userRepository.persist(user);
		return new ResponseEntity<>(user, HttpStatus.CREATED);
	}

	@Transactional
	@PutMapping("/{idUser}")
	public ResponseEntity<User> updateUser(@PathVariable Long idUser, @Valid @RequestBody User user) {
		// On vérifie que l'utilisateur existe
		User optionalUser = userRepository.findById(idUser);
		if (optionalUser== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// On met le bon id sur l'utilisateur
		User ancientUser = optionalUser;
		if (user.getUsername() != null) {
			ancientUser.setUsername(user.getUsername());
		}
		if (user.getMail() != null) {
			ancientUser.setMail(user.getMail());
		}
		// On update l'utilisateur dans la bdd
		User updatedUser = userRepository.getEntityManager().merge(ancientUser);
		
		
		return new ResponseEntity<>( HttpStatus.OK);
	}

	@Transactional
	@DeleteMapping("/{idUser}/deleteChoice/{idChoice}")
	public ResponseEntity<?> deleteChoice (@PathVariable Long idUser, @PathVariable Long idChoice) {
		User user = userRepository.findById(idUser);
		if (user== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			user.removeChoice(idChoice);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	
	@Transactional
	@PutMapping("/{idUser}/mealpreference/{idMp}")
	public ResponseEntity<?> addmealpreference(@PathVariable Long idUser, @PathVariable Long idMp) {
		User user = userRepository.findById(idUser);
		if (user== null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			user.addMealPreference(mealPreferenceRepository.findById(idMp));
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@Transactional
	@PostMapping("/{idUser}/createmealpreference/{mp}")
	public ResponseEntity<?> createmealpreference(@PathVariable Long idUser, @PathVariable String mp) {
		User user = userRepository.findById(idUser);
		if (user == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			MealPreference mpe = new MealPreference(mp);
			user.addMealPreference(mpe);
			mealPreferenceRepository.persist(mpe);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@Transactional
	@PutMapping("/{idUser}/choices/{idChoice}")
	public ResponseEntity<?> addChoice(@PathVariable Long idUser, @PathVariable Long idChoice) {
		User user = userRepository.findById(idUser);
		if (user == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			user.addChoiceId(idChoice);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
	
}
