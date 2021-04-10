package com.github.chen0040.jrl.poker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Card {
    private Integer number;
    private Suit suit;

    Card (Integer number, Integer weight){
        this.number = number;
        suit = Suit.getByWeight(weight);
    }

    public int getValue() {
        return number + suit.weight;
    }
}
