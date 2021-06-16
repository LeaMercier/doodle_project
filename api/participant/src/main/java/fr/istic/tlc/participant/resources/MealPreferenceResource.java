package fr.istic.tlc.participant.resources;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import fr.istic.tlc.participant.entity.MealPreference;
import fr.istic.tlc.participant.entity.User;
import fr.istic.tlc.participant.repository.MealPreferenceRepository;
import fr.istic.tlc.participant.repository.UserRepository;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api")
public class MealPreferenceResource {

    @Autowired
    MealPreferenceRepository mealPreferenceRepository;

    @Autowired
    UserRepository userRepository;

    //Here are the http request to the poll microservice
    //////////////////////////////////////////////////////////////////////////////////
   private boolean doesPollExists(String slug) throws Exception {
       String url = "http://poll:8083/api/poll/slug/"+slug;

       CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
       client.start();
       HttpGet request = new HttpGet(url);

       Future<HttpResponse> future = client.execute(request, null);
       HttpResponse response = future.get();

       client.close();

       return (response.getStatusLine().getStatusCode() == 200);
   }

   private List<MealPreference> getPollMealPreferences(String slug) throws Exception{

        String url = "http://poll:8083/api/polls/slug/"+slug+"/mealpreferences";
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

       List<String> mpString = Arrays.asList(responseString);
       List<MealPreference> mps = new ArrayList<MealPreference>();
       if(mpString.isEmpty()){
           return mps;
       }
       for (String id : mpString) {
           long mpId = Long.parseLong(id);
           mps.add(mealPreferenceRepository.findById(mpId));
       }

       return mps;

   }

    ///////////////////////////////////////////////////////////////////////////////////

    @GetMapping("polls/{slug}/mealpreferences")
    public ResponseEntity<Object> getAllMealPreferencesFromPoll(@PathVariable String slug) throws Exception{
        // On vérifie que le poll existe
        if(!this.doesPollExists(slug)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<MealPreference> mps = this.getPollMealPreferences(slug);

        return new ResponseEntity<>(mps, HttpStatus.OK);
    }

    @GetMapping("polls/{slug}/mealpreference/{idMealPreference}")
    public ResponseEntity<Object> getMealPreferenceFromPoll(@PathVariable String slug, @PathVariable long idMealPreference)
            throws Exception{
        // On vérifie que le poll et la meal preference existe
        MealPreference optMealPreference = mealPreferenceRepository.findById(idMealPreference);
        
        if(!(this.doesPollExists(slug)) || optMealPreference == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<MealPreference> mps = this.getPollMealPreferences(slug);
        // On vérifie que la meal preference appartienne bien au poll
        if (mps.isEmpty() || !mps.contains(optMealPreference)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(optMealPreference,HttpStatus.OK);
    }

    @Transactional
    @PostMapping("polls/{slug}/mealpreference/{idUser}")
    public ResponseEntity<Object> createMealPreference(@Valid @RequestBody MealPreference mealPreference, @PathVariable String slug, @PathVariable long idUser)
         throws Exception{
        // On vérifie que le poll et le User existe
        User user = userRepository.findById(idUser);

        if (!(this.doesPollExists(slug)) || (user == null)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On set le user dans la meal preference
        mealPreference.setUser(user);
        // On ajoute la meal preference dans le poll
        long idMp = mealPreference.getId();
        String url = "http://poll:8083/api/polls/slug/"+slug+"/mealpreferences/"+idMp;
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

        // On save la meal preference
        mealPreferenceRepository.persist(mealPreference);
        
        return new ResponseEntity<>(mealPreference, HttpStatus.CREATED);
    }
}



