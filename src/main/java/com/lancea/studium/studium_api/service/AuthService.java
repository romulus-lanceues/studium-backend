package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RefreshTokenRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.dto.response.AuthResponse;
import com.lancea.studium.studium_api.dto.response.NewRefreshTokenResponse;
import com.lancea.studium.studium_api.entity.RefreshToken;
import com.lancea.studium.studium_api.entity.Role;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.exception.UnauthorizedException;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder
    , JwtService jwtService){
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }

    //Creating a normal user
    public AuthResponse createUser(RegisterRequest registerRequest){

        //Check if the email already exists
        Optional<User> user = userRepository.findByEmail(registerRequest.email());
        if(user.isPresent()){
            //Throw a generic ResponseStatusException to avoid many custom exception entity
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        //Create an instance using Lombok builder
        User newUser = User.builder()
                .email(registerRequest.email())
                .password(hashPassword(registerRequest.password()))
                .fullName(registerRequest.fullName())
                .role(Role.USER)
                .build();

        userRepository.save(newUser);

        //       ===TOKEN CREATION FOR ACCOUNT CREATION NOT YET IMPLEMENTED ===

        return new AuthResponse(newUser.getId(), newUser.getEmail(), newUser.getFullName(), null, null);
    }

    //Create admin account
    public AuthResponse createAdminAccount(RegisterRequest registerRequest){
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.email());

        if(existingUser.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        User newAdminAccount = User.builder()
                .email(registerRequest.email())
                .password(registerRequest.password())
                .fullName(registerRequest.fullName())
                .role(Role.ADMIN)
                .build();

        userRepository.save(newAdminAccount);

        return new AuthResponse(newAdminAccount.getId(), newAdminAccount.getEmail(), newAdminAccount.getFullName(), null, null);
    }

    //Used to hash password during signup
    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }


    //Login method
    public AuthResponse verifyCredentials(LoginRequest loginRequest){

            //Authenticate user using Spring Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.email(),
                    loginRequest.password()
            );
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            System.out.println(userDetails.getUsername());
            User retrievedUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist."));
            System.out.println(retrievedUser.getId());

            String token = jwtService.generateJwtToken(retrievedUser);
            String refreshToken = jwtService.generateRefreshToken(retrievedUser.getId());

            return new AuthResponse(retrievedUser.getId(), retrievedUser.getEmail(), retrievedUser.getFullName(), token, refreshToken);

    }

    //Used to verify the password during login
    private boolean verifyPassword(String rawPassword, String hashedPassword){
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public User getUserInfo(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));
    }

    public Map<String, Object> getSpecificUser(Long userId){
        User user = userRepository.findById(userId).orElseThrow( () -> new ResourceNotFoundException("User not found"));
        Map<String, Object> authResponseBody = new HashMap<>();

        authResponseBody.put("id", user.getId());
        authResponseBody.put("name", user.getFullName());
        authResponseBody.put("email", user.getEmail());


        return authResponseBody;
    }

    public NewRefreshTokenResponse generateNewRefreshToken(RefreshTokenRequest refreshRequest){

        RefreshToken oldRefreshToken = jwtService.verifyRefreshToken(refreshRequest.refreshToken());

        jwtService.revokeRefreshToken(oldRefreshToken.getToken());

        User user = userRepository.findById(oldRefreshToken.getUserId()).orElseThrow( () -> new ResourceNotFoundException("User doesn't exist"));

        String newAccessToken = jwtService.generateJwtToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        return new NewRefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    public void logoutUser(RefreshTokenRequest logoutRequest){
        jwtService.revokeRefreshToken(logoutRequest.refreshToken());
    }


}
