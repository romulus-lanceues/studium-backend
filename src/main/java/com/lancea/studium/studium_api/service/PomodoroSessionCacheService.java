package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.entity.SessionType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.net.UnknownServiceException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class PomodoroSessionCacheService {

    private RedisTemplate<String, Object> redisTemplate;

    public PomodoroSessionCacheService(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    private static final String SESSION_KEY_PREFIX = "user:sessions:";
    private static final long TIME_WINDOW_HOURS = 2;
    private static final int SESSIONS_FOR_LONG_BREAK = 4;

    //Add a completed session to Redis
    public void addCompletedSession(Long userId, Long sessionId ){
        //Create a key
        String key = SESSION_KEY_PREFIX + userId;

        /*
        WHY NOT GET THE TIME FROM THE SESSION ENTITY?
        Redis Sorted Sets store items with a score (which must be a number). The score is used to:
        -Sort the items
        -Query ranges (e.g., "give me items with scores between X and Y")

        For time-based queries like "sessions in the past 2 hours", you need scores to be comparable numbers.
         */
        long timestamp = Instant.now().toEpochMilli();

        //Redis Sorted Sets
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();

        //Convert sessionId to string for Redis storage
        zSetOperations.add(key, sessionId.toString(), timestamp);

        //Set expiration on the key (auto-cleanup after inactivity)
        redisTemplate.expire(key, TIME_WINDOW_HOURS + 1, TimeUnit.HOURS);

        //Clean up old sessions
        removeOldSessions(userId);


    }

    public long countRecentSession (Long userId){
        String key = SESSION_KEY_PREFIX + userId;

        long currentTime = Instant.now().toEpochMilli();
        long timeWindowStart = currentTime - (TIME_WINDOW_HOURS * 60 * 60 * 1000);

        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();

        //This might return null so use the object Long
        Long count = zSetOperations.count(key, timeWindowStart, currentTime);

        return count != null ? count : 0;
    }

    public SessionType determineBreakType(Long userId){
        long sessionCount = countRecentSession(userId);

        if(sessionCount == 4){
            resetSessions(userId);
            return SessionType.LONG_BREAK;
        }

        return SessionType.SHORT_BREAK;
    }

    private void removeOldSessions(Long userId){
        String key = SESSION_KEY_PREFIX + userId;

        long currentTime = Instant.now().toEpochMilli();
        long timeWindowStart = currentTime - (TIME_WINDOW_HOURS * 60 * 60 * 1000);

        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();

        //Remove all sessions with scores less than timeWindowStart
        zSetOperations.removeRangeByScore(key, 0, timeWindowStart);
    }

    public void resetSessions(Long userId){
        String key = SESSION_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    //For debugging purposes
    public long getCurrentSessionCount(Long userId){
        return countRecentSession(userId);
    }

}
