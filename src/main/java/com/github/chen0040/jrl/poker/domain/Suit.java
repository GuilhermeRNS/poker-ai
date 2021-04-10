package com.github.chen0040.jrl.poker.domain;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum Suit {
    Clubs(100),
    Diamonds(200),
    Hearts(300),
    Spades(400);

    public static Suit getByWeight(Integer findWeight){
        return Arrays.stream(Suit.values()).filter(s -> s.weight.equals(findWeight)).findAny().orElse(null);
    }

    Integer weight;
}
