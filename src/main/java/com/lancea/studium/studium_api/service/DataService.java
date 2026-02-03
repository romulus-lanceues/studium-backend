package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.response.DashboardResponse;
import com.lancea.studium.studium_api.dto.response.SessionOverviewResponse;
import com.lancea.studium.studium_api.dto.response.SessionResponse;
import com.lancea.studium.studium_api.entity.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.util.UserDetailsUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DataService {

    private StudySessionRepository studySessionRepository;
    private UserRepository userRepository;

    public DataService(StudySessionRepository studySessionRepository, UserRepository userRepository){
        this.studySessionRepository = studySessionRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse retrieveDataNeededForDashboard(UserDetails userDetails) {

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        User user = userRepository.findById(userId).orElseThrow( () -> new ResourceNotFoundException("User not found"));

        List<StudySession> userCompletedSessionsForToday = studySessionRepository.
                retrieveCompletedSessionsToday(userId, LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(LocalTime.MAX), SessionStatus.COMPLETED);

        Integer streak = user.getStreak();

        String lastSession = configureLastSession(user.getLastSession());


        return new DashboardResponse(streak, lastSession, userCompletedSessionsForToday.size(), userCompletedSessionsForToday);

    }

    private String configureLastSession(LocalDate lastSessionDate){
        if(lastSessionDate.equals(LocalDate.now())){
            return "Today";
        }

        return ChronoUnit.DAYS.between(lastSessionDate, LocalDate.now()) + " days ago.";
    }


    public List<StudySession> getAllCompletedSessionsForUser(UserDetails userDetails){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        return studySessionRepository.retrieveSessionsWithSpecificStatus(userId, SessionStatus.COMPLETED);
    }

    public List<StudySession> getRecentCompletedSessions(UserDetails userDetails){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

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
