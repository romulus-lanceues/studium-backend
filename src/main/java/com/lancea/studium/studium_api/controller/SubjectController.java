package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.dto.request.CreateSubjectRequest;
import com.lancea.studium.studium_api.dto.response.SubjectResponse;
import com.lancea.studium.studium_api.security.MyUserDetails;
import com.lancea.studium.studium_api.service.SubjectService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/subject", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService){
        this.subjectService = subjectService;
    }

    @PostMapping("/add")
    public ResponseEntity<SubjectResponse> addSubject(@RequestBody CreateSubjectRequest createSubjectRequest,
                                        @AuthenticationPrincipal UserDetails userDetails){

        SubjectResponse responseBody = subjectService.addSubject(createSubjectRequest, userDetails);


        URI subjectLocation = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/subject/{subjectId}")
                .buildAndExpand(responseBody.subjectId())
                .toUri();


        return ResponseEntity.created(subjectLocation).body(responseBody);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponse>> getUserSubjects(@AuthenticationPrincipal UserDetails userDetails){

        return ResponseEntity.ok(subjectService.getUserSubjects(userDetails));
    }

    @GetMapping("{subjectId}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long subjectId){
        SubjectResponse responseBody = subjectService.getSubject(subjectId);

        return ResponseEntity.ok(responseBody);
    }


}
