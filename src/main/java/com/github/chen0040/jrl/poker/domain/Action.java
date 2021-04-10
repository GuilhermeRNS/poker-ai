package com.github.chen0040.jrl.poker.domain;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
public enum Action {
    Fold,
    Pass,
    Pay,
    Raise,
    Watch;

    private static final List<Action> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Action randomAction()  {
        Action action = VALUES.get(RANDOM.nextInt(SIZE));
        if(action.equals(Action.Watch)){ // can't do watch
            return randomAction();
        }
        return action;
    }
}
