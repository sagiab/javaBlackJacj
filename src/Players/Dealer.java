package Players;

import Cards.*;
import StatisticTools.StatisticObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the Dealer of a Blackjack game.
 * Dealer must stand on 17 or over and can only accept an Ace as 11.
 *
 * @author David Winter
 */
public class Dealer extends BlackjackPlayer
{
    /**
     * The Deck of cards used for the game. The Dealer is in complete control
     * of the Deck.
     */
    private Deck deck;
    
    public DealerCardHand hand = new DealerCardHand();
    
    /**
     * Whether or not the Dealer has dealt the initial two cards.
     */
    private boolean firstDeal = true;
    
    /**
     * The value the dealer must stand on.
     */
    public static final int DEALER_STANDS_ON = 17;
    public static final int CARD_PACKS = 2;
    
    private boolean gameOver = true;
    private boolean cardsFaceUp = false;
    
    /**
     * Whether the player is allowed to double at this stage in game.
     */
    private boolean playerCanDouble = true;
    
    private String said = "Please place your wager.";
    
    /**
     * Default constructor that creates a new dealer with a deck of
     * 2 card packs.
     */
    public Dealer()
    {
        super("Le Chiffre", 45, "male");
        
        deck = new Deck(CARD_PACKS);
    }
    
    public void say(String announcement)
    {
        said = announcement;
        System.out.println(said);
    }
    
    public String says()
    {
        return said;
    }
    
    public boolean isGameOver()
    {
        return gameOver;
    }
    
    public boolean areCardsFaceUp()
    {
        return cardsFaceUp;
    }
    
    /**
     * Acknowledge the bet from the player.
     *
     * @param   player  The player placing the bet.
     * @param   bet     The amount for the bet.
     */
    public boolean acceptBetFrom(Player player, double bet)
    {
        boolean betSet = player.setBet(bet);
        
        if (player.betPlaced())
        {
            say("Thank you for your bet of $" + player.getBet() + ". Would you like me to deal?");
        }
        else
        {
            say("Please place your bet.");
        }
        
        return (betSet) ? true : false;
    }
    
    /**
     * Deals initial two cards to player and self.
     * 
     * @param   player  The player to deal cards to.
     *
     * @return True if cards were dealt, otherwise false.
     */
    public boolean deal(Player player)
    {
        boolean cardsDealt = false;
        
        if (player.betPlaced() && !player.isBankrupt())
        {   
            gameOver = false;
            cardsFaceUp = false;

            playerCanDouble = true;
            
            player.hand = new PlayerCardHand();
            hand = new DealerCardHand();
            
            say("Initial deal made.");
            
            player.hand.add(deck.deal());
            this.hand.add(deck.deal());
            
            player.hand.add(deck.deal());
            this.hand.add(deck.deal());
            
            cardsDealt = true;
            firstDeal = false; 
            
            if (player.hand.hasBlackjack())
            {
                say("Blackjack!");
                go(player);
            }
            
        }
        else if (!player.betPlaced())
        {
            say("Please place your bets.");
            gameOver = true;
        }
        
        return cardsDealt;
    }
    
    /**
     * Player requests another card.
     *
     * @param player The player requesting another card.
     */
    public StatisticObject hit(Player player)
    {
        String s = "HIT";
        StatisticObject statisticObject = DateToStatistics(player, s);


        say(player.getName() + " hits.");
        player.hand.add(deck.deal());
        
        playerCanDouble = false;
        
        if (player.hand.isBust())
        {
            say(player.getName() + " busts. Loses $" + player.getBet());
            player.loses();
            gameOver = true;
        }
        return statisticObject;
    }

    private StatisticObject DateToStatistics(Player player, String moveType) {
        Deck duplicatedDeck = new Deck();
        duplicatedDeck = deck;
        DealerCardHand dealerCardHand = this.getHand();
        Card dealerOpenedCard =  dealerCardHand.get(1);
        int dealerCardToOfek = dealerOpenedCard.getValue();
        Card dealerClosedCard =  dealerCardHand.get(0);
        dealerOpenedCard.getFace().getValue();
        int[] ofekDeck = convertSagiDackToOfekDack(duplicatedDeck, dealerClosedCard);
        PlayerCardHand playerCardHand = player.getHand();
        List<Integer> playerHand = convertToOfekHand(playerCardHand);

        StatisticObject s = new StatisticObject();
        s.setDealerOpenCard(dealerCardToOfek);
        s.setDec(ofekDeck);
        s.setPlayerHand(playerHand);
        s.setPlayerMove(moveType);
        return s;
    }


