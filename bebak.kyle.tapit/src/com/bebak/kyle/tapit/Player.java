package com.bebak.kyle.tapit;

import java.util.ArrayList;

import processing.core.PImage;

public class Player {

  /**
	 * 
	 */
	private final TapIt sketch;
/*********************************
   The Player is responsible for displaying everything in the game, while the main class
   is responsible for flow control. Player's Deck is simply a pointer to the deck
   instantiated in the main routine, and player and deck position and dimensions are specified
   in the main class
   *********************************/

  private Deck deck; // simply a pointer to the deck instantiated in the main class
  private Card card;
  private float x; // position of player's card
  private float y;

  private int score;
  private boolean locked; // for rotating player's card
  private Timer timer;
  private int timeLimit;
  private int fixedTime;
  private boolean displayFixed; // for fixing display and ensuring committed score is identical to displayed score

  private ArrayList<FadingText> ft; // fading text when player either wins or loses a point

    private float r; // radius of circular card
  private float textSize = .038f; // * width
  private final float rotationSpeed = .00001f;
  private final int textColor = Utils.color(255);
  private final int correctTextColor = Utils.color(0, 255, 0);
  private final int wrongTextColor = Utils.color(255, 0, 0);

  private PImage back; // back arrow image
  private float backX = 1.05f; // * r + x
  private float backY = .75f; // * backDim
  private float backDim = .08f; // * width

  private float scoreX = .85f; // * r + x 
  private float scoreY = .9f; // * r + y

  private final int BG = Utils.color(50); // background color for board


  // constructor
  public Player(TapIt tapIt, float x, float y, Card c, Deck d, int timeLimit) {

    sketch = tapIt;
	/********** Set dimensions of back arrow and score, relative to player card **********/
    this.x = x;
    this.y = y;
    r = TapIt.cardRadius;
    r *= sketch.width;

    back = sketch.loadImage(TapIt.path + "images/back.png");
    backDim *= sketch.width;
    backX = backX * r + x;
    backY *= backDim;
    scoreX = scoreX * r + x;
    scoreY = scoreY * r + y;

    textSize *= sketch.width;


    /********** Initialize timer, score, player card and deck **********/
    this.initialize();
    card = c;
    card.changePosition(x, y);
    deck = d;
    ft = new ArrayList<FadingText>();

    score = 0;
    this.timeLimit = timeLimit;
    timer = new Timer(sketch, timeLimit);
    timer.start();
    displayFixed = false;
  }


  public void giveCard(Card c) {
    c.changePosition(x, y);
    card = c;
  }

  public void removeCard() {
    card = null;
  }

  public void giveDeck(Deck d) {
    deck = d;
  }

  public Card getCard() {
    return card;
  }

  public void initialize() {
    timer = new Timer(sketch, timeLimit);
    timer.start();
    score = 0;
  }

  // has the player's time run out?
  public boolean timeIsUp() {
    return timer.timeIsUp();
  }

  public int getScore() {
    return score;
  }

  public int timeElapsed() {
    return timer.elapsedTime();
  }

  // for time trial mode, to ensure that committed time and final displayed time are the same 
  public void fixAndDisplayTime(int time) {
    fixedTime = time;
    displayFixed = true;
    displayAndUpdate();
  }





  // update card angle and display it, also display score and lives
  public void displayAndUpdate() {
    sketch.background(BG);
    deck.displayDeck();
    deck.displayTopFront();

    // display back arrow
    sketch.imageMode(TapIt.CENTER);
    sketch.image(back, backX, backY, backDim, backDim);

    // display player card
    card.updateTheta();
    card.displayFront();
    sketch.fill(textColor);
    sketch.textSize(textSize);
    sketch.text(score, scoreX, scoreY);

    if (TapIt.mode.equals(TapIt.SURVIVOR))
      sketch.text(timer.remainingTimeToStringMinutes(), scoreX, scoreY + textSize);

    if (TapIt.mode.equals(TapIt.TIME_TRIAL) && !displayFixed)
      sketch.text(timer.elapsedTimeToStringMinutes(), scoreX, scoreY + textSize);
    if (TapIt.mode.equals(TapIt.TIME_TRIAL) && displayFixed)
      sketch.text(timer.timeToStringMinutes(fixedTime), scoreX, scoreY + textSize);

    // display fading text for incrementing score
    for (int i = ft.size() - 1; i >=0; i--) {
      FadingText f = ft.get(i);
      if (f.isDead()) ft.remove(i);
      f.displayAndUpdate();
    }
  }



  // if player pushes mouse check to see what symbol in the deck
  // they have clicked on and update their score appropriately
  public void checkClick() {
    if (TapIt.dist(sketch.mouseX, sketch.mouseY, x, y) > r) 
      locked = true; // lock out rotation

    // in survivor mode, extraPerCard decreases as player gets more points, slowly approaching zero
    float epcMultiplier = TapIt.pow( (float) TapIt.nSyms, TapIt.extraTimeDecayOrder ) / 
    TapIt.pow( (float) TapIt.max(TapIt.nSyms, score), TapIt.extraTimeDecayOrder );

    int symbolIndex = deck.indexAtPosition(sketch.mouseX, sketch.mouseY);
    if (symbolIndex == -1) 
      return; // no symbol was clicked on


    /*********** CORRECT CLICK ************/
    if (card.hasSymbol(symbolIndex)) { 
      score++;

      if (TapIt.mode.equals(TapIt.SURVIVOR)) {
        timer.addTime(TapIt.round(TapIt.extraPerCard * epcMultiplier));
        ft.add(new FadingText(sketch, correctTextColor, "+" + TapIt.nf(TapIt.extraPerCard * epcMultiplier / (float) 1000, 1, 1), 
        x + sketch.random(-r, r), y + sketch.random(-r, r)));
      }
      if (TapIt.mode.equals(TapIt.TIME_TRIAL))
        ft.add(new FadingText(sketch, correctTextColor, "+1", x + sketch.random(-r, r), y + sketch.random(-r, r)));

      this.giveCard(deck.removeTop()); // consume a card from the deck

      TapIt.getPlayer().setMediaFile(TapIt.CORRECT); // play correct sound whenever player gets one right
      TapIt.getPlayer().start();
    } 

    /*********** WRONG CLICK ************/
    else {
      if (TapIt.mode.equals(TapIt.SURVIVOR))
        timer.subtractTime(TapIt.PENALTY);
      if (TapIt.mode.equals(TapIt.TIME_TRIAL))
        timer.addTimeCountUp(TapIt.PENALTY);

      ft.add(new FadingText(sketch, wrongTextColor, "-" + TapIt.nf(TapIt.PENALTY / (float) 1000, 1, 1), 
      x + sketch.random(-r, r), y + sketch.random(-r, r)));

      TapIt.getPlayer().setMediaFile(TapIt.WRONG); // play wrong sound whenever player gets one wrong
      TapIt.getPlayer().start();
    }
  }



  // player is checking for a click within the back arrow
  public boolean checkBack() {
    // check if player clicked back button to return to splash screen, calls method form main Tap_It program
    return TapIt.dist(sketch.mouseX, sketch.mouseY, backX, backY) < backDim / 2.0f;
  }

  // this is called in main class whenever mouse is dragged
  public void checkRotate() {
    if (locked) 
      return;
    float alpha = (sketch.mouseX - x) * (sketch.mouseY - sketch.pmouseY) - (sketch.mouseY - y) * (sketch.mouseX - sketch.pmouseX);
    card.incrementOmega(alpha * rotationSpeed);
  }

  public void checkRelease() {
    locked = false;
  }
}