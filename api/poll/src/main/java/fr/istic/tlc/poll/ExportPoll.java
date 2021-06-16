package fr.istic.tlc.poll;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import fr.istic.tlc.poll.entity.Poll;
import fr.istic.tlc.poll.repository.PollRepository;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.json.simple.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

@RestController
@RequestMapping("/api")
public class ExportPoll {
	@Autowired
	PollRepository pollRepository;

	String EXCEL_FILE_LOCATION = "/tmp/excelFiles";


	@GetMapping("/polls/{slug}/results")
	public Response downloadResultsExcel(@PathVariable String slug) throws IOException {
		Poll poll = pollRepository.findBySlug(slug);
		if (poll == null) {
			return null;
		}
		String filePath = createExcelFile(poll, slug);
		return getHttpEntityToDownload(filePath, "vnd.ms-excel");
	}

	/*
	  @RequestMapping(value = "/polls/{slug}/print", method = RequestMethod.GET,
	  produces = APPLICATION_PDF)
	  public @ResponseBody HttpEntity<byte[]>
	  downloadResultsPdf(@PathVariable String slug) throws IOException {
		   Poll poll = pollRepository.findBySlug(slug);
		   if (poll == null) { 
			   return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			 } 
			String filePath = "./Test.xls";
	  		Utils.excel2pdf();
	  		convertToPdf(filePath);
	
			return getHttpEntityToDownload(filePath,"pdf"); }
	 */

	/*
	 * private String convertToPdf(String filePath){ return "";
	 * 
	 * }
	 */

	static int beginningColumnCell = 0;
	static int beginningRowCell = 3;
	static int fontSize = 9;
	static Colour borderColour = Colour.WHITE;

	private String createExcelFile(Poll poll, String slug) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy-HH.mm.ss");
		Date date = new Date();
		String fileName = EXCEL_FILE_LOCATION + File.separator + slug + "-" + dateFormat.format(date) + ".xls";

		File folder = new File(EXCEL_FILE_LOCATION);
		if (!folder.exists()) {
			folder.mkdir();
		}

