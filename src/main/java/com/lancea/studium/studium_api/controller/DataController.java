package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.dto.response.bundled_response.DashboardResponse;
import com.lancea.studium.studium_api.dto.response.bundled_response.SubjectsPageResponse;
import com.lancea.studium.studium_api.dto.response.paged_response.PagedResponse;
import com.lancea.studium.studium_api.dto.response.bundled_response.SessionOverviewResponse;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionsPerSubject;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionSummary;
import com.lancea.studium.studium_api.dto.response.single_response.SessionResponse;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.service.DataService;
import com.lancea.studium.studium_api.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/data")
public class DataController {

    private final DataService dataService;
    private final SessionService sessionService;


    public DataController(DataService dataService, SessionService sessionService){
        this.dataService = dataService;
        this.sessionService = sessionService;
    }

    //Dashboard Controller
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> retrieveDataNeededForDashboard(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.retrieveDataNeededForDashboard(userDetails));
    }

    @GetMapping("/completed-sessions")
    public ResponseEntity<List<StudySession>> retrieveCompletedSessionsForUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(sessionService.getAllCompletedSessionsForUser(userDetails));
    }

    @GetMapping("/recent-sessions")
    public ResponseEntity<List<StudySession>> retrieveRecentSessions(){
        return ResponseEntity.ok(sessionService.getRecentCompletedSessions());
    }

    @GetMapping("/cancelled-sessions")
    public ResponseEntity<List<StudySession>> retrieveCancelledSessionsForUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(sessionService.getCancelledSessions(userDetails));
    }

    @GetMapping("/week/overview")
    public ResponseEntity<SessionOverviewResponse> retrieveSessionOverviewForAWeek(@AuthenticationPrincipal UserDetails userDetails){

        SessionOverviewResponse results = sessionService.retrieveSessionsForThisWeek(userDetails);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/session-history")
    public ResponseEntity<PagedResponse<SessionResponse>>getHistory (@AuthenticationPrincipal UserDetails userDetails, @RequestParam int page, @RequestParam int size ){
        return ResponseEntity.ok(dataService.getStudySessionHistory(userDetails, page, size));
    }

    //Subject page controller
    @GetMapping("/subjects-data")
    public ResponseEntity<SubjectsPageResponse> getSubjectsDetails(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getSubjectsAndItsInfos(userDetails));
    }

    //Analytics page controllers

    @GetMapping("/user/total/sessions")
    public ResponseEntity<Long> getUserTotalSession(@AuthenticationPrincipal UserDetails userDetails){

        return ResponseEntity.ok(dataService.getUserSessionCount(userDetails));
    }

    //Subject to change - Will return the date and completed session for that specific date.
    @GetMapping("/analytics/monthly/completed")
    public ResponseEntity<List<CompletedSessionSummary>> retrieveCompletedSessionsThisMonth(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "#{T(java.time.YearMonth).now()") YearMonth month){
        //Call the service
        return ResponseEntity.ok(sessionService.getMonthlyCompletedSessions(userDetails, month));
    }


    @GetMapping("/analytics/subjects/completed-sessions")
    public ResponseEntity<List<CompletedSessionsPerSubject>> retrieveCompletedSessionsByTimePeriod(@AuthenticationPrincipal UserDetails userDetails, @RequestParam (defaultValue = "#{T(java.time.YearMonth).now()") YearMonth month){
        return ResponseEntity.ok(sessionService.getCompletedSessionsByTimePeriod(userDetails, month));
    }

}
