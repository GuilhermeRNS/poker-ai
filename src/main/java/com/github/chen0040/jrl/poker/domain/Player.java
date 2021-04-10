package com.github.chen0040.jrl.poker.domain;

import com.github.chen0040.jrl.poker.hand.HandCalculator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.paukov.combinatorics3.Generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Slf4j
public class Player {
    private Hold hold;
    private Integer money;
    private HandCalculator bestHand;
    private Integer compromisedMoney;
    private boolean active;

    public void initialize(){
        hold.getCards().clear();
        hold.getCards().add(HandCalculator.PokerCard.getByValue(Game.getRandomCard().getValue()));
        hold.getCards().add(HandCalculator.PokerCard.getByValue(Game.getRandomCard().getValue()));
        this.active=true;
        this.compromisedMoney=0;
        this.bestHand = null;
    }

    public void calculateBestHand(Board board, Round betRound){
        if(Round.PRE_FLOP.equals(betRound)){
            return;
        }

        ArrayList<HandCalculator.PokerCard> boardList =
                new ArrayList<>(board.getCards().subList(0,betRound.visibleCards)
                        .stream()
                        .map(p -> HandCalculator.PokerCard.getByValue(p.getValue()))
                        .collect(Collectors.toList()));

        log.info("Board Cards [{}]", boardList);

        boardList.addAll(this.getHold().getCards());
        List<HandCalculator.PokerCard> result = Generator.combination(boardList)
                .simple(5)
                .stream().max(Comparator.comparing(HandCalculator::new)).orElse(null);
        log.info("Hold Cards [{}]", this.getHold());
        this.setBestHand(new HandCalculator(result));
    }

    public Integer decideRaiseValue(Integer raiseValue, Integer pot,Integer blind){
        Integer value = new Random().nextInt(100);

        if(raiseValue == 0){
            raiseValue = Math.max(pot/10,blind);
        }

        Integer newRaise = 0;

        if(value > 50){
            newRaise =  raiseValue*2;
        }else if(value < 50 && value > 15){
            newRaise = raiseValue*3;
        }else if(value < 15 && value > 3){
            newRaise = raiseValue*4;
        }else if(value < 3 && value >= 1){
            newRaise = raiseValue * 5;
        }else{
            newRaise = money; // All in
        }

        // can't raise more than money
        return newRaise > (money-compromisedMoney) ? ((money-compromisedMoney)) : newRaise;
    }

    //TODO: do
    //TODO: logic to compare with current money not in place as not relevant now
    //TODO: can/should improve raise chance by using position and the fact that there is a raise or not
    public Action decideAction(Integer raise, Round betRound, Board board){

        if(!this.active){
            return Action.Watch;
        }

        int random = new Random().nextInt(100);

        Double potRatio = 1.0;
        if(board.getRoundPot() != 0 && raise != 0){ // prevent initial conditions to cause chaos
            potRatio = (double) board.getRoundPot() / (double) (raise-this.compromisedMoney);
        }

        if(betRound.equals(Round.PRE_FLOP)) {
            String suited = (hold.getCards().get(0).getValue() / 100 == hold.getCards().get(1).getValue() / 100) ? "s" : "u";
            Probs prob = Game.PROB_CARD_MAP.get(hold.getCards().get(0).getValue() % 100 + "-" + hold.getCards().get(1).getValue() % 100 + suited);

            // Fold if prob wasnt met
            if (prob.getPlayProb().intValue() / potRatio < random) {
                if(raise > 0){
                    return Action.Fold;
                }
                return Action.Pass;
            }
            if (prob.getRaiseProb().intValue() / potRatio < random) {
                if (raise > 0) {
                    return Action.Pay;
                }
                return Action.Pass;
            }

            return Action.Raise;
        }

        if(this.bestHand.getPokerCategory().getWinningOdds()/potRatio < random){
            if(raise > 0){
                return Action.Fold;
            }
            return Action.Pass;
        }
        random = new Random().nextInt(100);
        if(this.bestHand.getPokerCategory().getRaiseChance()/potRatio < random){
            if(raise > 0){
                return Action.Pay;
            }
        }
        return Action.Raise;
    }

    private Action doActionOrGoCrazy(Action action){
        int random = new Random().nextInt(100);
        if(random < 5){ // 5% chance of going crazy
            return Action.Raise;
        }

    }
}
