package fr.istic.tlc.participant.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.ElementCollection;
import javax.persistence.Column;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "participant", schema = "public")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;
    private String mail;
    private String icsurl;

    @Column
    @ElementCollection(targetClass=Long.class)
    List<Long> userChoices = new ArrayList<Long>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<MealPreference> userMealPreferences = new ArrayList<>();

    public User(){}

    public User(String username) {	
        this.username = username;
    }

    public void addChoiceId(Long choiceId){
        this.userChoices.add(choiceId);
    }
    public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}


    public String getIcsurl() {
		return icsurl;
	}

	public void setIcsurl(String icsurl) {
		this.icsurl = icsurl;
	}

	public void removeChoice(Long choiceId){
        this.userChoices.remove(choiceId);
    }


    public void addMealPreference (MealPreference mealPreference) {this.userMealPreferences.add(mealPreference);}

    public void removeMealPreference (MealPreference mealPreference) {this.userMealPreferences.remove(mealPreference);}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Long> getUserChoices() {
        return userChoices;
    }

    public List<MealPreference> getUserMealPreferences() {
        return userMealPreferences;
    }

    public void setUserMealPreferences(List<MealPreference> userMealPreferences) {
        this.userMealPreferences = userMealPreferences;
    }

    public void setUserChoices(List<Long> userChoices) {
        this.userChoices = userChoices;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
