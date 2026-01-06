package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.config.CookieUtil;
import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.dto.response.AuthResponse;
import com.lancea.studium.studium_api.entity.RefreshToken;
import com.lancea.studium.studium_api.entity.Role;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.security.MyUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CookieUtil cookieUtil;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, CookieUtil cookieUtil){
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.cookieUtil = cookieUtil;

    }

    //Creating a normal user
    public long createUser(RegisterRequest registerRequest, HttpServletResponse response){

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

        String jwtToken = jwtService.generateJwtToken(newUser);
        String refreshToken = jwtService.generateRefreshToken(newUser.getId());

        cookieUtil.addAuthCookies(response, jwtToken, refreshToken);

        return newUser.getId();
    }

    //Create admin account (Not yet used)
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

        return new AuthResponse("User created");
    }

    //Used to hash password during signup
    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }


    //Login method
    public void verifyCredentials(LoginRequest loginRequest, HttpServletResponse response){

            //Authenticate user using Spring Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.email(),
                    loginRequest.password()
            );

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            System.out.println(userDetails.getUsername());
            User retrievedUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist."));

            String jwtToken = jwtService.generateJwtToken(retrievedUser);
            String refreshToken = jwtService.generateRefreshToken(retrievedUser.getId());

            cookieUtil.addAuthCookies(response, jwtToken, refreshToken);

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

    public void generateNewRefreshToken(HttpServletRequest request, HttpServletResponse response){

        //Extract refresh token from the cookie
        String existingRefreshTokenFromCookie = cookieUtil.getRefreshTokenFromCookie(request);

        if(existingRefreshTokenFromCookie.isEmpty()){
            throw  new ResourceNotFoundException("No token provided");
        }

        //Verify the refresh token and return the RefreshToken object that'll be used for the creation of new tokens
         RefreshToken oldRefreshToken = jwtService.verifyRefreshToken(existingRefreshTokenFromCookie);

        jwtService.revokeRefreshToken(oldRefreshToken.getToken());

        User user = userRepository.findById(oldRefreshToken.getUserId()).orElseThrow( () -> new ResourceNotFoundException("User doesn't exist"));

        String newAccessToken = jwtService.generateJwtToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        cookieUtil.addJwtCookie(response, newAccessToken);
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);
    }

    public void logoutUser(HttpServletRequest request, HttpServletResponse response){
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

        //Revoke refresh token
        if(!refreshToken.isEmpty()){
            jwtService.revokeRefreshToken(refreshToken);
        }

        cookieUtil.deleteBothCookies(response);
    }

}
