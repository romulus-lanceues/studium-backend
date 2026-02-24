package com.lancea.studium.studium_api.shared.interfaces;

import java.time.LocalDate;


//used by the User and StudySession entity for easier streak configuration
public interface Streakable {

    public LocalDate getLastSession();
    public void setLastSession(LocalDate lastSession);
    public void increaseStreak();
    public void setStreak(Integer streak);

}
