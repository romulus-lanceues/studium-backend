package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.entity.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        Long userid = ( (MyUserDetails) userDetails).getUserId();

        return studySessionRepository.retrieveSessionsWithSpecificStatus(userid, SessionStatus.CANCELLED);
    }


}
