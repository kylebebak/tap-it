package bebak.kyle.tap_it;

import java.util.ArrayList;

import processing.core.PImage;


/*********************************
 * The Player is responsible for displaying everything in the game, while the main class
 * is responsible for flow control. Player's Deck is simply a pointer to the deck
 * instantiated in the main class, and player and deck position and dimensions are specified
 * in the main class
*/
public class Player {

  /**
	 * 
	 */
	private final MatchIt sketch;

  private Deck deck; // simply a pointer to the deck instantiated in the main class
  private Card card;
  private float x; // position of player's card
  private float y;

  private int score;
  private boolean locked; // for rotating player's card
  private Timer timer;
  private int timeLimit;
  private long fixedTime;
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


  /**
   * Constructor
   * 
   * @param d reference of game deck
   * @param c reference of card that is in player's possession (the one he can spin)
   * @param timeLimit only relevant if the game type is survivor. in time trial
   * the time limit can't run out
   */
  public Player(MatchIt matchIt, float x, float y, Card c, Deck d, int timeLimit) {

    sketch = matchIt;
	/********** Set dimensions of back arrow and score, relative to player card **********/
    this.x = x;
    this.y = y;
    r = MatchIt.cardRadius;
    r *= sketch.width;

    back = sketch.loadImage(MatchIt.path + "images/back.png");
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
    timer = new Timer(timeLimit);
    timer.start();
    displayFixed = false;
  }

  /** Give the player this card */
  public void giveCard(Card c) {
    c.changePosition(x, y);
    card = c;
  }
  
  /** Remove the player's reference to his current card */
  public void removeCard() {
    card = null;
  }

  public void giveDeck(Deck d) {
    deck = d;
  }
  
  /** return a pointer to player's card */
  public Card getCard() {
    return card;
  }
  
  /** restart timer and score */
  public void initialize() {
    timer = new Timer(timeLimit);
    timer.start();
    score = 0;
  }

  /** has the player's time run out? */
  public boolean timeIsUp() {
    return timer.timeIsUp();
  }

  public int getScore() {
    return score;
  }
  
  /** how much of player's time has elapsed? this is the player's score in time trial mode */
  public long timeElapsed() {
    return timer.elapsedTime();
  }

  /** for time trial mode, to ensure that committed time and final displayed time are the same */ 
  public void fixAndDisplayTime(long time) {
    fixedTime = time;
    displayFixed = true;
    displayAndUpdate();
  }





  /** update card angle and display it, also display score and lives */
  public void displayAndUpdate() {
    sketch.background(BG);
    deck.displayDeck(sketch.g); // this is reference of the active PGraphics from the sketch.
    // all PApplet objects have a PGraphics renderer called g
    deck.displayTopFront();

    // display back arrow
    sketch.imageMode(MatchIt.CENTER);
    sketch.image(back, backX, backY, backDim, backDim);

    // display player card
    card.updateTheta();
    card.displayFront();
    sketch.fill(textColor);
    sketch.textSize(textSize);
    sketch.text(score, scoreX, scoreY);

    if (MatchIt.mode.equals(MatchIt.SURVIVOR))
      sketch.text(timer.remainingTimeToStringMinutes(), scoreX, scoreY + textSize);

    if (MatchIt.mode.equals(MatchIt.TIME_TRIAL) && !displayFixed)
      sketch.text(timer.elapsedTimeToStringMinutes(), scoreX, scoreY + textSize);
    if (MatchIt.mode.equals(MatchIt.TIME_TRIAL) && displayFixed)
      sketch.text(timer.timeToStringMinutes(fixedTime), scoreX, scoreY + textSize);

    // display fading text for incrementing score
    for (int i = ft.size() - 1; i >=0; i--) {
      FadingText f = ft.get(i);
      if (f.isDead()) ft.remove(i);
      f.displayAndUpdate();
    }
  }



  /** if player pushes mouse check to see what symbol in the deck
   * they have clicked on and update their score appropriately
   */
  public void checkClick() {
    if (MatchIt.dist(sketch.mouseX, sketch.mouseY, x, y) > r) 
      locked = true; // lock out rotation

    // in survivor mode, extraPerCard decreases as player gets more points, slowly approaching zero
    float epcMultiplier = MatchIt.pow( (float) MatchIt.nSyms, MatchIt.extraTimeDecayOrder ) / 
    MatchIt.pow( (float) MatchIt.max(MatchIt.nSyms, score), MatchIt.extraTimeDecayOrder );

    int symbolIndex = deck.indexAtPosition(sketch.mouseX, sketch.mouseY);
    if (symbolIndex == -1) 
      return; // no symbol was clicked on


    /*********** CORRECT CLICK ************/
    if (card.hasSymbol(symbolIndex)) { 
      score++;

      if (MatchIt.mode.equals(MatchIt.SURVIVOR)) {
        timer.addTime(MatchIt.round(MatchIt.extraPerCard * epcMultiplier));
        ft.add(new FadingText(sketch, correctTextColor, "+" + MatchIt.nf(MatchIt.extraPerCard * epcMultiplier / (float) 1000, 1, 2), 
        x + sketch.random(-r, r), y + sketch.random(-r, r)));
      }
      if (MatchIt.mode.equals(MatchIt.TIME_TRIAL))
        ft.add(new FadingText(sketch, correctTextColor, "+1", x + sketch.random(-r, r), y + sketch.random(-r, r)));

      this.giveCard(deck.removeTop()); // consume a card from the deck

      MatchIt.getPlayer().setMediaFile(MatchIt.CORRECT); // play correct sound whenever player gets one right
      MatchIt.getPlayer().start();
    } 

    /*********** WRONG CLICK ************/
    else {
      if (MatchIt.mode.equals(MatchIt.SURVIVOR))
        timer.subtractTime(MatchIt.PENALTY);
      if (MatchIt.mode.equals(MatchIt.TIME_TRIAL))
        timer.addTimeCountUp(MatchIt.PENALTY);

      ft.add(new FadingText(sketch, wrongTextColor, "-" + MatchIt.nf(MatchIt.PENALTY / (float) 1000, 1, 2), 
      x + sketch.random(-r, r), y + sketch.random(-r, r)));

      MatchIt.getPlayer().setMediaFile(MatchIt.WRONG); // play wrong sound whenever player gets one wrong
      MatchIt.getPlayer().start();
    }
  }



  /** player is checking for a click within the back arrow */
  public boolean checkBack() {
    // check if player clicked back button to return to menu screen, calls method form main Tap_It program
    return MatchIt.dist(sketch.mouseX, sketch.mouseY, backX, backY) < backDim / 2.0f;
  }

  /** this is called in main class whenever mouse is dragged */
  public void checkRotate() {
    if (locked) 
      return;
    float alpha = (sketch.mouseX - x) * (sketch.mouseY - sketch.pmouseY) - (sketch.mouseY - y) * (sketch.mouseX - sketch.pmouseX);
    card.incrementOmega(alpha * rotationSpeed);
  }

  /** this is called in main class whenever mouse is released, it allows 
   * "lockout" to be implemented in the rotation of the card so that a user
   * can only rotate the card if he starts dragging within the card 
   */
  public void checkRelease() {
    locked = false;
  }
}