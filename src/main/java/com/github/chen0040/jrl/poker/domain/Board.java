package com.github.chen0040.jrl.poker.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Board {
    private List<Card> cards = new ArrayList();
    private Integer pot;
    private Integer roundPot;


    public void initialize(){
        for(int i=1; i <=5; i++){
            cards.add(Game.getRandomCard());
        }
        pot=0;
        roundPot=0;
    }
}
