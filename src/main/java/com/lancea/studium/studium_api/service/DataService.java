package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.dto.projection.*;
import com.lancea.studium.studium_api.dto.response.bundled_response.*;
import com.lancea.studium.studium_api.dto.response.paged_response.PagedResponse;
import com.lancea.studium.studium_api.dto.response.single_response.*;
import com.lancea.studium.studium_api.shared.enums.BreakDownPeriod;
import com.lancea.studium.studium_api.shared.enums.ProductivityTrend;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.exception.ResourceNotFoundException;
import com.lancea.studium.studium_api.repository.StudySessionRepository;
import com.lancea.studium.studium_api.repository.SubjectRepository;
import com.lancea.studium.studium_api.repository.UserRepository;
import com.lancea.studium.studium_api.util.UserDetailsUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
public class DataService {

    private static final double COMPLETION_WEIGHT = 0.40;
    private static final double CONSISTENCY_WEIGHT = 0.25;
    private static final double VOLUME_WEIGHT = 0.15;
    private static final double FOCUS_QUALITY_WEIGHT = 0.20;
    private static final int TREND_THRESHOLD = 5;

    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final InsightGenerator insightGenerator;


    public DataService(StudySessionRepository studySessionRepository,
                       UserRepository userRepository,
                       SubjectRepository subjectRepository,
                       InsightGenerator insightGenerator){
        this.studySessionRepository = studySessionRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.insightGenerator = insightGenerator;
    }

    //Frontend dashboard page API call
    public DashboardResponse retrieveDataNeededForDashboard(UserDetails userDetails) {

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        User user = userRepository.findById(userId).orElseThrow( () -> new ResourceNotFoundException("User not found"));

        int userCompletedSessionsForToday = studySessionRepository.
                countCompletedSessionsToday(userId, LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(LocalTime.MAX), SessionStatus.COMPLETED).intValue();

        Integer streak = user.getStreak();

        String lastSession = configureLastSession(user.getLastSession());

        String sessionsTodayValidationMessage =
                (userCompletedSessionsForToday > user.getHighestSession() || userCompletedSessionsForToday == user.getHighestSession()) ? "Personal best!" : " ";

        return new DashboardResponse(user.getFullName(), streak, lastSession, userCompletedSessionsForToday, sessionsTodayValidationMessage);

    }

    private String configureLastSession(LocalDate lastSessionDate){

        if(lastSessionDate.isBefore(LocalDate.now().minusYears(20)) || lastSessionDate.equals(LocalDate.now().minusYears(20))){
            return "None";
        }
        if(lastSessionDate.equals(LocalDate.now())){
            return "Today";
        }

        return ChronoUnit.DAYS.between(lastSessionDate, LocalDate.now()) + " days ago.";
    }

    public PagedResponse<SessionResponse> getStudySessionHistory(UserDetails userDetails, int page, int size ){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        //Create a Pageable object that contains the target page and size
        Pageable pageable = PageRequest.of(page, size);

        //Call the query that retrieves the user's session history and returns a Page
        Page<StudySession> sessionPage = studySessionRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);

        Page<SessionResponse> sessionResponseDTO = sessionPage.map(SessionResponse::from);

