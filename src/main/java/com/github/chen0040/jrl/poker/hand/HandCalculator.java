package com.github.chen0040.jrl.poker.hand;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ordering.from;
import static com.google.common.collect.Ordering.natural;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class HandCalculator implements Comparable<HandCalculator> {
    private final PokerCategory pokerCategory;
    private Collection<PokerCard> cards;



    private final LinkedList<PokerRank> distinctPokerRanks = new LinkedList<>();

    public HandCalculator(Collection<PokerCard> pokerCards) {
        this.cards=pokerCards;
        checkArgument(pokerCards.size() == 5);
        Set<PokerSuit> pokerSuits = EnumSet.noneOf(PokerSuit.class);
        Multiset<PokerRank> pokerRanks = EnumMultiset.create(PokerRank.class);
        for (PokerCard pokerCard : pokerCards) {
            pokerSuits.add(pokerCard.pokerSuit);
            pokerRanks.add(pokerCard.pokerRank);
        }
        Set<Entry<PokerRank>> entries = pokerRanks.entrySet();
        for (Entry<PokerRank> entry : byCountThenRank.immutableSortedCopy(entries)) {
            distinctPokerRanks.addFirst(entry.getElement());
        }
        PokerRank first = distinctPokerRanks.getFirst();
        int distinctCount = distinctPokerRanks.size();
        if (distinctCount == 5) {
            boolean flush = pokerSuits.size() == 1;
            if (first.ordinal() - distinctPokerRanks.getLast().ordinal() == 4) {
                pokerCategory = flush ? PokerCategory.STRAIGHT_FLUSH : PokerCategory.STRAIGHT;
            }
            else if (first == PokerRank.ACE && distinctPokerRanks.get(1) == PokerRank.FIVE) {
                pokerCategory = flush ? PokerCategory.STRAIGHT_FLUSH : PokerCategory.STRAIGHT;
                // ace plays low, move to end
                distinctPokerRanks.addLast(distinctPokerRanks.removeFirst());
            }
            else {
                pokerCategory = flush ? PokerCategory.FLUSH : PokerCategory.HIGH_CARD;
            }
        }
        else if (distinctCount == 4) {
            pokerCategory = PokerCategory.ONE_PAIR;
        }
        else if (distinctCount == 3) {
            pokerCategory = pokerRanks.count(first) == 2 ? PokerCategory.TWO_PAIR : PokerCategory.THREE_OF_A_KIND;
        }
        else {
            pokerCategory = pokerRanks.count(first) == 3 ? PokerCategory.FULL_HOUSE : PokerCategory.FOUR_OF_A_KIND;
        }
    }

    @Override
    public final int compareTo(HandCalculator that) {
        return byCategoryThenRanks.compare(this, that);
    }

    private static final Ordering<Entry<PokerRank>> byCountThenRank;

    private static final Comparator<HandCalculator> byCategoryThenRanks;

    static {
        Comparator<Entry<PokerRank>> byCount = comparingInt(Entry::getCount);
        Comparator<Entry<PokerRank>> byRank = comparing(Entry::getElement);
        byCountThenRank = from(byCount.thenComparing(byRank));
        Comparator<HandCalculator> byCategory = comparing((HandCalculator hand) -> hand.pokerCategory);
        Function<HandCalculator, Iterable<PokerRank>> getRanks =
                (HandCalculator hand) -> hand.distinctPokerRanks;
        Comparator<HandCalculator> byRanks =
                comparing(getRanks, natural().lexicographical());
        byCategoryThenRanks = byCategory.thenComparing(byRanks);
    }

    @AllArgsConstructor
    @Getter
    public enum PokerCategory {
        HIGH_CARD(5,20),
        ONE_PAIR(15,25),
        TWO_PAIR(45,30),
        THREE_OF_A_KIND(70,40),
        STRAIGHT(85,50),
        FLUSH(92,70),
        FULL_HOUSE(97,70),
        FOUR_OF_A_KIND(99,70),
        STRAIGHT_FLUSH(100,70);

        private Integer winningOdds;
        private Integer raiseChance;

    }

    @Getter
    public enum PokerRank {
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE;
    }

    @Getter
    public enum PokerSuit {
        DIAMONDS,
        CLUBS,
        HEARTS,
        SPADES;
    }

    @Getter
    public enum PokerCard {
        TWO_DIAMONDS(202, PokerRank.TWO, PokerSuit.DIAMONDS),
        THREE_DIAMONDS(203, PokerRank.THREE, PokerSuit.DIAMONDS),
        FOUR_DIAMONDS(204, PokerRank.FOUR, PokerSuit.DIAMONDS),
        FIVE_DIAMONDS(205, PokerRank.FIVE, PokerSuit.DIAMONDS),
        SIX_DIAMONDS(206, PokerRank.SIX, PokerSuit.DIAMONDS),
        SEVEN_DIAMONDS(207, PokerRank.SEVEN, PokerSuit.DIAMONDS),
        EIGHT_DIAMONDS(208, PokerRank.EIGHT, PokerSuit.DIAMONDS),
        NINE_DIAMONDS(209, PokerRank.NINE, PokerSuit.DIAMONDS),
        TEN_DIAMONDS(210, PokerRank.TEN, PokerSuit.DIAMONDS),
        JACK_DIAMONDS(211, PokerRank.JACK, PokerSuit.DIAMONDS),
        QUEEN_DIAMONDS(212, PokerRank.QUEEN, PokerSuit.DIAMONDS),
        KING_DIAMONDS(213, PokerRank.KING, PokerSuit.DIAMONDS),
        ACE_DIAMONDS(214, PokerRank.ACE, PokerSuit.DIAMONDS),

        TWO_CLUBS(102, PokerRank.TWO, PokerSuit.CLUBS),
        THREE_CLUBS(103, PokerRank.THREE, PokerSuit.CLUBS),
        FOUR_CLUBS(104, PokerRank.FOUR, PokerSuit.CLUBS),
        FIVE_CLUBS(105, PokerRank.FIVE, PokerSuit.CLUBS),
        SIX_CLUBS(106, PokerRank.SIX, PokerSuit.CLUBS),
        SEVEN_CLUBS(107, PokerRank.SEVEN, PokerSuit.CLUBS),
        EIGHT_CLUBS(108, PokerRank.EIGHT, PokerSuit.CLUBS),
        NINE_CLUBS(109, PokerRank.NINE, PokerSuit.CLUBS),
        TEN_CLUBS(110, PokerRank.TEN, PokerSuit.CLUBS),
        JACK_CLUBS(111, PokerRank.JACK, PokerSuit.CLUBS),
        QUEEN_CLUBS(112, PokerRank.QUEEN, PokerSuit.CLUBS),
        KING_CLUBS(113, PokerRank.KING, PokerSuit.CLUBS),
        ACE_CLUBS(114, PokerRank.ACE, PokerSuit.CLUBS),

        TWO_HEARTS(302, PokerRank.TWO, PokerSuit.HEARTS),
        THREE_HEARTS(303, PokerRank.THREE, PokerSuit.HEARTS),
        FOUR_HEARTS(304, PokerRank.FOUR, PokerSuit.HEARTS),
        FIVE_HEARTS(305, PokerRank.FIVE, PokerSuit.HEARTS),
        SIX_HEARTS(306, PokerRank.SIX, PokerSuit.HEARTS),
        SEVEN_HEARTS(307, PokerRank.SEVEN, PokerSuit.HEARTS),
        EIGHT_HEARTS(308, PokerRank.EIGHT, PokerSuit.HEARTS),
        NINE_HEARTS(309, PokerRank.NINE, PokerSuit.HEARTS),
        TEN_HEARTS(310, PokerRank.TEN, PokerSuit.HEARTS),
        JACK_HEARTS(311, PokerRank.JACK, PokerSuit.HEARTS),
        QUEEN_HEARTS(312, PokerRank.QUEEN, PokerSuit.HEARTS),
        KING_HEARTS(313, PokerRank.KING, PokerSuit.HEARTS),
        ACE_HEARTS(314, PokerRank.ACE, PokerSuit.HEARTS),

        TWO_SPADES(402, PokerRank.TWO, PokerSuit.SPADES),
        THREE_SPADES(403, PokerRank.THREE, PokerSuit.SPADES),
        FOUR_SPADES(404, PokerRank.FOUR, PokerSuit.SPADES),
        FIVE_SPADES(405, PokerRank.FIVE, PokerSuit.SPADES),
        SIX_SPADES(406, PokerRank.SIX, PokerSuit.SPADES),
        SEVEN_SPADES(407, PokerRank.SEVEN, PokerSuit.SPADES),
        EIGHT_SPADES(408, PokerRank.EIGHT, PokerSuit.SPADES),
        NINE_SPADES(409, PokerRank.NINE, PokerSuit.SPADES),
        TEN_SPADES(410, PokerRank.TEN, PokerSuit.SPADES),
        JACK_SPADES(411, PokerRank.JACK, PokerSuit.SPADES),
        QUEEN_SPADES(412, PokerRank.QUEEN, PokerSuit.SPADES),
        KING_SPADES(413, PokerRank.KING, PokerSuit.SPADES),
        ACE_SPADES(414, PokerRank.ACE, PokerSuit.SPADES);

        private final Integer value;
        private final PokerRank pokerRank;
        private final PokerSuit pokerSuit;

        PokerCard(Integer value, PokerRank pokerRank, PokerSuit pokerSuit) {
            this.value=value;
            this.pokerRank = pokerRank;
            this.pokerSuit = pokerSuit;
        }

        public static PokerCard getByValue(Integer findValue){
            return Arrays.stream(values()).filter(s -> s.value.equals(findValue)).findAny().orElse(null);
        }
    }

}


