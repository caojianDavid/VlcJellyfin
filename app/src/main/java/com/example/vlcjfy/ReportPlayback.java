package com.example.vlcjfy;


import java.io.IOException;

public class ReportPlayback {

    public static void ReportPlaybackProgress(String baseUrl,String Id,boolean paused,long PositionTicks){
        String url = baseUrl + "Sessions/Playing/Progress";
        String json = "{'itemId': '"+Id+"','canSeek' : 'true','isPaused':'"+paused+"','isMuted':'false',";
        json += "'positionTicks': '"+PositionTicks+"'}" ;
        try {
            HttpClient.doPost(url,json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ReportPlaybackStop(String baseUrl,String Id,long PositionTicks){
        String url = baseUrl + "Sessions/Playing/Stopped";
        String json = "{'Id':'"+Id+"','PositionTicks':'"+PositionTicks+"'}";
        try {
            HttpClient.doPost(url,json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