        return PagedResponse.from(sessionResponseDTO);
    }

    //Frontend subjects page API call
    public SubjectsPageResponse getSubjectsAndItsInfos(UserDetails userDetails){
        long userId = UserDetailsUtils.extractUserId(userDetails);

        long totalSubjects = subjectRepository.subjectCount(userId);
        long totalSessions = studySessionRepository.userSessionsCount(userId).intValue();
        long totalStudyTimeForAllTheSubject = subjectRepository.getUserTotalStudyTime(userId);

        String studyTime = String.format("%dh %dm", totalStudyTimeForAllTheSubject / 60, totalStudyTimeForAllTheSubject % 60 );

        return new SubjectsPageResponse(totalSubjects, totalSessions, studyTime);
    }

    public Long getUserSessionCount(UserDetails userDetails){
        long userId = UserDetailsUtils.extractUserId(userDetails);
        return studySessionRepository.userSessionsCount(userId);
    }

    /**
     * Collects the user's:
     * - Total completed sessions
     *  - Completion rate
     *  - Total focus minutes
     *  - Current streak
     *  - Longest streak
     * @param userDetails
     * @return aggregates these data through SummaryStatsDTO
     */
    public SummaryStatsDTO getUsersSummaryStats(UserDetails userDetails){
        Long userId = UserDetailsUtils.extractUserId(userDetails);

        User requestUser = userRepository.findById(userId)
                .orElseThrow( () -> new ResourceNotFoundException("User not found"));

        SummaryStatsProjection projection = studySessionRepository.fetchSummaryStats(userId);

        return new SummaryStatsDTO(projection.getTotalSessions(),
                projection.getCompletedSessions(),
                projection.getCompletionRate(),
                projection.getTotalFocusMinutes(),
                requestUser.getStreak(),
                requestUser.getLongestStreak(),
                computeFocusQuality(projection.getAverageInterruptions())
                );

    }

    /**
     * Takes the average interruption and compute for its focus quality rate
     * Used by getSummaryStats and computeScore
     * @param averageInterruption
     * @return the focusQuality based on the average interruption
     */
    private double computeFocusQuality(double averageInterruption){
        final int MAX_INTERRUPTIONS_THRESHOLD = 3;

        double raw = 1.0 - (averageInterruption / MAX_INTERRUPTIONS_THRESHOLD);
        double clamped = Math.max(raw, 0.0);
        return Math.round(clamped * 100.0) / 100.0;
    }

    /**
     * Returns the user's most productive hour for a certain time-period
     * along with the other hours when the user started a session
     * @param userDetails
     * @param numberDays
     * @return PeakHoursDTO that contains the user's peak hour along with the other hours
     */
    public PeakHoursDTO getUserPeakHours(UserDetails userDetails, Integer numberDays ){  //Within a week, a month, two months, six months, since the start
        Long userId = UserDetailsUtils.extractUserId(userDetails);

        LocalDateTime since = LocalDateTime.now().minusDays(numberDays);

        List<PeakHourProjection> peakHoursList = studySessionRepository.findPeakHoursByUser(userId, since);

        if(peakHoursList.isEmpty()){
            return new PeakHoursDTO(null, Collections.emptyList());
        }

        List<HourlyStatDTO> distribution = peakHoursList.stream().map(peakHour -> {
            return new HourlyStatDTO(insightGenerator.generatePeakHourInsight(peakHour.getHour()),
                   peakHour.getSessions(), peakHour.getCompletionRate());
        }).toList();

        return new PeakHoursDTO(insightGenerator.generatePeakHourInsight(peakHoursList.getFirst().getHour()), distribution );
    }

    /**
     * Retrieves the user's raw productivity data from the database
     * and compute the user's productivity score.
     * @param userDetails
     * @return ProductivityScore
     */

    public ProductivityScoreDTO getUserProductivityScore(UserDetails userDetails){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sixtyDaysAgo =LocalDateTime.now().minusDays(60);

        ProductivityRawProjection current = studySessionRepository.
                fetchRawUserProductivityData(userId, thirtyDaysAgo, now);
        ProductivityRawProjection previous = studySessionRepository.
                fetchRawUserProductivityData(userId, sixtyDaysAgo, thirtyDaysAgo);

        int currentScore = computeScore(current, userId);
        int previousScore = computeScore(previous, userId);

        ProductivityTrend productivityTrend = determineProductivityTrend(currentScore, previousScore);

        return new ProductivityScoreDTO(currentScore,
                COMPLETION_WEIGHT, CONSISTENCY_WEIGHT,
                VOLUME_WEIGHT, FOCUS_QUALITY_WEIGHT, productivityTrend);

    }

    /**
     * Helper method that computes the productivity score of the
     * user based on the rawProjection data fetch from the database
     * @param rawProjection
     * @param userId
     * @return The user's score
     */

    private int computeScore(ProductivityRawProjection rawProjection, Long userId){
        if(rawProjection.getTotalSessions() == null || rawProjection.getTotalSessions() == 0) return 0;

        double completionRate = rawProjection.getCompletionRate() != null ?
                rawProjection.getCompletionRate() : 0.0;
        double consistencyRate = rawProjection.getConsistencyRate() != null ?
                rawProjection.getConsistencyRate() : 0.0;
        double focusQuality = rawProjection.getAverageInterruptions() != null ?
                computeFocusQuality(rawProjection.getAverageInterruptions()) : 0.0;
        Long userTargetSessionPerWeek = subjectRepository.userTargetSessionPerWeek(userId);

        //Volume score: sessions completed this past period divided by session target for 4 weeks
        // Clamped to 1.0 so exceeding the target doesn't inflate the score
        int periodTarget =  userTargetSessionPerWeek.intValue() * 4;
        double volumeRate = Math.min(rawProjection.getTotalSessions().doubleValue() / periodTarget, 1.0) ;

        double rawScore = (completionRate * COMPLETION_WEIGHT) +
                            (consistencyRate * CONSISTENCY_WEIGHT) +
                            (volumeRate * VOLUME_WEIGHT) +
                            (focusQuality * FOCUS_QUALITY_WEIGHT);

        return (int) Math.round(rawScore * 100);
    }

    /**
     * Returns the user's productivity trend (IMPROVING, STABLE, IMPROVING) based on their score
     * @param currentScore
     * @param previousScore
     * @return user's ProductivityTrend
     */

    private ProductivityTrend determineProductivityTrend(int currentScore, int previousScore){
        int delta = currentScore - previousScore;
        if(delta >= TREND_THRESHOLD) return ProductivityTrend.IMPROVING;
        if(delta <= -TREND_THRESHOLD) return  ProductivityTrend.DECLINING;
        return ProductivityTrend.STABLE;
    }

    /**
     * Provides a detailed breakdown of the user's session for a certain period
     * @param userDetails
     * @param period
     * @return BreakDownDTO that aggregates these data in one DTO
     */

    public BreakDownDTO getBreakDown(UserDetails userDetails, BreakDownPeriod period){

        Long userId = UserDetailsUtils.extractUserId(userDetails);

        final int DAILY_LOOKBACK_DAYS = 30;
        final int WEEKLY_LOOKBACK_DAYS = 12;
        final int MONTHLY_LOOKBACK_DAYS = 12;


        LocalDateTime since = switch (period){
            case DAILY -> LocalDateTime.now().minusDays(DAILY_LOOKBACK_DAYS);
            case WEEKLY -> LocalDateTime.now().minusWeeks(WEEKLY_LOOKBACK_DAYS);
            case MONTHLY -> LocalDateTime.now().minusMonths(MONTHLY_LOOKBACK_DAYS);
        };

        List<BreakDownProjection> breakDowns = switch (period){
            case DAILY -> studySessionRepository.findDailyBreakDown(userId, since);
            case WEEKLY -> studySessionRepository.findWeeklyBreakdown(userId, since);
            case MONTHLY -> studySessionRepository.findMonthlyBreakDown(userId, since);
        };

        List<BreakDownEntryDTO> breakDownEntries = breakDowns.stream()
                .map( breakDownEntry -> new BreakDownEntryDTO(breakDownEntry.getPeriodStart().toLocalDate(),
                        breakDownEntry.getSessions(), breakDownEntry.getFocusMinutes(), breakDownEntry.getCompletionRate())).toList();

        return new BreakDownDTO(period, breakDownEntries);
    }

    /**
     * Retrieves the user's target session per week and how much they have complted so far.
     * Sum them all up as one and compute for the overall percentage
     * @param userDetails
     * @return a GoalProgressDTO which aggregates all of this information.
     */

    public GoalProgressDTO getUserWeeklyGoals(UserDetails userDetails){
        Long userId = UserDetailsUtils.extractUserId(userDetails);
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        List<SubjectGoalProjection> currentWeeklyProgressPerSubject = subjectRepository.findSubjectGoalProgress(userId, weekStart);

        if(currentWeeklyProgressPerSubject.isEmpty()){
            return new GoalProgressDTO(0, 0L, 0.0, Collections.emptyList());
        }

        List<SubjectGoalDTO> weeklyProgressionPerSubject = currentWeeklyProgressPerSubject
                .stream()
                .map( subjectProgress ->{

                    double progress = subjectProgress.getWeeklyGoal() > 0 ?
                            Math.min(subjectProgress.getCompletedThisWeek().doubleValue() / subjectProgress.getWeeklyGoal(), 1.0)
                            : 0.0;


                    return new SubjectGoalDTO(subjectProgress.getSubjectId(),
                            subjectProgress.getSubjectName(),
                            subjectProgress.getWeeklyGoal(),
                            subjectProgress.getCompletedThisWeek(),
                            progress);
                }).toList();

        int totalWeeklyGoal = weeklyProgressionPerSubject.stream()
                .mapToInt(SubjectGoalDTO::weeklyGoal)
                .sum();

        long totalCompletedThisWeek = weeklyProgressionPerSubject.stream()
                .mapToLong(SubjectGoalDTO::completedThisWeek)
                .sum();

        double weeklyProgress = totalWeeklyGoal > 0 ?
                Math.min( (double) totalCompletedThisWeek / totalWeeklyGoal , 1.0)
                : 0.0;

        return new GoalProgressDTO(totalWeeklyGoal,
                totalCompletedThisWeek,
                weeklyProgress,
                weeklyProgressionPerSubject);

    }

}
