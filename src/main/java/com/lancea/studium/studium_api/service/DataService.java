package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.response.SessionOverviewResponse;
import com.lancea.studium.studium_api.dto.response.SessionResponse;
import com.lancea.studium.studium_api.entity.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataService {

    private StudySessionRepository studySessionRepository;

    public DataService(StudySessionRepository studySessionRepository){
        this.studySessionRepository = studySessionRepository;
    }

    public List<StudySession> getAllCompletedSessionsForUser(UserDetails userDetails){

        Long userId = ( (MyUserDetails) userDetails).getUserId();

        return studySessionRepository.retrieveSessionsWithSpecificStatus(userId, SessionStatus.COMPLETED);
    }

    public List<StudySession> getRecentCompletedSessions(UserDetails userDetails){

        Long userId = ( (MyUserDetails) userDetails).getUserId();

        return studySessionRepository.findRecentCompletedSessions(LocalDateTime.now().minusHours(1), SessionStatus.COMPLETED);
    }

    public List<StudySession> getCancelledSessions(UserDetails userDetails){

        Long userId = ( (MyUserDetails) userDetails).getUserId();

        return studySessionRepository.retrieveSessionsWithSpecificStatus(userId, SessionStatus.CANCELLED);
    }

    public SessionOverviewResponse retrieveSessionsForThisWeek(UserDetails userDetails){

        Long userId = ( (MyUserDetails) userDetails).getUserId();


        LocalDateTime startOfTheWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();

        LocalDateTime endOfTheWeek = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59);

        List<StudySession> sessions =  studySessionRepository.retrieveCompletedAndCancelledSessionsForASpecificTimePeriod(startOfTheWeek, endOfTheWeek, Arrays.asList(SessionStatus.COMPLETED, SessionStatus.CANCELLED), userId);

        List<SessionResponse> completedSessions = new ArrayList<>();
        List<SessionResponse> cancelledSessions = new ArrayList<>();

        for(StudySession session : sessions){
            if(session.getSessionStatus().equals(SessionStatus.COMPLETED)) {
                completedSessions.add(new SessionResponse(session.getId(), session.getSubject().getName(), session.getPlannedDurationMinutes(), session.getActualDurationMinutes(), session.getSessionStatus(), session.getStartTime(), session.getEndTime()));
                continue;
            }

            cancelledSessions.add(new SessionResponse(session.getId(), session.getSubject().getName(), session.getPlannedDurationMinutes(), session.getActualDurationMinutes(), session.getSessionStatus(), session.getStartTime(), session.getEndTime()));
        }


         return  new SessionOverviewResponse(completedSessions.size(), cancelledSessions.size(), completedSessions, cancelledSessions);

    }


}
