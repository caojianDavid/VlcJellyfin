package com.example.vlcjfy;

public class utils {
    public static String TrickToTime(long trick) {
        String time = "";
        long totalSeconds = trick / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return time;
    }
}
