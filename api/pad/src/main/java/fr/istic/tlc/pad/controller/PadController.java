package fr.istic.tlc.pad.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/pads")
public class PadController {
	long n = 0;
	@Autowired
	PadService service;
	

	@GetMapping
	public ResponseEntity<Object> getPads() {
		List<Pad> list = service.getPads();

		return new ResponseEntity<>(list, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/hello")
	public ResponseEntity<Object> get() {
		service.createPad("pollTitle" + n, "pollLocation" + n, "pollDescription" + n);
		n++;
		return new ResponseEntity<>("Here is pad service ! ", HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Pad> getPadById(@PathVariable("id") long id) throws Exception {
		Pad entity = service.getPadById(id);
		return  new ResponseEntity<Pad>(entity, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public void deletePad(@PathVariable("id") long id) throws Exception {
		service.deletePadById(id);
	}

//	@PostMapping
//	@ResponseBody
//	public ResponseEntity<Object> initPad(@Valid @RequestParam("pollTitle") String pollTitle,
//			@Valid @RequestParam("pollLocalisation") String pollLocalisation,
//			@Valid @RequestParam("pollDescription") String pollDescription) {
//		Pad pad = service.initPad(pollTitle, pollLocalisation, pollDescription);
//		return new ResponseEntity<>(pad, new HttpHeaders(), HttpStatus.OK);
//	}

	@PutMapping("/{id}")
	public Pad updatePad(@Valid @RequestParam("pollTitle") String pollTitle,
			@Valid @RequestParam("pollLocalisation") String pollLocalisation,
			@Valid @RequestParam("pollDescription") String pollDescription, @PathVariable long id) throws Exception {
		Pad pad = service.updatePad(pollTitle, pollLocalisation, pollDescription, id);
		return pad;

	}

	@PostMapping
	@ResponseBody
	public ResponseEntity<Object> createPad(@Valid @RequestParam("pollTitle") String pollTitle,
			@Valid @RequestParam("pollLocalisation") String pollLocalisation,
			@Valid @RequestParam("pollDescription") String pollDescription) {
		Pad pad = service.createPad(pollTitle, pollLocalisation, pollDescription);
		return new ResponseEntity<>(pad, new HttpHeaders(), HttpStatus.OK);
	}

}
