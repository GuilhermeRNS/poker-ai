package com.github.chen0040.jrl.poker.domain;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum Round {
    PRE_FLOP(0),
    FLOP(3),
    TURN(4),
    RIVER(5),
    OVER(5);

    public Round next(){
        switch (this){
            case PRE_FLOP:
                return FLOP;
            case FLOP:
                return TURN;
            case TURN:
                return RIVER;
            default:
                return OVER;
        }
    }

    Integer visibleCards;
}
