package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.request.CreateSubjectRequest;
import com.lancea.studium.studium_api.dto.response.SubjectResponse;
import com.lancea.studium.studium_api.entity.Subject;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.repository.SubjectRepository;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository){
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public SubjectResponse addSubject(CreateSubjectRequest createSubjectRequest, UserDetails userDetails){

        //Retrieve user id and do a check
        Long userid = ((MyUserDetails) userDetails).getUserId();
        User user = userRepository.findById(userid).orElseThrow( () -> new ResourceNotFoundException("User doesn't exist"));

        //Create a new subject after verification
        Subject newSubject = Subject.builder()
                .name(createSubjectRequest.subjectName())
                .color(createSubjectRequest.color())
                .description(createSubjectRequest.description())
                .weeklyGoalMinutes(createSubjectRequest.weeklyGoal())
                .user(user)
                .build();

        //Save it to the database
        subjectRepository.save(newSubject);

        return  new SubjectResponse(newSubject.getId(), newSubject.getName(), newSubject.getColor(), newSubject.getDescription(), newSubject.getWeeklyGoalMinutes(), newSubject.getTotalStudyTime());

    }


    public List<SubjectResponse> getUserSubjects(UserDetails userDetails){

        //Retrieve user id
        Long userId = ((MyUserDetails) userDetails).getUserId();

        //Subjects will be stored here
        List<SubjectResponse> responseBody = new ArrayList<>();

        //Query all subjects owned by the user
        List<Subject> userSubjects = subjectRepository.findByUserId(userId);

        userSubjects.forEach( subject -> {
            SubjectResponse subjectDetails =  new SubjectResponse(subject.getId(),
                    subject.getName(), subject.getColor(), subject.getDescription(), subject.getWeeklyGoalMinutes(), subject.getTotalStudyTime());

            responseBody.add(subjectDetails);
        });

        return responseBody;
    }

    public SubjectResponse getSubject(Long subjectId){
        Subject subject = subjectRepository.findById(subjectId).orElseThrow( () -> new ResourceNotFoundException("Subject doesn't exist"));

        return new SubjectResponse(subject.getId(), subject.getName(), subject.getColor(), subject.getDescription(), subject.getWeeklyGoalMinutes(), subject.getTotalStudyTime());
    }
}
