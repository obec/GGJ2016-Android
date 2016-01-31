package com.ggj2016.voudousnafu.messages;

import com.google.gson.annotations.SerializedName;

public class GameStateMessage {

    @SerializedName("next_level")
    int nextLevel;
    @SerializedName("current_level")
    int currentLevel;

    @SerializedName("arm_score")
    int armScore;
    @SerializedName("head_score")
    int headScore;
    @SerializedName("leg_score")
    int legsScore;
    @SerializedName("body_score")
    int bodyScore;
    @SerializedName("total_score")
    int totalScore;

}
