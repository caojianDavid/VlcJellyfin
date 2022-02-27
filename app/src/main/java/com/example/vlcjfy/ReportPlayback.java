package com.example.vlcjfy;


import android.util.Log;

import java.io.IOException;

public class ReportPlayback {
    public static String TAG = "ReportPlayback";

    public static void ReportPlaybackProgress(String baseUrl, String Id, boolean paused, long PositionTicks,String token) {
        String json = "{'itemId' : '" + Id + "','canSeek' : 'true','isPaused':'" + paused + "','isMuted':'false',";
        json += "'positionTicks': '" + PositionTicks * 10000 + "'}";
        String url = baseUrl + "/Sessions/Playing/Progress";
        Log.d(TAG, "ReportPlaybackProgress: " + json);
        try {
            HttpClient.doPost(url, json,token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ReportPlaybackStop(String baseUrl, String Id, long PositionTicks,String token) {
        String url = baseUrl + "/Sessions/Playing/Stopped";
        String json = "{'Id':'" + Id + "','PositionTicks':'" + PositionTicks * 10000 + "'}";
        Log.d(TAG, "ReportPlaybackStop: " + json);
        try {
            HttpClient.doPost(url, json,token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
