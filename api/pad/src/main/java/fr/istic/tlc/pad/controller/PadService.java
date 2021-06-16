package fr.istic.tlc.pad.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import net.gjerull.etherpad.client.EPLiteClient;

@Service
public class PadService {

	@Autowired
	PadRepository repository;

	String padUrl = "http://localhost:9001/";

	String externalPadUrl = "http://etherpad:9001/";

	String apikey = "19d89ca52bc0fa4f19d6325464d9d7a032649b9fa68c111514627081e2784b4a";
	EPLiteClient client;

	public List<Pad> getPads() {
		List<Pad> padList = repository.findAll();

		if (padList.size() > 0) {
			return padList;
		} else {
			return new ArrayList<Pad>();
		}
	}

	public Pad getPadById(long id) throws Exception {
		Optional<Pad> pad = repository.findById(id);

		if (pad.isPresent()) {
			return pad.get();
		} else {
			throw new Exception("No pad exist for given id");
		}
	}

//	
//	
//  public String getPadId(long id) throws Exception {
//	  Optional<Pad> pad = repository.findById(id);
//
//		if (pad.isPresent()) {
//			return pad.get().getPadId();
//		} else {
//			throw new Exception("No pad exist for given id");
//		}
//  }
//  
	public void deletePadById(long id) throws Exception {
		Optional<Pad> pad = repository.findById(id);

		if (pad.isPresent()) {
			repository.deleteById(id);
		} else {
			throw new Exception("No pad exist for given id");
		}
	}

//
//	public Pad initPad(String pollTitle, String pollLocalisation, String pollDescription) {
//
//			Pad newPad = new Pad( pollTitle,  pollLocalisation,  pollDescription);
//			repository.save(newPad);
//			return newPad;
//		}
//		
//	
//	
	public Pad updatePad(String pollTitle, String pollLocalisation, String pollDescription, long id) throws Exception {
		Optional<Pad> pad = repository.findById(id);

		if (pad.isPresent()) {
			Pad pad2 = pad.get();
			String padId = pad2.getPadId();
			System.out.println("Pad ID = "+padId);
			String ancientPad = (String) client.getText(padId).get("text");
			if (pollTitle != null) {
				ancientPad = ancientPad.replaceFirst(pad2.getPadURL(), pollTitle);
				pad2.setTitre(pollTitle);
			}
			if (pollLocalisation != null) {
				ancientPad = ancientPad.replaceFirst(pad2.getLocalisation(), pollLocalisation);
				pad2.setLocalisation(pollLocalisation);
			}
			if (pollDescription != null) {
				ancientPad = ancientPad.replaceFirst(pad2.getDescription(), pollDescription);
				pad2.setDescription(pollDescription);
			}
			repository.save(pad2);
			client.setText(padId, ancientPad);
			return pad2;
		} else {
			throw new Exception("No pad exist for given id");
		}
	}

//	
	public Pad createPad(String pollTitle, String pollLocalisation, String pollDescription) {
		String padKey = Utils.generateSlug(15);
		if (client == null) {
			client = new EPLiteClient(externalPadUrl, apikey);
		}
		client.createPad(padKey);
		initPad(pollTitle, pollLocalisation, pollDescription, client, padKey);
		Pad pad = new Pad(padUrl + "p/" + padKey,pollTitle,pollLocalisation,pollDescription);
		repository.save(pad);
		return pad;
	}

	private static void initPad(String pollTitle, String pollLocation, String pollDescription, EPLiteClient client,
			String padKey) {
		final String str = pollTitle + '\n' + "Localisation : " + pollLocation + '\n' + "Description : "
				+ pollDescription + '\n';
		client.setText(padKey, str);
	}

}
