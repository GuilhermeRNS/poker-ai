package com.github.chen0040.jrl.poker;

import com.github.chen0040.jrl.poker.domain.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class SarsaUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(SarsaUnitTest.class);

    @Test
    public void basictest(){
        Game board = new Game();

        board.test(null,1);


    }

}
