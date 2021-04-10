package com.github.chen0040.jrl.poker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Probs {
    private Double playProb;
    private Double raiseProb;

    public Probs(String playProbStr, String raiseProbStr) {
        playProb = Double.parseDouble(playProbStr);
        raiseProb = Double.parseDouble(raiseProbStr);
    }
}
