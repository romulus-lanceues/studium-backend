package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.dto.response.AuthResponse;
import com.lancea.studium.studium_api.entity.Role;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.exception.UnauthorizedException;
import com.lancea.studium.studium_api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder
    , JwtService jwtService){
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

        return new AuthResponse(newUser.getId(), newUser.getEmail(), newUser.getFullName(), null);
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

        return new AuthResponse(newAdminAccount.getId(), newAdminAccount.getEmail(), newAdminAccount.getFullName(), null);
    }

    //Used to hash password during signup
    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public AuthResponse verifyCredentials(LoginRequest loginRequest){

        Optional<User> existingUser = userRepository.findByEmail(loginRequest.email());

        User retrievedUser = existingUser.orElseThrow(() -> new ResourceNotFoundException("User doesn't exist."));

        if(!verifyPassword(loginRequest.password(), retrievedUser.getPassword())){
            throw new UnauthorizedException("Incorrect Password");
        }

        String token = jwtService.generateJwtToken(existingUser.get());

        return new AuthResponse(retrievedUser.getId(), retrievedUser.getEmail(), retrievedUser.getFullName(), token);
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
}
