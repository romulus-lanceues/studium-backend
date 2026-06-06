package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.request.CreateSubjectRequest;
import com.lancea.studium.studium_api.dto.request.UpdateSubjectRequest;
import com.lancea.studium.studium_api.dto.response.paged_response.PagedResponse;
import com.lancea.studium.studium_api.dto.response.single_response.SubjectResponse;
import com.lancea.studium.studium_api.entity.Subject;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.repository.SubjectRepository;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import com.lancea.studium.studium_api.util.UserDetailsUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        Long userid = UserDetailsUtils.extractUserId(userDetails);
        User user = userRepository.findById(userid).orElseThrow( () -> new ResourceNotFoundException("User doesn't exist"));

            //Create a new subject after verification
            Subject newSubject = Subject.builder()
                    .name(createSubjectRequest.subjectName())
                    .color(createSubjectRequest.color())
                    .description(createSubjectRequest.description())
                    .weeklyGoalSessions(createSubjectRequest.weeklyGoal())
                    .user(user)
                    .build();

            //Save it to the database
            subjectRepository.save(newSubject);

            return  SubjectResponse.from(newSubject);


    }


    public PagedResponse<SubjectResponse> getUserSubjects(UserDetails userDetails, int pageNumber, int pageSize){

        Long userId = ((MyUserDetails) userDetails).getUserId();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));

        Page<Subject> subjects = subjectRepository.findByUserId(userId, pageable);

        Page<SubjectResponse> subjectResponses = subjects.map(SubjectResponse::from);

        return PagedResponse.from(subjectResponses);

    }

    public SubjectResponse getSubject(Long subjectId){
        Subject subject = subjectRepository.findById(subjectId).orElseThrow( () -> new ResourceNotFoundException("Subject doesn't exist"));

        return SubjectResponse.from(subject);
    }

    public void deleteSubject(Long subjectId){

        //Find the subject from the DB first and validate if it exists
        Subject subject = subjectRepository.findById(subjectId).orElseThrow( () -> new ResourceNotFoundException("Subject not found"));

        subjectRepository.deleteById(subjectId);
    }

    public SubjectResponse updateSubjectDetails(UserDetails userDetails,
                                                Long subjectId, UpdateSubjectRequest updateSubjectRequest){

        Long userId = UserDetailsUtils.extractUserId(userDetails);
        Subject subjectToBeUpdated = subjectRepository.findByIdAndUserId(subjectId, userId).orElseThrow(
                () -> new ResourceNotFoundException("Subject not found"));

        //Existing data because we retrieved the existing name description, etc. of the existing subject

        if(!Objects.equals(updateSubjectRequest.subjectName(), subjectToBeUpdated.getName())){
            subjectToBeUpdated.setName(updateSubjectRequest.subjectName());
        }
        if(!Objects.equals(updateSubjectRequest.subjectDescription(), subjectToBeUpdated.getName())){
            subjectToBeUpdated.setDescription(updateSubjectRequest.subjectDescription());
        }

        if(!Objects.equals(updateSubjectRequest.weeklyGoalSessions(), subjectToBeUpdated.getWeeklyGoalSessions())){
            subjectToBeUpdated.setWeeklyGoalSessions(updateSubjectRequest.weeklyGoalSessions()  );
        }

        if(!Objects.equals(updateSubjectRequest.subjectColor(), subjectToBeUpdated.getColor())){
            subjectToBeUpdated.setColor(updateSubjectRequest.subjectColor());
        }

        subjectRepository.save(subjectToBeUpdated);

        return SubjectResponse.from(subjectToBeUpdated);

    }

}
