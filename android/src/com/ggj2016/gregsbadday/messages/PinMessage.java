package com.ggj2016.gregsbadday.messages;

public class PinMessage {

    private int head;
    private int body;
    private int arms;
    private int legs;
//    private int arm_right;
//    private int arm_left;
//    private int leg_right;
//    private int leg_left;

//    PinMessage(int head, int body, int armRight, int armLeft, int legRight, int legLeft) {
//        this.head = head;
//        this.body = body;
//        this.arm_right = armRight;
//        this.arm_left = armLeft;
//        this.leg_right = legRight;
//        this.leg_left = legLeft;
//    }

    public PinMessage(int head, int body, int arms, int legs) {
        this.head = head;
        this.body = body;
        this.arms = arms;
        this.legs = legs;
    }

}
