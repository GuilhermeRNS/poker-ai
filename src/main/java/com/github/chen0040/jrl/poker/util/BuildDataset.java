package com.github.chen0040.jrl.poker.util;

import com.github.chen0040.jrl.poker.hand.HandCalculator;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class BuildDataset {
    public static void main(String args[]) throws IOException {
        URL file = HandCalculator.class.getResource("/handProbabilities.csv");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/handProbs.csv"))){
            Files.lines(Paths.get(file.getPath()))
                    .forEachOrdered(l ->  {
                        String[] rows = l.split(",");
                        String card1 = rows[0].split("/")[0].trim();
                        String card2 = rows[0].split("/")[1].trim();
                        Double winPct = Double.parseDouble(rows[2].replace("%","").trim());
                        Double playProb = Double.min((winPct-5)*6,100.0);
                        Double raiseProb = Double.min((winPct-5)*3,100.0);
                        String suit = rows[1].trim().equals("suited") ? "s" : "u";
                        String value = getValueFromCard(card1).toString() +"-"+ getValueFromCard(card2).toString() + suit;
                        String valueRev = getValueFromCard(card2).toString() +"-"+ getValueFromCard(card1).toString() + suit;
                        try {
                            writer.write(String.join(",",value, playProb.toString(), raiseProb.toString()));
                            writer.newLine();
                            if(!value.equals(valueRev)){
                                writer.write(String.join(",",valueRev, playProb.toString(), raiseProb.toString()));
                                writer.newLine();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }catch (Exception e){
            log.error("Exception'", e);
        }
    }

    private static Integer getValueFromCard(String card) {
        switch(card){
            case "A":
                return 14;
            case "K":
                return 13;
            case "Q":
                return 12;
            case "J":
                return 11;
            case "T":
                return 10;
            default:
                return Integer.parseInt(card);
        }
    }
}
