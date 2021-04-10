package com.github.chen0040.jrl.poker.domain;

import com.github.chen0040.jrl.poker.hand.HandCalculator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Hold {
    private List<HandCalculator.PokerCard> cards = new ArrayList<>();
}
