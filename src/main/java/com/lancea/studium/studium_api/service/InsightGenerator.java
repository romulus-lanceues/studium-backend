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
        String bestBucketCompletionPct = generatePercentage(bestBucket.completionRate());

        //Find the bucket that isn't the best bucket via most completed sessions
        return buckets.stream().filter( bucket -> !bucket.plannedDurationMinutes().equals(bestBucketPlannedMinutes))
                .max(Comparator.comparingLong(bucket -> bucket.completedSessions()))
                .map( competitor -> {
                    String competitorCompletionPct = generatePercentage(competitor.completionRate());

                    return String.format("You completed %s of your %d-minute sessions but only %s of %d-minute ones",
                            bestBucketCompletionPct, bestBucketPlannedMinutes,
                            competitorCompletionPct, competitor.plannedDurationMinutes());})
                .orElse(String.format("You complete %s of your %d-minute sessions. Great work!",
                        bestBucketCompletionPct, bestBucketPlannedMinutes));
    }

    /**
     * Converts 24-hour integer into a more readable output
     * @param peakHour
     * @return
     */

    public String generatePeakHourInsight(Integer peakHour){

            if(peakHour == 0 ) return "12 AM";
            if(peakHour == 12) return "12 PM";
            if(peakHour < 12) return String.format("%d AM", peakHour);
            return (peakHour - 12) + " PM";

    }

    /**
     * Converts any decimal into its percentage format
     * @param rating decimal state of the data
     * @return String
     */
    private String generatePercentage(Double rating){
        return String.format("%d%%", Math.round(rating * 100));
    }
}