		// Create an Excel file
		WritableWorkbook Wbook = null;
		try {
			System.out.println("Création du fichier");
			// Create an Excel file in the file location
			File file = new File(fileName);

			if (!file.createNewFile()) {
				System.out.println("Erreur lors de la création du fichier");
			}
			Wbook = Workbook.createWorkbook(file);

			// Create an Excel sheet
			WritableSheet mainSheet = Wbook.createSheet("SONDAGE", 0);
			Wbook.setColourRGB(Colour.BLUE, 53, 37, 230);

			// Format objects
			
			WritableFont fontTitle = new WritableFont(WritableFont.TAHOMA, 16, WritableFont.BOLD);
			fontTitle.setColour(Colour.BLUE);
			WritableCellFormat formatTitle = new WritableCellFormat(fontTitle);

			Label label;
			label = new Label(0, 0, "Sondage \"" + poll.getTitle() + "\"", formatTitle);
			mainSheet.addCell(label);
			label = new Label(0, 1, "http://localhost:8083/api/polls/" + poll.getSlug());
			mainSheet.addCell(label);

			// On récupere les users qui ont voté dans ce sondage
			List<String> users = poll.retrieveAllUsers();

//			// On ecrit les users sur la première colonne
			writeUsers(poll, Wbook, users);

//			// On ecrit les choix avec les votes de chaque users
			writeChoices(poll, Wbook, users);

			System.out.println("Enregistrement du fichier");
			// On ecrit les donnée du workbook dans un format excel
			Wbook.write();

		} catch (Exception e) {
			System.out.println("Erreur lors de la création du fichier :( " + e.toString());
			e.printStackTrace();
		} finally {

			if (Wbook != null) {
				try {
					Wbook.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return fileName;
	}

//	private List<String> retrieveUsers(Poll poll) {
//		List<User> users = new ArrayList<>();
//		// On parcours les choix du poll pour récupérer les users ayant voté
//		if (!poll.getPollChoices().isEmpty()) {
//			for (Choice choice : poll.getPollChoices()) {
//				if (!choice.getUsers().isEmpty()) {
//					for (User user : choice.getUsers()) {
//						// On vérifie que le user ne soit pas déjà dans la liste
//						if (!users.contains(user)) {
//							users.add(user);
//						}
//					}
//				}
//			}
//		}
//		return users;
//	}

	private void writeChoices(Poll poll, WritableWorkbook Wbook, List<String> users) 
	throws Exception {
		Label label;
		Number number;
		WritableSheet mainSheet = Wbook.getSheet(0);

		List<String> choices = poll.retrieveAllChoices();
		// List<Choice> choices =
		// choiceRepository.findAll(Sort.by(Sort.Direction.ASC,"startDate"));

		// Format objects
		WritableCellFormat formatVoteYes = new WritableCellFormat();
		formatVoteYes.setAlignment(Alignment.CENTRE);
		formatVoteYes.setVerticalAlignment(VerticalAlignment.CENTRE);
		formatVoteYes.setBorder(Border.ALL, BorderLineStyle.THIN, borderColour);
		formatVoteYes.setBackground(Colour.LIGHT_GREEN);
		WritableFont fontVoteYes = new WritableFont(WritableFont.TAHOMA, fontSize, WritableFont.NO_BOLD);
		fontVoteYes.setColour(Colour.BLACK);
		formatVoteYes.setFont(fontVoteYes);
		// Format objects
		Wbook.setColourRGB(Colour.LIGHT_ORANGE, 255, 195, 195);
		WritableCellFormat formatVoteNo = new WritableCellFormat();
		formatVoteNo.setAlignment(Alignment.CENTRE);
		formatVoteNo.setVerticalAlignment(VerticalAlignment.CENTRE);
		formatVoteNo.setBorder(Border.ALL, BorderLineStyle.THIN, borderColour);
		formatVoteNo.setBackground(Colour.LIGHT_ORANGE);
		WritableFont fontVoteNo = new WritableFont(WritableFont.TAHOMA, fontSize, WritableFont.NO_BOLD);
		fontVoteNo.setColour(Colour.BLACK);
		formatVoteNo.setFont(fontVoteNo);

		// On ecrit les colonnes des choix
		List<Long> choiceIds = poll.getPollChoices();
		for (int i = 0; i < choices.size(); i++) {
			mainSheet.setColumnView(1 + beginningColumnCell + i, 14);
			// On ecrit la date
			writeChoiceDate(Wbook, choices, i);

		
			// On ecrit les votes
			List<Long> listUsersVotes = poll.retrieveChoiceUsers(choiceIds.get(i));

			//On recupere les identifiants des users
			JSONParser parser = new JSONParser();
			List<Long> userIds = new ArrayList<Long>();
			try {
				for (String user : users) {
					Object obj = parser.parse(user);

					JSONObject obj2 = (JSONObject) obj;
					userIds.add(Long.parseLong((String)obj2.get("id")));
				
				}
			} catch (ParseException pe) {

				System.err.println("erreur de parsing à la position: " + pe.getPosition());
			}

			for (int x = 0; x < userIds.size(); x++) {
				if (listUsersVotes.contains(userIds.get(x))) {
					label = new Label(1 + beginningColumnCell + i, 3 + beginningRowCell + x, "OK", formatVoteYes);
				} else {
					label = new Label(1 + beginningColumnCell + i, 3 + beginningRowCell + x, "-", formatVoteNo);
				}
				mainSheet.addCell(label);
			}

			// on ecrit le nombre total de vote pour le choix
			number = new Number(1 + beginningColumnCell + i, 3 + beginningRowCell + users.size(),
					listUsersVotes.size());
			mainSheet.addCell(number);
			
		}
	}

	private void writeChoiceDate(WritableWorkbook Wbook, List<String> choices, int i) throws jxl.write.WriteException {
		Label label;
		WritableSheet mainSheet = Wbook.getSheet(0);
		String month[] = { "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Aout", "Septembre",
				"Novembre", "Décembre" };
		String dayOfWeek[] = { "Lun.", "Mar.", "Mer.", "Jeu.", "Ven.", "Sam.", "Dim." };
		Wbook.setColourRGB(Colour.BLUE, 53, 37, 230);
		// Format objects
		WritableCellFormat formatDate = new WritableCellFormat();
		formatDate.setAlignment(Alignment.CENTRE);
		formatDate.setVerticalAlignment(VerticalAlignment.CENTRE);
		formatDate.setBorder(Border.ALL, BorderLineStyle.THIN, borderColour);
		formatDate.setBackground(Colour.BLUE);
		WritableFont fontDate = new WritableFont(WritableFont.TAHOMA, fontSize, WritableFont.NO_BOLD);
		fontDate.setColour(Colour.WHITE);
		formatDate.setFont(fontDate);

		// On recupère la date de début
		String choice = choices.get(i);

		//On parse le choice pour recuperer les dates

		JSONParser parser = new JSONParser();
		Date debutChoice = new Date();
		Date finChoice = new Date();
		try {
			
			Object obj = parser.parse(choice);

				JSONObject obj2 = (JSONObject) obj;
				debutChoice = (Date) obj2.get("startDate");
				finChoice = (Date) obj2.get("endDate");

		} catch (ParseException pe) {

			System.err.println("erreur de parsing à la position: " + pe.getPosition());
		}

		
		Date startDate = debutChoice;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int startYear = calendar.get(Calendar.YEAR);
		String startMonth = month[calendar.get(Calendar.MONTH)];
		int startDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		String startDayOfWeek = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		int startHourInt = calendar.get(Calendar.HOUR_OF_DAY);
		String startHour = (startHourInt < 10 ? "0" : "") + startHourInt;
		int startMinuteInt = calendar.get(Calendar.MINUTE);
		String startMinute = (startMinuteInt < 10 ? "0" : "") + startMinuteInt;
		// On recupère la date de fin
		Date endDate = finChoice;
		calendar.setTime(endDate);
		int endHourInt = calendar.get(Calendar.HOUR_OF_DAY);
		String endHour = (endHourInt < 10 ? "0" : "") + endHourInt;
		int endMinuteInt = calendar.get(Calendar.MINUTE);
		String endMinute = (endMinuteInt < 10 ? "0" : "") + endMinuteInt;

		label = new Label(1 + beginningColumnCell + i, beginningRowCell, startMonth + " " + startYear, formatDate);
		mainSheet.addCell(label);
		label = new Label(1 + beginningColumnCell + i, 1 + beginningRowCell, startDayOfWeek + " " + startDayOfMonth,
				formatDate);
		mainSheet.addCell(label);
		label = new Label(1 + beginningColumnCell + i, 2 + beginningRowCell,
				startHour + ":" + startMinute + " - " + endHour + ":" + endMinute, formatDate);
		mainSheet.addCell(label);
	}

	private void writeUsers(Poll poll, WritableWorkbook Wbook, List<String> users) throws jxl.write.WriteException {
		Label label;

		WritableSheet mainSheet = Wbook.getSheet(0);
		mainSheet.setColumnView(beginningColumnCell, 25);

		// Format objects
		WritableCellFormat formatUser = new WritableCellFormat();
		formatUser.setAlignment(Alignment.RIGHT);
		formatUser.setVerticalAlignment(VerticalAlignment.CENTRE);
		formatUser.setBorder(Border.ALL, BorderLineStyle.THIN, borderColour);

		formatUser.setBackground(Colour.GRAY_25);
		WritableFont fontUser = new WritableFont(WritableFont.TAHOMA, fontSize, WritableFont.NO_BOLD);
		fontUser.setColour(Colour.BLACK);
		formatUser.setFont(fontUser);

		//Recuperer les username des user
		JSONParser parser = new JSONParser();
		List<String> userNames = new ArrayList<String>();
		try {
			for (String user : users) {
				Object obj = parser.parse(user);

				JSONObject obj2 = (JSONObject) obj;
				userNames.add((String) obj2.get("username"));

			}
		} catch (ParseException pe) {

			System.err.println("erreur de parsing à la position: " + pe.getPosition());
		}

		// On ecrit la premier colonne avec users et label "Nombre"
		for (int i = 0; i < users.size(); i++) {
			label = new Label(beginningColumnCell, 3 + beginningRowCell + i, userNames.get(i), formatUser);
			mainSheet.addCell(label);
		}
		label = new Label(beginningColumnCell, 3 + beginningRowCell + users.size(), "Nombre");
		mainSheet.addCell(label);
	}

	private Response getHttpEntityToDownload(String filePath, String fileType) throws IOException {
		File file = getFile(filePath);

		// header.set("Content-Disposition", "inline; filename=" + file.getName());
		return Response.ok(((Object) file), MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();
	}

	private File getFile(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("file with path: " + filePath + " was not found.");
		}
		return file;
	}

	/*
	 * @ControllerAdvice public class GlobalExceptionHandler {
	 * 
	 * @ExceptionHandler(value = FileNotFoundException.class) public void
	 * handle(FileNotFoundException ex, HttpServletResponse response) throws
	 * IOException { System.out.println("handling file not found exception");
	 * response.sendError(404, ex.getMessage()); }
	 * 
	 * @ExceptionHandler(value = IOException.class) public void handle(IOException
	 * ex, HttpServletResponse response) throws IOException {
	 * System.out.println("handling io exception"); response.sendError(500,
	 * ex.getMessage()); } }
	 */

}