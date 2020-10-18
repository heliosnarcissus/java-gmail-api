package com.gmailapijava.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/")
public class GmailRestController {

		@GetMapping("/hello")
		//@CrossOrigin(origins = "http://localhost:9005")
		public String hello() {
			return "gmail-api-java: GmailRestController";	
		}
		/***
		//GET full list of actors 
		@GetMapping(value = "/actors", produces = "application/json")
		@CrossOrigin(origins = "http://localhost:8080")
		public List<Actor> selectAllActors(){
				return actorMapper.selectAllActors();
	    }
		//GET specific actor by actor_id 
		@CrossOrigin(origins = "*", allowedHeaders = "*")
		@GetMapping(value = "/actors/{id}", produces = "application/json")
		public List<Actor> selectActorById(@PathVariable(name="id") Integer actor_id){
		     return actorMapper.selectActorById(actor_id);
		}
		//INSERT a new actor on the database
		@CrossOrigin(origins = "*", allowedHeaders = "*")
		@PostMapping(value="/actors", consumes = "application/json", produces = "application/json")
		public int insertNewActor(@RequestBody Actor actor) {
			return actorMapper.insertNewActor(actor);
		}
		//UPDATE a specific entry in the database, later, add the no-change-id functionality
		@CrossOrigin(origins = "*", allowedHeaders = "*")
		@PutMapping(value="/actors/{id}", consumes = "application/json", produces = "application/json")
		public int updateActorById(@RequestBody Actor actor, @PathVariable(name="id") Integer actor_id) {
			return actorMapper.updateActorById(actor);
		}
		//DELETE actor entry from the database
		@CrossOrigin(origins = "*", allowedHeaders = "*")
		@DeleteMapping(value="/actors/{id}")
		public int deleteActorById(@PathVariable(name="id") Integer actor_id) {
			return actorMapper.deleteActorById(actor_id);
		}***/
}
