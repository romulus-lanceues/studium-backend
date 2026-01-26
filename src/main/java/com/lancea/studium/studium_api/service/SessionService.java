package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.request.CompletionRequest;
import com.lancea.studium.studium_api.dto.request.StartSessionRequest;
import com.lancea.studium.studium_api.dto.response.SessionResponse;
import com.lancea.studium.studium_api.entity.SessionStatus;
import com.lancea.studium.studium_api.entity.SessionType;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.entity.Subject;
import com.lancea.studium.studium_api.exception.InvalidSessionStateException;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.exception.UnauthorizedException;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.repository.SubjectRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SessionService {

    private final SubjectRepository subjectRepository;
    private final StudySessionRepository studySessionRepository;
    private final PomodoroSessionCacheService pomodoroSessionCacheService;

    public SessionService(SubjectRepository subjectRepository, StudySessionRepository studySessionRepository,
                          PomodoroSessionCacheService pomodoroSessionCacheService){
        this.subjectRepository = subjectRepository;
        this.studySessionRepository = studySessionRepository;
        this.pomodoroSessionCacheService = pomodoroSessionCacheService;
    }

    /*
        Start a session service

        1. Check if the subject belongs to the user
        2. Create a new session using the details provided in the request
        3. Return a request
     */

    public SessionResponse createSession(StartSessionRequest startSessionRequest, UserDetails userDetails){

        //Retrieve the user id from the security context
        Long userId = ((MyUserDetails) userDetails).getUserId();
        //Check if the subject belongs to the user
        Subject targetSubject = subjectRepository.findByIdAndUserId(startSessionRequest.subjectId(), userId).
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

        return new SessionResponse(newSession.getId(), newSession.getSubject().getName(),
                newSession.getPlannedDurationMinutes(), newSession.getActualDurationMinutes(), newSession.getSessionStatus(), newSession.getCreatedAt(), newSession.getEndTime());
    }

    public SessionResponse getSession(Long sessionId){

        StudySession session = studySessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session doesn't exist"));

        return new SessionResponse(session.getId(), session.getSubject().getName(),
                session.getPlannedDurationMinutes(), session.getActualDurationMinutes(), session.getSessionStatus(), session.getStartTime(), session.getEndTime() );
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
    public Map<String, Object> completeSession(Long sessionId, CompletionRequest completionRequest, UserDetails userDetails){
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

        //Verify if the frontend's total minute is accurate by computing it
        long elapsedMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), completionTime);

        boolean isActualDurationMinutesValid = completionRequest.actualDurationMinutes() == (int) elapsedMinutes;

        /*
        If isActualDurationMinutesValid is true set the ActualDurationMinutes of the session to the minutes frontend sent
        If not set use the one backend calculated
         */
        session.setActualDurationMinutes(isActualDurationMinutesValid ? completionRequest.actualDurationMinutes() : (int) elapsedMinutes);

        session.setEndTime(completionTime);
        session.setActualDurationMinutes((int) elapsedMinutes);
        session.setSessionStatus(SessionStatus.COMPLETED);

        //Increase the pomodoros completed for this session's subject

        Subject sessionSubject = session.getSubject();
        sessionSubject.increasePomodoroCompleted();
        sessionSubject.increaseStudyTime(session.getActualDurationMinutes());

        studySessionRepository.save(session);
        subjectRepository.save(sessionSubject);


        //Add the completed session to cache
        pomodoroSessionCacheService.addCompletedSession(requestUserId, session.getId());

        //Return a break type response
        SessionType breakType = pomodoroSessionCacheService.determineBreakType(requestUserId);

        responseBody.put("sessionId", session.getId() );
        responseBody.put("status", session.getSessionStatus());
        responseBody.put("break", breakType);

        return responseBody;
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

        return new SessionResponse(session.getId(), session.getSubject().getName(),
                session.getPlannedDurationMinutes(), session.getActualDurationMinutes(), session.getSessionStatus(), session.getStartTime(), session.getEndTime());

    }

}
