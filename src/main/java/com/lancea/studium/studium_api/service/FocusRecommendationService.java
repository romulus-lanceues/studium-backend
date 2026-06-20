package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.projection.PeakHourProjection;
import com.lancea.studium.studium_api.dto.response.single_response.DurationBucketDTO;
import com.lancea.studium.studium_api.dto.response.single_response.FocusRecommendationDTO;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.shared.enums.SessionType;
import com.lancea.studium.studium_api.util.UserDetailsUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Core business logic when it comes to generating user insights and session suggestion.
 * What it does:
 * 1. Identify the best session duration when the user has the highest completion rate and least interruptions.
 * 2. Calculate the confidence as additional backup on why this duration is ideal for the user.
 * 3. Provide the "Peak Hour" this is the time the user is the most productive.
 * 4. Generate insights (human-readable version of the computed data) delegated via InsightGeneratorService
 */
@Service
public class FocusRecommendationService {

    private static final int MINIMUM_SESSIONS_THRESHOLD = 10;
    private static final int ANALYSIS_WINDOW_DAYS = 90;
    private static final int DEFAULT_FOCUS_MINUTES = 25;
    private static final double INTERRUPTION_PENALTY_WEIGHT = 0.05;


    private final StudySessionRepository studySessionRepository;
    private final InsightGenerator insightGenerator;


    public FocusRecommendationService(StudySessionRepository studySessionRepository, InsightGenerator insightGenerator){
        this.studySessionRepository = studySessionRepository;
        this.insightGenerator = insightGenerator;
    }

    public FocusRecommendationDTO retrieveRecommendation(UserDetails userDetails){

        long userId = UserDetailsUtils.extractUserId(userDetails);

        LocalDateTime since = LocalDateTime.now().minusDays(ANALYSIS_WINDOW_DAYS);

        Long eligibleSessionsCount = studySessionRepository.countEligibleSessionByUserId(userId,
                SessionType.WORK,
                SessionStatus.COMPLETED,
                SessionStatus.CANCELLED,
                since);

        //Guard: check if the eligible sessions are >= MINIMUM_SESSIONS_THRESHOLD(10)
        if(eligibleSessionsCount < MINIMUM_SESSIONS_THRESHOLD){
            return buildInsufficientDataResponse(eligibleSessionsCount);
        }

        List<DurationBucketDTO> sessionBuckets = studySessionRepository.findDurationBucketsByUserId(userId,
                SessionType.WORK, SessionStatus.COMPLETED, SessionStatus.CANCELLED, since);

        List<PeakHourProjection> peakHourList = studySessionRepository.findPeakHoursByUser(userId, since);


        DurationBucketDTO bestBucket = scoreBuckets(sessionBuckets);
        Double confidence = calculateConfidence(bestBucket, eligibleSessionsCount);
        String peakHour = retrievePeakHour(peakHourList);
        String insights = insightGenerator.generateInsight(bestBucket, sessionBuckets);

        return new FocusRecommendationDTO(
                bestBucket.plannedDurationMinutes(),
                confidence,
                eligibleSessionsCount,
                peakHour,
                insights,
                true
        );

    }

    /**
     * Calculates the score of each duration bucket and return the best one.
     * Score = Bucket Completion Rate - Interruption Penalty (Average Interruptions * INTERRUPTION_PENALTY_WEIGHT)
     * Completion rate is the primary signal. Interruptions apply a small hindrance or penalty.
     * A duration finished with constant interruptions is weaker than a clean session without one.
     * @param buckets
     * @return DurationBucketDTO
     */

    private DurationBucketDTO scoreBuckets(List<DurationBucketDTO> buckets){
        return buckets.stream()
                // performance: potential performance issue score() is being repeatedly run every comparison
                .max(Comparator.comparingDouble(this::score))
                .orElseThrow( () -> new IllegalStateException("Duration Buckets Empty"));

    }

    private double score(DurationBucketDTO bucket){
        double interruptionPenalty = bucket.avgInterruptions() * INTERRUPTION_PENALTY_WEIGHT;
        return bucket.completionRate() - interruptionPenalty;
    }

    /**
     * Confidence is a factor of 2 things:
     * 1. How strong is the completion rate of the bestBucket
     * 2. How many sessions back it up (more data = more trust)
     *
     * Sample size factor approaches 1.0 as sessions exceed 50. Below 50, confidence is dampened proportionally.
     *
     * @param bestBucket
     * @param totalSessions
     * @return confidence level of the user
     */

    private double calculateConfidence(DurationBucketDTO bestBucket, Long totalSessions){
        double sampleSizeFactor = Math.min(totalSessions * 50.0, 1.00);
        double rawConfidence = bestBucket.completionRate() * sampleSizeFactor;

        return Math.round(rawConfidence * 100.0) / 100.0;
    }

    /**
     * Get the hour with the most completed session
     * Delegate the generation of a readable insight from generatePeakHourInsight (InsightGeneratorService)
     * @param peakHours
     * @return human readable insight
     */
    private String retrievePeakHour(List<PeakHourProjection> peakHours){
        return insightGenerator.generatePeakHourInsight(
                peakHours.isEmpty() ? null : peakHours.getFirst().getHour());
    }


    /**
     * Creates the response if the user's session is less than MINIMUM_SESSIONS_THRESHOLD
     * @param sessionCount
     * @return FocusRecommendationDTO
     */
    private FocusRecommendationDTO buildInsufficientDataResponse(Long sessionCount){
        return new FocusRecommendationDTO(
                DEFAULT_FOCUS_MINUTES,
                0.0,
                sessionCount,
                null,
                String.format("Complete at least %d sessions so we can estimate the recommended session duration for you. You have %d so far", MINIMUM_SESSIONS_THRESHOLD, sessionCount),
                false
        );
    }

}


