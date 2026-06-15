package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.request.CompletionRequest;
import com.lancea.studium.studium_api.dto.request.StartSessionRequest;
import com.lancea.studium.studium_api.dto.response.paged_response.PagedResponse;
import com.lancea.studium.studium_api.dto.response.bundled_response.SessionOverviewResponse;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionSummary;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionsPerSubject;
import com.lancea.studium.studium_api.dto.response.single_response.SessionResponse;
import com.lancea.studium.studium_api.entity.*;
import com.lancea.studium.studium_api.exception.InvalidSessionStateException;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.exception.UnauthorizedException;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.repository.SubjectRepository;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.shared.enums.SessionType;
import com.lancea.studium.studium_api.shared.interfaces.Streakable;
import com.lancea.studium.studium_api.util.UserDetailsUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SubjectRepository subjectRepository;
    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;

    public SessionService(SubjectRepository subjectRepository, StudySessionRepository studySessionRepository,
                           UserRepository userRepository){
        this.subjectRepository = subjectRepository;
        this.studySessionRepository = studySessionRepository;
        this.userRepository = userRepository;
    }


    public SessionResponse createSession(Long subjectId, StartSessionRequest startSessionRequest, UserDetails userDetails){

        //Retrieve the user id from the security context
        Long userId = ((MyUserDetails) userDetails).getUserId();

        //Check if the subject belongs to the user
        Subject targetSubject = subjectRepository.findByIdAndUserId(subjectId, userId).
                orElseThrow(() -> new  UnauthorizedException("The subject you're trying to create a schedule doesn't belong to you."));

        //Create a new session object and save to the database
        StudySession newSession = StudySession.builder()
                .sessionType(startSessionRequest.sessionType())
                .plannedDurationMinutes(startSessionRequest.plannedDuration())
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .notes(startSessionRequest.notes())
                .subject(targetSubject)
                .user(targetSubject.getUser())
                .build();



        studySessionRepository.save(newSession);

        return SessionResponse.from(newSession);
    }



    public SessionResponse getSession(Long sessionId){

        StudySession session = studySessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session doesn't exist"));

        return  SessionResponse.from(session);
    }

    public Map<String, Object> addInterruption (Long sessionId, UserDetails userDetails){

        Map<String, Object> responseBody = new HashMap<>();

        Long userId = ((MyUserDetails) userDetails).getUserId();

        //Verify if the session belongs to the user
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId).orElseThrow(
                () -> new UnauthorizedException("This subject doesn't exist or you're not authorized to access it"));

        //Update interruption count
        session.setInterruptionsCount(session.getInterruptionsCount() + 1);

        studySessionRepository.save(session);

        responseBody.put("id", session.getId());
        responseBody.put("interruptionsCount", session.getInterruptionsCount());
        responseBody.put("message", "Interruption recorded");

        return responseBody;
    }

    public Map<String, Object> pauseSession (Long sessionId, UserDetails userDetails){

        Map<String, Object> responseBody = new HashMap<>();

        Long userId = ((MyUserDetails) userDetails).getUserId();

        //Verify if the session belongs to the user
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId).orElseThrow(
                () -> new UnauthorizedException("This subject doesn't exist or you're not authorized to access it"));

        if(session.getSessionStatus() != SessionStatus.IN_PROGRESS){
            throw new InvalidSessionStateException("You can only pause a session that's currently in progress");
        }

        session.setSessionStatus(SessionStatus.PAUSED);

        // ===== INCREASE INTERRUPTION TIME (PAUSE = INTERRUPTION) ======

        studySessionRepository.save(session);

        responseBody.put("sessionId", session.getId() );
        responseBody.put("status", session.getSessionStatus());

        return responseBody;

    }

    public Map<String, Object> resumeSession(Long sessionId, UserDetails userDetails){

        Map<String, Object> responseBody = new HashMap<>();

        Long userId = ((MyUserDetails) userDetails).getUserId();

        //Verify if the session belongs to the user
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId).orElseThrow(
                () -> new UnauthorizedException("This subject doesn't exist or you're not authorized to access it"));

        if(session.getSessionStatus() != SessionStatus.PAUSED){
            throw new InvalidSessionStateException("You can only resume paused sessions");
        }

        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        studySessionRepository.save(session);

        responseBody.put("sessionId", session.getId() );
        responseBody.put("status", session.getSessionStatus());

        return responseBody;
    }

    @Transactional
    public Map<String, Object> completeSession(Long sessionId, UserDetails userDetails){
        Map<String, Object> responseBody = new HashMap<>();

        Long requestUserId = ((MyUserDetails) userDetails).getUserId();

        //Get the session with its user and subject
        StudySession session = studySessionRepository.findByIdWithSubjectAndUser(sessionId).orElseThrow(
                () -> new UnauthorizedException("This subject doesn't exist or you're not authorized to access it"));

        //Verify if the user is authorized to make changes for this session
        if(!session.getUser().getId().equals(requestUserId)){
            throw new UnauthorizedException("You're not authorized to access it");
        }

        LocalDateTime completionTime = LocalDateTime.now();

        long elapsedMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), completionTime);

        session.setActualDurationMinutes((int) elapsedMinutes);

        session.setEndTime(completionTime);
        session.setActualDurationMinutes((int) elapsedMinutes);
        session.setSessionStatus(SessionStatus.COMPLETED);

        //Retrieve session subject
        Subject sessionSubject = session.getSubject();
        sessionSubject.increasePomodoroCompleted();
        sessionSubject.increaseStudyTime(session.getActualDurationMinutes());
        configureStreak(sessionSubject);

        //Retrieve session user
        User sessionUser = session.getUser();
        configureStreak(sessionUser);
        configureHighestSession(requestUserId, sessionUser);


        studySessionRepository.save(session);
        subjectRepository.save(sessionSubject);
        userRepository.save(sessionUser);


        //Return a break type response

        //Count the number of completed within 2 hours
        LocalDateTime windowStart = LocalDateTime.now().minusHours(2);
        SessionType breakType = configureBreakType(studySessionRepository.countSpecificSessionWithinSpecificTimePeriod(requestUserId,
                SessionStatus.COMPLETED, windowStart ));


        responseBody.put("break", breakType);
        responseBody.put("sessionId", session.getId() );
        responseBody.put("status", session.getSessionStatus());


        return responseBody;
    }

    private void configureHighestSession (Long userId, User sessionUser) {

        int userCompletedSessionsForToday = studySessionRepository.
                countCompletedSessionsToday(userId, LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(LocalTime.MAX), SessionStatus.COMPLETED).intValue();

        if(userCompletedSessionsForToday > sessionUser.getHighestSession()){
            sessionUser.setHighestSession(userCompletedSessionsForToday);
        }

    }

    private SessionType configureBreakType(long numberOfSessions){
        if(numberOfSessions == 0) return SessionType.SHORT_BREAK;

        if(numberOfSessions % 4 == 0) return SessionType.LONG_BREAK;

        return SessionType.SHORT_BREAK;

    }

    public void configureStreak(Streakable streakable){

        LocalDate today = LocalDate.now();
        if(streakable.getLastSession() != null){

            if(streakable.getLastSession().equals(today)) return;

            if(streakable.getLastSession().plusDays(1).isEqual(today)) {
                streakable.increaseStreak();
                streakable.setLastSession(today);

            } else {
                streakable.setStreak(0);
                streakable.setLastSession(today);
            }

        } else {
            streakable.setLastSession(today);
            streakable.setStreak(1);
        }
    }

    /*
        Database approach without Redis

        private boolean eligibleForALongBreak(){
        //Query for the past completed sessions within 2 hours
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
        List<StudySession> completedSessionsForThePast2Hours = studySessionRepository.findRecentCompletedSessions(cutoff, SessionStatus.COMPLETED);

        //If sessions.length < 4 return false
        return completedSessionsForThePast2Hours.size() == 4;

    }
     */


    public SessionResponse cancelSession(Long sessionId, UserDetails userDetails){
        StudySession session = studySessionRepository.findByIdWithSubjectAndUser(sessionId).orElseThrow( () -> new ResourceNotFoundException("Session not found"));

        //Verify that the session belongs to the user
        Long userId = ((MyUserDetails) userDetails).getUserId();
        if(!userId.equals(session.getUser().getId())){
            throw new UnauthorizedException("This subject doesn't exist or you're not authorized to access it");
        }

        //Check if the session has a correct a status
        if(session.getSessionStatus() != SessionStatus.IN_PROGRESS && session.getSessionStatus() != SessionStatus.PAUSED){
            throw new InvalidSessionStateException("The session must be in progress or paused to be canceled");
        }

        //Calculate the actual minute spent studying
        long minutesSpent = ChronoUnit.MINUTES.between(session.getStartTime(), LocalDateTime.now());

        session.setActualDurationMinutes((int) minutesSpent);
        session.setSessionStatus(SessionStatus.CANCELLED);
        session.setEndTime(LocalDateTime.now());

        studySessionRepository.save(session);

        return SessionResponse.from(session);
    }

    public PagedResponse<SessionResponse> getAllSessionsForToday(UserDetails userDetails, int pageNumber, int pageSize){
        Long userId = UserDetailsUtils.extractUserId(userDetails);

        Pageable pageNumberAndSize = PageRequest.of(pageNumber, pageSize);

        Page<StudySession> retrievedSessionForToday =  studySessionRepository.findSessionsForToday(userId, LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX),
                Arrays.asList(SessionStatus.COMPLETED, SessionStatus.CANCELLED, SessionStatus.IN_PROGRESS),
                pageNumberAndSize
        );

        Page<SessionResponse> convertedStudySessionsToSessionResponse = retrievedSessionForToday.map(SessionResponse::from);

        return PagedResponse.from(convertedStudySessionsToSessionResponse);

    }

    public List<StudySession> getAllCompletedSessionsForUser(UserDetails userDetails){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        return studySessionRepository.retrieveSessionsWithSpecificStatus(userId, SessionStatus.COMPLETED);
    }

    public List<StudySession> getRecentCompletedSessions(){
        return studySessionRepository.findRecentCompletedSessions(LocalDateTime.now().minusHours(1), SessionStatus.COMPLETED);
    }

    public List<StudySession> getCancelledSessions(UserDetails userDetails){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        return studySessionRepository.retrieveSessionsWithSpecificStatus(userId, SessionStatus.CANCELLED);
    }


    public SessionOverviewResponse retrieveSessionsForThisWeek(UserDetails userDetails){

        Long userId = UserDetailsUtils.extractUserId(userDetails);


        LocalDateTime startOfTheWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime endOfTheWeek = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(LocalTime.MAX);

        LocalDateTime startOfPreviousWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfPreviousWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1).toLocalDate().with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);


        List<StudySession> completedSessionsThisWeekQuery = studySessionRepository.
                retrieveSessionsForASpecificTimePeriod(userId, startOfTheWeek, endOfTheWeek, SessionStatus.COMPLETED);
        List<StudySession> cancelledSessionThisWeekQuery = studySessionRepository.
                retrieveSessionsForASpecificTimePeriod(userId, startOfTheWeek, endOfTheWeek, SessionStatus.CANCELLED);
        List<StudySession> completedSessionsLastWeekQuery = studySessionRepository.
                retrieveSessionsForASpecificTimePeriod(userId, startOfPreviousWeek, endOfPreviousWeek, SessionStatus.COMPLETED);
        List<StudySession> cancelledSessionsLastWeekQuery = studySessionRepository.
                retrieveSessionsForASpecificTimePeriod(userId, startOfPreviousWeek, endOfPreviousWeek, SessionStatus.CANCELLED);

        //Modify it to be done at Query level to avoid processing it here
        List<SessionResponse> completedSessionsThisWeek = completedSessionsThisWeekQuery.stream().map(SessionResponse::from).collect(Collectors.toList());
        List<SessionResponse> cancelledSessionsThisWeek = cancelledSessionThisWeekQuery.stream().map(SessionResponse::from).collect(Collectors.toList());
        List<SessionResponse> completedSessionsPreviousWeek = completedSessionsLastWeekQuery.stream().map(SessionResponse::from).collect(Collectors.toList());
        List<SessionResponse> cancelledSessionsPreviousWeek = cancelledSessionsLastWeekQuery.stream().map(SessionResponse::from).collect(Collectors.toList());


        //Percentage Logic
        int totalSessionsThisWeek = completedSessionsThisWeek.size() + cancelledSessionsThisWeek.size();
        int  completionRateThisWeek = totalSessionsThisWeek == 0 ? 0 :
                (int) ((double)completedSessionsThisWeek.size() / totalSessionsThisWeek * 100);

        int totalSessionsPreviousWeek =  completedSessionsPreviousWeek.size() + cancelledSessionsPreviousWeek.size();
        int completionRatePreviousWeek = totalSessionsPreviousWeek == 0 ? 0 :
                (int) ( (double)completedSessionsPreviousWeek.size() / totalSessionsPreviousWeek * 100);

        WeekStats thisWeekStats = new WeekStats(completedSessionsThisWeek.size(), cancelledSessionsThisWeek.size(), totalSessionsThisWeek, completionRateThisWeek);
        WeekStats previousWeekStats = new WeekStats(completedSessionsPreviousWeek.size(), cancelledSessionsPreviousWeek.size(), totalSessionsPreviousWeek, completionRatePreviousWeek);

        //More math logic comparing the previous week to the current
        List<String> changes = validateStatsMessages(thisWeekStats, previousWeekStats);


        return  new SessionOverviewResponse(completedSessionsThisWeek.size(), cancelledSessionsThisWeek.size(),
                completedSessionsThisWeek, cancelledSessionsThisWeek, completionRateThisWeek, changes);
    }


    private List<String> validateStatsMessages (WeekStats thisWeekStats, WeekStats previousWeekStats){

        // String thisWeekChangesCompletion = describeChange(thisWeekStats.completedSessions, previousWeekStats.completedSessions, "Integer");
        String thisWeekChangesTotalSessions = describeChange(thisWeekStats.totalSessions, previousWeekStats.totalSessions, "Integer");
        String thisWeekChangesCancellation = describeChange(thisWeekStats.cancelledSessions, previousWeekStats.cancelledSessions, "Integer");
        String percentageChanges = describeChange(thisWeekStats.completionRate, previousWeekStats.completionRate, "Percentage");


        return Arrays.asList( thisWeekChangesTotalSessions, thisWeekChangesCancellation, percentageChanges);
    }

    //Integer & Percentage
    private String describeChange (int currentWeek, int previousWeek, String type){
        int difference = currentWeek - previousWeek;

        if(type.equals("Integer")){  //For session counts
            if (difference > 0) return difference + " more sessions than last week";
            if(difference < 0) return Math.abs(difference) + " less sessions than last week";
        }

        if(type.equals("Percentage")){ //For percentage changes
            if(difference > 0 ) return difference + "% increase";
            if(difference < 0) return  Math.abs(difference) + "% decrease";
        }

        return "";
    }

    public PagedResponse<SessionResponse> retrieveSubjectSessionHistory(Long subjectId, int pageNumber, int pageSize){

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<StudySession> retrievedSessions = studySessionRepository.findHistoryForSubject(subjectId, pageable);

        Page<SessionResponse> convertSessionsToSessionResponse = retrievedSessions.map(SessionResponse::from);

        return PagedResponse.from(convertSessionsToSessionResponse);
    }

    public List<CompletedSessionSummary> getMonthlyCompletedSessions (UserDetails userDetails, YearMonth yearMonth){

        long userId = UserDetailsUtils.extractUserId(userDetails);

        return studySessionRepository.getCompletedSessionsForSpecificMonth(userId, yearMonth.getYear(), yearMonth.getMonthValue(),SessionStatus.COMPLETED);

    }

    public List<CompletedSessionsPerSubject> getCompletedSessionsByTimePeriod(UserDetails userDetails, YearMonth yearMonth){
        Long userId = UserDetailsUtils.extractUserId(userDetails);

        return studySessionRepository.getCompletedSessionsByTimePeriod(userId, yearMonth.getYear(), yearMonth.getMonthValue(), SessionStatus.COMPLETED);

    }

    //Record use for the validate Stats parameters
    private record WeekStats(
            int completedSessions,
            int cancelledSessions,
            int totalSessions,
            int completionRate
    ){}

}
