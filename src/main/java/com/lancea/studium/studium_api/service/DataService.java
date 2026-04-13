package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.response.bundled_response.DashboardResponse;
import com.lancea.studium.studium_api.dto.response.bundled_response.SubjectsPageResponse;
import com.lancea.studium.studium_api.dto.response.paged_response.PagedResponse;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionSummary;
import com.lancea.studium.studium_api.dto.response.single_response.SessionResponse;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.repository.SubjectRepository;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.util.UserDetailsUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DataService {

    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;


    public DataService(StudySessionRepository studySessionRepository, UserRepository userRepository, SubjectRepository subjectRepository){
        this.studySessionRepository = studySessionRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    //Frontend dashboard page API call
    public DashboardResponse retrieveDataNeededForDashboard(UserDetails userDetails) {

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        User user = userRepository.findById(userId).orElseThrow( () -> new ResourceNotFoundException("User not found"));

        int userCompletedSessionsForToday = studySessionRepository.
                countCompletedSessionsToday(userId, LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(LocalTime.MAX), SessionStatus.COMPLETED).intValue();

        Integer streak = user.getStreak();

        String lastSession = configureLastSession(user.getLastSession());


        return new DashboardResponse(user.getFullName(), streak, lastSession, userCompletedSessionsForToday);

    }

    private String configureLastSession(LocalDate lastSessionDate){
        if(lastSessionDate.equals(LocalDate.now())){
            return "Today";
        }

        return ChronoUnit.DAYS.between(lastSessionDate, LocalDate.now()) + " days ago.";
    }

    public PagedResponse<SessionResponse> getStudySessionHistory(UserDetails userDetails, int page, int size ){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        //Create a Pageable object that contains the target page and size
        Pageable pageable = PageRequest.of(page, size);

        //Call the query that retrieves the user's session history and returns a Page
        Page<StudySession> sessionPage = studySessionRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);

        Page<SessionResponse> sessionResponseDTO = sessionPage.map(SessionResponse::from);

        return PagedResponse.from(sessionResponseDTO);
    }

    //Frontend subjects page API call
    public SubjectsPageResponse getSubjectsAndItsInfos(UserDetails userDetails){
        long userId = UserDetailsUtils.extractUserId(userDetails);

        long totalSubjects = subjectRepository.subjectCount(userId);
        long totalSessions = studySessionRepository.userSessionsCount(userId).intValue();
        long totalStudyTimeForAllTheSubject = subjectRepository.getUserTotalStudyTime(userId);

        String studyTime = String.format("%dh %dm", totalStudyTimeForAllTheSubject / 60, totalStudyTimeForAllTheSubject % 60 );

        return new SubjectsPageResponse(totalSubjects, totalSessions, studyTime);
    }

    public Long getUserSessionCount(UserDetails userDetails){
        long userId = UserDetailsUtils.extractUserId(userDetails);
        return studySessionRepository.userSessionsCount(userId);
    }



}