    /**
     * Player would like to place a bet up to double of his original and
     * have the dealer give him one more card.
     *
     * @param player The player requesting to play double.
     */
    public StatisticObject playDouble(Player player)
    {
        String s = "DOUBLE";
        StatisticObject statisticObject = DateToStatistics(player, s);

        if (player.doubleBet() && playerCanDouble)
        {
            player.hand.add(deck.deal());
            say(player.getName() + " plays double.");
            
            if (player.hand.isBust())
            {
                say(player.getName() + " busts. Loses $" + player.getBet());
                player.loses();
                gameOver = true;
            }
            else
            {
                go(player);
            }
        }
        else
        {
            say(player.getName() + ", you can't double. Not enough money.");
        }
        return statisticObject;
    }
    
    /**
     * The player wishes to stand. The dealer then takes his go.
     *
     * @param player The player who wishes to stand.
     */
    public StatisticObject stand(Player player)
    {
        String s = "STAND";
        StatisticObject statisticObject = DateToStatistics(player, s);

        say(player.getName() + " stands. " + this.getName() + " turn.");
        go(player);
        return statisticObject;
    }
    
    /**
     * The dealers turn.
     *
     * @param player The opposing player of the dealer.
     */
    private void go(Player player)
    {
        cardsFaceUp = true;
        
        if (!hand.hasBlackjack())
        {
            while (hand.getTotal() < DEALER_STANDS_ON)
            {
                hand.add(deck.deal());
                say(this.getName() + " hits.");
            }
            
            if (hand.isBust())
            {
                say(this.getName() + " is BUST");
            }
            else
            {
                say(this.getName() + " stands on " + hand.getTotal());
            }            
        }
        else
        {
            say(this.getName() + " has BLACKJACK!");
        }
        
        if (hand.hasBlackjack() && player.hand.hasBlackjack())
        {
            say("Push");
            player.clearBet();
        }
        else if (player.hand.hasBlackjack())
        {
            double winnings = (player.getBet() * 3) / 2;
            say(player.getName() + " wins with Blackjack $" + winnings);
            player.wins(player.getBet() + winnings);
        }
        else if (hand.hasBlackjack())
        {
            say("Dealer has Blackjack. " + player.getName() + " loses $" + player.getBet());
            player.loses();
        }
        else if (hand.isBust())
        {
            say("Dealer is bust. " + player.getName() + " wins $" + player.getBet());
            player.wins(player.getBet() * 2);
        }
        else if (player.hand.getTotal() == hand.getTotal())
        {
            say("Push");
            player.clearBet();
        }
        else if (player.hand.getTotal() < hand.getTotal())
        {
            say(player.getName() + " loses $" + player.getBet());
            player.loses();
        }
        else if (player.hand.getTotal() > hand.getTotal())
        {
            say(player.getName() + " wins $" + player.getBet());
            player.wins(player.getBet() * 2);
        }
        
        gameOver = true;
    }
    
    public int cardsLeftInPack()
    {
        return deck.size();
    }
    
    public void newDeck()
    {
        deck = new Deck(CARD_PACKS);
    }
    
    public boolean canPlayerDouble(Player player)
    {
        return (playerCanDouble && player.canDouble()) ? true : false;
    }
    
    public DealerCardHand getHand()
    {
        return hand;
    }

    private int[] convertSagiDackToOfekDack(Deck deck, Card closeCared){
        int deckSize = deck.size();
        int[] res = new int[deckSize];
        while(!deck.isEmpty()){
            Card c = deck.pop();
            res[c.getValue()]++;
        }
        res[closeCared.getValue()]++;

        return res;
    }

    private List<Integer> convertToOfekHand(PlayerCardHand playerCardHand) {
        List<Integer> res = new ArrayList<>();
        for(Card c : playerCardHand){
            res.add(c.getValue());
        }
        return  res;
    }

}