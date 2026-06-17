package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.response.single_response.DurationBucketDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class InsightGenerator {


    /**
     * Compare the best bucket to a competing bucket for the insight string
     * @param bestBucket
     * @param buckets
     * @return
     */

    public String generateInsight(DurationBucketDTO bestBucket, List<DurationBucketDTO> buckets){

        int bestBucketPlannedMinutes = bestBucket.plannedDurationMinutes();
        long bestBucketCompletionPct = Math.round(bestBucket.completionRate() * 100);

        //Find the bucket that isn't the best bucket via most completed sessions
        return buckets.stream().filter( bucket -> !bucket.plannedDurationMinutes().equals(bestBucketPlannedMinutes))
                .max(Comparator.comparingLong(bucket -> bucket.completedSessions()))
                .map( competitor -> {
                    long competitorCompletionPct = Math.round(competitor.completionRate() * 100);

                    return String.format("You completed %d%% of your %d-minute sessions but only %d%% of %d-minute ones",
                            bestBucketCompletionPct, bestBucketPlannedMinutes,
                            competitorCompletionPct, competitor.plannedDurationMinutes());})
                .orElse(String.format("You complete %d%% of your %%d-minute sessions. Great work!",
                        bestBucketCompletionPct, bestBucketPlannedMinutes));
    }

    /**
     * Converts 24-hour integer into a more readable output
     * @param peakHour
     * @return
     */

    public String generatePeakHourInsight(Long peakHour){

            if(peakHour == 0 ) return "12 AM";
            if(peakHour == 12) return "12 PM";
            if(peakHour < 12) return String.format("%d", peakHour);
            return (peakHour - 12) + " PM";

    }
}
