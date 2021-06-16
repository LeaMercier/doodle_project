package fr.istic.tlc.planning.resources;

import java.util.*;


import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.istic.tlc.planning.entity.Choice;
import fr.istic.tlc.planning.repository.ChoiceRepository;
import fr.istic.tlc.planning.dto.EventDTO;
import fr.istic.tlc.planning.dto.EventDTOAndSelectedChoice;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.util.MapTimeZoneCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.io.ByteArrayOutputStream;
import java.io.*;
import java.util.Arrays;

@RestController
public class PlanningResources {
	

    @Autowired
    ChoiceRepository choiceRepository;

    /**
	 * 
	 * @param slug
	 * @return
	 * @throws Exception
	 */
	private boolean doesPollExists(String slug) throws Exception{
        
        String url = "http://poll:8083/api/poll/"+slug;

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

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	/**
	 * 
	 * @param slug
	 * @param ics
	 * @return
	 * @throws IOException
	 * @throws ParserException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws MessagingException
	 */
	@GetMapping("/planning/polls/{slug}/{planning}")
	@javax.ws.rs.Produces(MediaType.APPLICATION_JSON)
	public EventDTOAndSelectedChoice parseCalendartoAppointments(@PathParam("slug") String slug,
			@PathParam("planning") String ics)
			throws IOException, ParseException, InterruptedException, ExecutionException, Exception {
		EventDTOAndSelectedChoice result = new EventDTOAndSelectedChoice();
		List<EventDTO> appointments = new ArrayList<>();
		List<Long> selectedChoices = new ArrayList<>();
		result.setEventdtos(appointments);
		result.setSelectedChoices(selectedChoices);

		// Get Poll
		Date minDate = new Date();
		if (!doesPollExists(slug)) {
			// Get minimal date for Poll to filter ics
			Date debutChoice = choiceRepository
								.findById(this.getPollChoices(slug).get(0))
								.getstartDate();

			if (this.getPollChoices(slug).size() > 0
				&& minDate.after(debutChoice))
				minDate = debutChoice;
		}

		// Get user to get its ICS
		// User u = this.userRep.find("mail", usermail).firstResult();
		byte[] decodedBytes = Base64.getDecoder().decode(ics);
		String decodedString = new String(decodedBytes);

		if (decodedString != null && !"".equals(decodedString)) {

			System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
			CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
			client.start();
			HttpGet request = new HttpGet(decodedString);

			Future<HttpResponse> future = client.execute(request, null);
			HttpResponse response = future.get();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String responseString = out.toString();
			out.close();
			client.close();

			// Parse result
			StringReader sin = new StringReader(responseString);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(sin);
			ComponentList<CalendarComponent> events = calendar.getComponents(Component.VEVENT);
			List<Choice> choices = new ArrayList<Choice>();
			if (!doesPollExists(slug)){
				List<Long> choiceIds = this.getPollChoices(slug);
				
				for (Long id : choiceIds){
					choices.add(choiceRepository.findById(id));
				}
			}

			// Create Event to draw
			java.util.Calendar calEnd = java.util.Calendar.getInstance();
			calEnd.setTime(new Date());
			calEnd.add(java.util.Calendar.YEAR, 1);
			DateTime start = new DateTime(minDate);
			DateTime end = new DateTime(calEnd.getTime());
			for (CalendarComponent event : events) {

				Period period = new Period(start, end);
				PeriodList list = event.calculateRecurrenceSet(period);
				for (Period p1 : list) {
					if (minDate.before(p1.getStart())) {
						EventDTO a = new EventDTO();
						a.setStartDate(p1.getStart());
						a.setEndDate(p1.getEnd());
						if (((VEvent) event).getSummary() != null)
							a.setDescription(((VEvent) event).getSummary().getValue());

						// Si intersection ajoute l'ID du choice comme ID selected
						// https://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
						for (Choice choice : choices) {
							if (Utils.intersect(choice.getstartDate(), choice.getendDate(), p1.getStart(),
									p1.getEnd())) {
								if (!selectedChoices.contains(choice.getId())) {
									selectedChoices.add(choice.getId());
								}
							}
						}
						appointments.add(a);
					}

				}
			}

		}
		return result;
	}
		
}
