package ch.nblotti.pheidippides.controller;


import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@GeneratedExcludeJacocoTestCoverage
@RestController
@RequestMapping("/ping")
public class PingController {


    private final DateTimeFormatter format1;


    private final StateMachine<STATES, EVENTS> stateMachine;

    public PingController(StateMachine<STATES, EVENTS> stateMachine,DateTimeFormatter format1){
        this.format1 = format1;
        this.stateMachine = stateMachine;
    }
    @GetMapping
    public ResponseEntity<String> ping(@PathParam(value = "key") String key) {

        if (stateMachine.isComplete())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("%s - %s", key == null ? "" : key, LocalDateTime.now().format(format1)));

        return ResponseEntity.ok(String.format("%s - %s", key == null ? "" : key, LocalDateTime.now().format(format1)));


    }


}



