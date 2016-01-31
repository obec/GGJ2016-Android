package com.ggj2016.gregsbadday;

/**
 * Created by Sami on 1/30/16.
 */
public class Score {

    private int score;
    private String bodyPart;

    public Score(){
        bodyPart = "None";
        score = 0;
    }
    public Score(String bodyPart, int score) {
        this.bodyPart = bodyPart;
        this.score = score;
    }
    public void setOnlyScore(int newScore) {
        score = newScore;
    }
    public int getScore(){
        return score;
    }
    public void setBodyPart(String newBodyPart){
        bodyPart = newBodyPart;
    }
    public String getBodyPart(){
        return bodyPart;
    }

    public void setScore(String bodyPart, int score){
        this.bodyPart = bodyPart;
        this.score = score;
    }



}
