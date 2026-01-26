package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.dto.request.CompletionRequest;
import com.lancea.studium.studium_api.dto.request.StartSessionRequest;
import com.lancea.studium.studium_api.dto.response.SessionResponse;
import com.lancea.studium.studium_api.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService){
        this.sessionService = sessionService;
    }


    @PostMapping("/start")
    public ResponseEntity<SessionResponse> createSession(@RequestBody StartSessionRequest startSessionRequest,
                                                         @AuthenticationPrincipal UserDetails userDetails){

            SessionResponse sessionRequestBody = sessionService.createSession(startSessionRequest, userDetails);

            URI sessionLocation = ServletUriComponentsBuilder
                    .fromCurrentContextPath()  //Get context path ex: http://localhost:8080
                    .path("/api/v1/sessions/{id}") //Add the right path
                    .buildAndExpand(sessionRequestBody.id())
                    .toUri();

            return ResponseEntity.created(sessionLocation).body(sessionRequestBody);
    }

    @GetMapping("{id}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable Long id){

        SessionResponse sessionDetails = sessionService.getSession(id);

        return ResponseEntity.ok(sessionDetails);
    }

    @PatchMapping("/{sessionId}/interruptions")
    public ResponseEntity<Map<String, Object>> addInterruption(@PathVariable Long sessionId,
                                                               @AuthenticationPrincipal UserDetails userDetails){

        Map<String, Object> responseBody = sessionService.addInterruption(sessionId, userDetails);

        return ResponseEntity.ok(responseBody);
    }

    @PatchMapping("/{sessionId}/pause")
    public ResponseEntity<Map<String, Object>> pauseCurrentSession(@PathVariable Long sessionId,
                                                                   @AuthenticationPrincipal UserDetails userDetails){

        Map<String, Object> responseBody = sessionService.pauseSession(sessionId, userDetails);

        return  ResponseEntity.ok(responseBody);
    }

    @PatchMapping("/{sessionId}/resume")
    public ResponseEntity<Map<String, Object>> resumeCurrentSession(@PathVariable Long sessionId,
                                                                    @AuthenticationPrincipal UserDetails userDetails){

        Map<String, Object> responseBody = sessionService.resumeSession(sessionId, userDetails);

        return ResponseEntity.ok(responseBody);
    }

    @PatchMapping("/{sessionId}/completed")
    public ResponseEntity<Map<String, Object>> completeSession(@PathVariable Long sessionId,
                                                               @RequestBody CompletionRequest completionRequest,
                                                               @AuthenticationPrincipal UserDetails userDetails){

        Map<String, Object> responseBody = sessionService.completeSession(sessionId, completionRequest, userDetails);

        return ResponseEntity.ok(responseBody);
    }

    @PatchMapping("{sessionId}/cancel")
    public ResponseEntity<SessionResponse> cancelSession(@PathVariable Long sessionId,
                                                         @AuthenticationPrincipal UserDetails userDetails){
        SessionResponse responseBody = sessionService.cancelSession(sessionId, userDetails);

        return ResponseEntity.ok(responseBody);
    }


}
