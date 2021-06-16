package fr.istic.tlc.pad.controller;

import javax.annotation.Generated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.Table;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "pad")
public class Pad {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name="idPad")
	private long idPad;

	@Column(name = "urlPad")
	private String urlPad;

	@Column(name = "Titre")
	private String titre;
	@Column(name = "Localisation")
	private String localisation;
	@Column(name = "Description")
	private String description;

	public Pad(String padKey,String pollTitle, String pollLocation, String pollDescription) {
		this.localisation = pollLocation;
		this.titre=pollTitle;
		this.description=pollDescription;
		this.urlPad =padKey;
		
	}

	
	public Pad(String urlPad) {
		this.urlPad = urlPad;
	}



	public String getPadId() {
		return getPadURL().substring(getPadURL().lastIndexOf('/') + 1);
	}

	public String getPadURL() {
		return urlPad;
	}

	public Pad() {

	}

	public long getId() {
		return idPad;
	}
	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getLocalisation() {
		return localisation;
	}

	public void setLocalisation(String localisation) {
		this.localisation = localisation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
