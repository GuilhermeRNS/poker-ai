package com.github.chen0040.jrl.poker.domain;

import com.github.chen0040.jrl.poker.hand.HandCalculator;
import com.github.chen0040.rl.learning.qlearn.QLearner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.paukov.combinatorics3.Generator;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class Game {
    private int dealerPos = 0;
    private Integer blind = 20;
    private Integer currentRaise = 20;
    private Round betRound = Round.PRE_FLOP;
    private List<Player> playerList;
    private Board board = new Board();
    private static HashMap<Integer,Card> usedCards = new HashMap<>();
    private Integer raisePosition = 2;
    public static final HashMap<String,Probs> PROB_CARD_MAP = new HashMap<>();

    private static Integer STARTING_MONEY = 10000;

    private void resetPlayers(){
        playerList = new ArrayList<>();
        for(int i=0;i < 9; i++){
            playerList.add(new Player(new Hold(),STARTING_MONEY,null,0,true));
        }
    }

    public Game() {
        //populate Hashap
        URL file = HandCalculator.class.getResource("/handProbs.csv");
        try {
            Files.lines(Paths.get(file.getPath())).forEach(l -> {
                String[] columns = l.split(",");
                PROB_CARD_MAP.put(columns[0],new Probs(columns[1],columns[2]));
            });
        } catch (IOException e) {
            log.error("Failed to populate prob map");
        }
    }

    public void bet(Player player, Integer betValue){
        Integer previousComprisedMoney = player.getCompromisedMoney();
        player.setCompromisedMoney(betValue);
        board.setRoundPot(board.getRoundPot() + (betValue-previousComprisedMoney));
    }

    public void finalizeRound(){
        playerList.forEach(p -> {
            board.setPot(board.getPot() + p.getCompromisedMoney());
            p.setMoney(p.getMoney() - p.getCompromisedMoney());
        });
        raisePosition = dealerPos;
        currentRaise = 0;
        betRound = betRound.next();
        log.info("Finalizing round with current pot [{}]", board.getPot());
    }

    public void finalizeGame(){
        Player winner = null;
        if(playerList.stream().filter(Player::isActive).count() == 1){
            winner = playerList.stream().filter(Player::isActive).findAny().orElse(null);
        }else{
            List<Player> competitors = playerList.stream().filter(Player::isActive).collect(Collectors.toList());

            for (Player competitor : competitors) {
                competitor.calculateBestHand(board,Round.RIVER);
                log.info("Player has [{}] with the cards [{}]", competitor.getBestHand().getPokerCategory(),competitor.getBestHand().getCards());
            }

            winner = competitors.stream().max(Comparator.comparing(Player::getBestHand)).orElse(null);
        }
        if(winner != null){
            if(winner.getBestHand() != null){
                log.info("Winner has hand [{}] [{}]", winner.getBestHand().getCards(), winner.getBestHand().getPokerCategory());
            }
            winner.setMoney(board.getPot() + winner.getMoney());
            log.info("Winner has now [{}]", winner.getMoney());
        }
        board.setPot(0);
        betRound = Round.PRE_FLOP;
        dealerPos = 0;
        blind = 20;
        currentRaise = 20;
        board = new Board();
        usedCards = new HashMap<>();
        raisePosition = 2;
        playerList = playerList.stream().filter(p -> p.getMoney() > 0).collect(Collectors.toList());
    }


    public static Card getRandomCard(){
        int number;
        int suit;
        Card randomCard;
        do{
            number = new Random().nextInt(13) + 2; // A is 14
            suit = new Random().nextInt(4) +1;
            randomCard = new Card(number,100*suit);
        } while (usedCards.putIfAbsent(randomCard.getValue(),randomCard) != null);
        return randomCard;
    }


    public double test(QLearner model, Integer episodes) {
//        QBot bot1 = new QBot(1, board, model);
//        NaiveBot bot2 =new NaiveBot(2, board);
        //TODO: initialize bots

        int wins = 0;
        int loses = 0;
        for(int i=0; i < episodes; ++i) {
            //reset board and players for EPISODE
            board = new Board();
            this.resetPlayers();
            dealerPos = 0;

            while(playerList.size() > 1){
                //reset for GAME
                raisePosition = Math.min(dealerPos + 2, playerList.size()-1); // Big Blind 1st round
                board.initialize();
                playerList.forEach(Player::initialize);

                log.info("Iteration: {} / {}", (i+1), episodes);
                // Wait until only 1 player remains
                while (playerList.stream().filter(Player::isActive).count() > 1 && !Round.OVER.equals(betRound)) {

                    playerList.forEach(p -> p.setCompromisedMoney(0));
                    switch (betRound){
                        case PRE_FLOP:
                            log.info("Starting betRound 1");
                            if(playerList.size() == 2){ // prob can do better than this
                                bet(playerList.get(dealerPos),blind/2);
                                bet(playerList.get(dealerPos+1),blind);
                                playRound(dealerPos);
                            }else if(playerList.size() == 3){ // prob can do better than this
                                bet(playerList.get(dealerPos + 1),blind/2);
                                bet(playerList.get(dealerPos + 2),blind);
                                playRound(dealerPos);
                            }else{
                                bet(playerList.get(dealerPos+1),blind/2);
                                bet(playerList.get(dealerPos+2),blind);
                                playRound(dealerPos + 3);
                            }
                            break;
                        case FLOP:
                            log.info("Starting betRound 2");
                            playerList.stream().filter(Player::isActive).forEach(p -> p.calculateBestHand(board,betRound));
                            playRound(dealerPos + 1);
                            break;
                        case TURN:
                            log.info("Starting betRound 3");
                            playerList.stream().filter(Player::isActive).forEach(p -> p.calculateBestHand(board,betRound));
                            playRound(dealerPos + 1);
                            break;
                        case RIVER:
                            log.info("Starting betRound 4");
                            playerList.stream().filter(Player::isActive).forEach(p -> p.calculateBestHand(board,betRound));
                            playRound(dealerPos + 1);
                            break;
                        default:
                            log.info("No betRound created");
                            break;
                    }

                }
                finalizeGame();
            }

            log.info("Player [{}] won the game", playerList.get(0));
            //int winner = board.getWinner();
//            log.info("Winner: {}", winner);
//            wins += winner == 1 ? 1 : 0;
//            loses += winner == 2 ? 1 : 0;
        }

        return wins * 1.0 / episodes;

    }

    private void playRound(Integer activePlayerPos) {
        do{
            Player activePlayer = playerList.get(activePlayerPos);
            Action action = activePlayer.decideAction(currentRaise,betRound,board);
            switch (action){
                case Fold:
                    activePlayer.setActive(false);
                    log.info("Player [{}] FOLDED", activePlayerPos);
                    break;
                case Pass:
                    log.info("Player [{}] PASSED", activePlayerPos);
                    break;
                case Pay:
                    bet(activePlayer,currentRaise);
                    log.info("Player [{}] decided to PAY [{}]", activePlayerPos, currentRaise);
                    break;
                case Raise:
                    Integer newRaise = activePlayer.decideRaiseValue(currentRaise,board.getPot(),blind);
                    bet(activePlayer,newRaise);
                    currentRaise = newRaise;
                    raisePosition = activePlayerPos;
                    log.info("Player [{}] decided to RAISE [{}]", activePlayerPos,newRaise);
                    break;
                case Watch:
                    // log.debug("Player [{}] is just watching", activePlayerPos);
                    break;
                default:
                    log.error("Action not defined");
                    break;
            }
            activePlayerPos = (activePlayerPos+1)%playerList.size();
        }
        while(!raisePosition.equals(activePlayerPos));
        this.finalizeRound();
    }


}
