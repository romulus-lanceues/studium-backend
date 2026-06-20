package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.dto.response.bundled_response.*;
import com.lancea.studium.studium_api.dto.response.paged_response.PagedResponse;
import com.lancea.studium.studium_api.dto.response.single_response.*;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.service.DataService;
import com.lancea.studium.studium_api.service.FocusRecommendationService;
import com.lancea.studium.studium_api.service.SessionService;
import com.lancea.studium.studium_api.shared.enums.BreakDownPeriod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/data")
public class DataController {

    private final DataService dataService;
    private final SessionService sessionService;
    private final FocusRecommendationService focusRecommendationService;


    public DataController(DataService dataService, SessionService sessionService, FocusRecommendationService focusRecommendationService){
        this.dataService = dataService;
        this.sessionService = sessionService;
        this.focusRecommendationService = focusRecommendationService;
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
    public ResponseEntity<List<CompletedSessionsPerSubject>> retrieveCompletedSessionsByTimePeriod(@AuthenticationPrincipal UserDetails userDetails,
                                                                                                   @RequestParam (defaultValue = "#{T(java.time.YearMonth).now()") YearMonth month){
        return ResponseEntity.ok(sessionService.getCompletedSessionsByTimePeriod(userDetails, month));
    }

    @GetMapping("/analytics/recommendation")
    public ResponseEntity<FocusRecommendationDTO> retrieveRecommendation(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(focusRecommendationService.retrieveRecommendation(userDetails));
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<SummaryStatsDTO> retrieveUserSummary(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getUsersSummaryStats(userDetails));
    }

    @GetMapping("/analytics/peak-hours")
    public ResponseEntity<PeakHoursDTO> retrievePeakHours(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestParam(defaultValue = "90") int days){
        return ResponseEntity.ok(dataService.getUserPeakHours(userDetails, days));
    }

    @GetMapping("/analytics/productivity-score")
    public ResponseEntity<ProductivityScoreDTO> retrieveProductivityScore(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getUserProductivityScore(userDetails));
    }

    @GetMapping("/analytics/breakdown")
    public ResponseEntity<BreakDownDTO> retrieveBreakdown(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestParam(defaultValue = "WEEKLY")BreakDownPeriod period){
        return ResponseEntity.ok(dataService.getBreakDown(userDetails, period));
    }

    @GetMapping("analytics/goals")
    public ResponseEntity<GoalProgressDTO> retrieveUserWeeklyGoals(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(dataService.getUserWeeklyGoals(userDetails));
    }


}
