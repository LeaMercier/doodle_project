package fr.istic.tlc.poll.entity;

import static fr.istic.tlc.poll.Utils.generateSlug;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.ElementCollection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.concurrent.Future;

import javax.persistence.Table;

@Entity
@Table(name = "poll", schema = "public")
public class Poll{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String localisation;
    private String detail;
    private boolean has_meal;
    private String slug = generateSlug(24);
    private String slugAdmin = generateSlug(24);
    private String tlkURL = "https://tlk.io/"+generateSlug(12);
    public boolean clos = false;
    

	@CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    //@OneToMany(cascade = CascadeType.ALL)
    //@JoinColumn(name = "pollID")
    //@OrderBy("startDate ASC")
    @Column
    @ElementCollection(targetClass = Long.class)
    List<Long> pollChoices = new ArrayList<Long>();

    
    //@OneToOne(cascade = {CascadeType.PERSIST,CascadeType.REMOVE,CascadeType.REFRESH})
    Long selectedChoice;

    
    @OneToMany(cascade =  {CascadeType.PERSIST,CascadeType.REMOVE,CascadeType.REFRESH})
    @JoinColumn(name = "pollID")
    List<Comment> pollComments = new ArrayList<>();

    @Column
    @ElementCollection(targetClass = Long.class)
    List<Long> pollMealPreferences = new ArrayList<Long>();

    private String padURL;
    private long padId;

    public Poll(){}

    public Poll(String title, String location, String description, boolean has_meal, List<Long> pollChoices) {
        this.title = title;
        this.localisation = location;
        this.detail = description;
        this.has_meal = has_meal;
        this.pollChoices = pollChoices;
    }

    public void addChoice(Long choice){
        this.pollChoices.add(choice);
    }

    public void removeChoice(Long choice){
        this.pollChoices.remove(choice);
    }

    public void setPadId(long padId) {this.padId = padId;}

    public long getPadId() {return this.padId;}

    public void addComment(Comment comment){ this.pollComments.add(comment);}

    public void removeComment(Comment comment){ this.pollComments.remove(comment);}

    public void addMealPreference(Long mealPreference){ this.pollMealPreferences.add(mealPreference);}

    public void removeMealPreference(Long mealPreference){ this.pollMealPreferences.remove(mealPreference);}

    public Long getId() {
        return id;
    }

    public String getTlkURL() {
        return tlkURL;
    }

    public void setTlkURL(String tlkURL) {
        this.tlkURL = tlkURL;
    }

    public List<Comment> getPollComments() {
        return pollComments;
    }

    public List<Long> getPollMealPreferences() {
        return pollMealPreferences;
    }

    public void setPollMealPreferences(List<Long> pollMealPreferences) {
        this.pollMealPreferences = pollMealPreferences;
    }

    public void setPollComments(List<Comment> pollComments) {
        this.pollComments = pollComments;
    }

    public void setId(Long id) {
        this.id = id;
    }

   public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return localisation;
    }

    public void setLocation(String location) {
        this.localisation = location;
    }

    public String getDescription() {
        return detail;
    }

    public void setDescription(String description) {
        this.detail = description;
    }

    public boolean isHas_meal() {
        return has_meal;
    }

    public void setHas_meal(boolean has_meal) {
        this.has_meal = has_meal;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSlugAdmin() {
        return slugAdmin;
    }

    public void setSlugAdmin(String slugAdmin) {
        this.slugAdmin = slugAdmin;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Long> getPollChoices() {
        return pollChoices;
    }

    public void setPollChoices(List<Long> pollChoices) {
        this.pollChoices = pollChoices;
    }

    public Long getSelectedChoice() {
		return selectedChoice;
	}

	public void setSelectedChoice(Long selectedChoice) {
		this.selectedChoice = selectedChoice;
	}

	public String getPadURL() {
        return this.padURL;
    }

    
    public void setPadURL(String padURL) {
        this.padURL=padURL;
    }
    
    public boolean isClos() {
		return clos;
	}

	public void setClos(boolean clos) {
		this.clos = clos;
	}


    @Override
    public String toString() {
        return "Poll{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", location='" + localisation + '\'' +
                ", description='" + detail + '\'' +
                ", has_meal=" + has_meal +
                ", createdAt=" + createdAt +
                '}';
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Long> retrieveAllUserIds() throws Exception {
        Set<Long> userIds = new LinkedHashSet<Long>();
        
        //On verifie que le poll contient des choix
        if (!this.getPollChoices().isEmpty()) {
            for (long choice : this.getPollChoices()) {

                //On recupère la liste des userId que contient chaque choice
                List<Long> choiceUserIds = this.retrieveChoiceUsers(choice);


                if (!choiceUserIds.isEmpty()) {
                    userIds.addAll(choiceUserIds);
                }
            }
        }

        return userIds;

    }

    public List<Long> retrieveChoiceUsers(long choice) throws Exception {
        String url = "http://planning:8082/api/planning/" + choice + "/userids";
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        HttpGet request = new HttpGet(url);

        Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getEntity().writeTo(out);
        String responseString = out.toString().substring(1, out.toString().length() - 1);
        List<String> tmpList = Arrays.asList(responseString.split(","));
        List<Long> choiceUserIds = new ArrayList<Long>();
        for (String tmp : tmpList) {
            if (!tmp.isEmpty())
                choiceUserIds.add(Long.parseLong(tmp));
        }

        out.close();
        client.close();

        return choiceUserIds;
    }

    public List<String> retrieveAllUsers() throws Exception {
        List<String> users = new ArrayList<String>();
        Set<Long> userIds = this.retrieveAllUserIds();

            //On recupère les users sous format Json
            for (Long id : userIds) {
                String url = "http://participant:8081/api/users/" + id;
                CloseableHttpAsyncClient client2 = HttpAsyncClients.createDefault();
                client2.start();
                HttpGet request = new HttpGet(url);

                Future<HttpResponse> future = client2.execute(request, null);
                HttpResponse response = future.get();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                users.add(out.toString());

                out.close();
                client2.close();
            }

         
        return users;
    }
    
    public List<String> retrieveAllChoices() throws Exception {
        List<String> choices = new ArrayList<String>();
        List<Long> choiceIds = this.pollChoices;

        // On recupère les users sous format Json
        for (Long id : choiceIds) {
           
            choices.add(this.retrieveChoice(id));

        }

        return choices;
    }

    public String retrieveChoice(long choiceId) throws Exception {
        String url = "http://planning:8082/api/choices/" + choiceId;
        CloseableHttpAsyncClient client2 = HttpAsyncClients.createDefault();
        client2.start();
        HttpGet request = new HttpGet(url);

        Future<HttpResponse> future = client2.execute(request, null);
        HttpResponse response = future.get();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getEntity().writeTo(out);
        String choice = out.toString();

        out.close();
        client2.close();
        
        return choice;
    }
}
