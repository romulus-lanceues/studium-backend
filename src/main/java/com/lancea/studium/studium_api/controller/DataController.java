package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.dto.response.SessionOverviewResponse;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/data")
public class DataController {

    private DataService dataService;

    public DataController(DataService dataService){
        this.dataService = dataService;
    }

    @GetMapping("/completed-sessions")
    public ResponseEntity<List<StudySession>> retrieveCompletedSessionsForUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getAllCompletedSessionsForUser(userDetails));
    }

    @GetMapping("/recent-sessions")
    public ResponseEntity<List<StudySession>> retrieveRecentSessions(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getRecentCompletedSessions(userDetails));
    }

    @GetMapping("/cancelled-sessions")
    public ResponseEntity<List<StudySession>> retrieveCancelledSessionsForUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getCancelledSessions(userDetails));
    }

    @GetMapping("/week/overview")
    public ResponseEntity<SessionOverviewResponse> retrieveSessionOverviewForAWeek(@AuthenticationPrincipal UserDetails userDetails){

        SessionOverviewResponse results = dataService.retrieveSessionsForThisWeek(userDetails);

        return ResponseEntity.ok(results);
    }
}
