package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.config.SecurityConfigMock;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.entity.Subject;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.security.MyUserDetails;
import com.lancea.studium.studium_api.service.DataService;
import com.lancea.studium.studium_api.service.FocusRecommendationService;
import com.lancea.studium.studium_api.service.SessionService;
import com.lancea.studium.studium_api.shared.enums.Role;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.shared.enums.SessionType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataController.class)
public class DataControllerTest extends SecurityConfigMock {



    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private DataService dataService;

    @MockitoBean
    private FocusRecommendationService focusRecommendationService;

    @MockitoBean
    Subject subject;



    @Test
    void retrieveCompletedSessionsForUser_returnsOk() throws Exception {

        User testUser = new User(1L, "testemail@email.com",
                "passyworddy", "Test Coordinator", Role.USER,
                LocalDateTime.now(), LocalDateTime.now(),
                0, LocalDate.now().minusYears(20),
                0, 0, new ArrayList<>(), new ArrayList<>());

        MyUserDetails mockedMyUserDetails = new MyUserDetails(testUser);

        when(sessionService.getAllCompletedSessionsForUser(any(MyUserDetails.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/data/completed-sessions").with(user(mockedMyUserDetails))).
                andExpect(status().isOk()).andExpect(jsonPath("$").isArray());


    }

    @Test
    void retrieveCompletedSessions_passesCorrectUserId() throws Exception{

        User testUser = new User(1L, "testemail@email.com",
                "passyworddy", "Test Coordinator", Role.USER,
                LocalDateTime.now(), LocalDateTime.now(),
                0, LocalDate.now().minusYears(20),
                0, 0, new ArrayList<>(), new ArrayList<>());

        MyUserDetails mockedMyUserDetails = new MyUserDetails(testUser);

        ArgumentCaptor<UserDetails> captor = ArgumentCaptor.forClass(UserDetails.class);

        when(sessionService.getAllCompletedSessionsForUser(captor.capture()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/data/completed-sessions").with(user(mockedMyUserDetails)))
                .andExpect(status().isOk());

        MyUserDetails captured = (MyUserDetails) captor.getValue();
        assertEquals(1L, captured.getUserId());
    }

    @Test
    void retrieveCompletedSessions_returnsTheRightContents() throws Exception{
        User testUser = new User(1L, "testemail@email.com",
                "passyworddy", "Test Coordinator", Role.USER,
                LocalDateTime.now(), LocalDateTime.now(),
                0, LocalDate.now().minusYears(20),
                0, 0, new ArrayList<>(), new ArrayList<>());

        List<StudySession> sessions = List.of(
                StudySession.builder()
                        .id(1L)
                        .startTime(LocalDateTime.of(2026, 6, 30, 9, 0))
                        .endTime(LocalDateTime.of(2026, 6, 30, 9, 25))
                        .plannedDurationMinutes(25)
                        .actualDurationMinutes(25)
                        .sessionStatus(SessionStatus.COMPLETED)
                        .sessionType(SessionType.WORK)
                        .interruptionsCount(0)
                        .notes("Completed chapter 1 review.")
                        .createdAt(LocalDateTime.of(2026, 6, 30, 9, 0))
                        .user(testUser)
                        .subject(subject)
                        .build()
        );

        MyUserDetails mockedMyUserDetails = new MyUserDetails(testUser);

        when(sessionService.getAllCompletedSessionsForUser(any(MyUserDetails.class)))
                .thenReturn(sessions);

        mockMvc.perform(get("/api/v1/data/completed-sessions").with(user(mockedMyUserDetails)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

}
