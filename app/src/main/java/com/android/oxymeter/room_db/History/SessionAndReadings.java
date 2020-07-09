package com.android.oxymeter.room_db.History;

import java.util.List;

public class SessionAndReadings {

    long id;

    String user_id;

    int is_self; //0 = false, 1 = true

    long start_time;

    long end_time;

    long duration;

    double average_pulse;

    double average_spo2;

    double average_pi;

    int is_sync; //0 = false, 1 = true

    String session_id;

    List<Readings> readingsTables;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getIs_self() {
        return is_self;
    }

    public void setIs_self(int is_self) {
        this.is_self = is_self;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getAverage_pulse() {
        return average_pulse;
    }

    public void setAverage_pulse(double average_pulse) {
        this.average_pulse = average_pulse;
    }

    public double getAverage_spo2() {
        return average_spo2;
    }

    public void setAverage_spo2(double average_spo2) {
        this.average_spo2 = average_spo2;
    }

    public double getAverage_pi() {
        return average_pi;
    }

    public void setAverage_pi(double average_pi) {
        this.average_pi = average_pi;
    }

    public int getIs_sync() {
        return is_sync;
    }

    public void setIs_sync(int is_sync) {
        this.is_sync = is_sync;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public List<Readings> getReadingsTables() {
        return readingsTables;
    }

    public void setReadingsTables(List<Readings> readingsTables) {
        this.readingsTables = readingsTables;
    }

    public static class Readings {

        int pulse;

        int spo2;

        double pi_data;

        public int getPulse() {
            return pulse;
        }

        public void setPulse(int pulse) {
            this.pulse = pulse;
        }

        public int getSpo2() {
            return spo2;
        }

        public void setSpo2(int spo2) {
            this.spo2 = spo2;
        }

        public double getPi_data() {
            return pi_data;
        }

        public void setPi_data(double pi_data) {
            this.pi_data = pi_data;
        }
    }
}
