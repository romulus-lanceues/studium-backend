package com.lancea.studium.studium_api.util;

import com.lancea.studium.studium_api.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

//A utility class used to extract the user id from the UserDetails
public class UserDetailsUtils {

    private UserDetailsUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Long extractUserId(UserDetails userDetails){

        if(! (userDetails instanceof MyUserDetails)) {
            throw new IllegalArgumentException("Invalid UserDetails type");
        }

        return ((MyUserDetails) userDetails).getUserId();
    }
}
