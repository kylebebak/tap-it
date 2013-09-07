package com.bebak.kyle.tapit;

import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

import android.os.Environment;
import android.util.Log;
import apwidgets.APMediaPlayer;

public class TapIt extends PApplet {



 // for APMediaPlayer



/****************************************/

static final String DISPLAY_NAME = "Tap  It";
private static final String APPNAME = "TapIt";
static String sdPath = null;


static final int spcInitialIndex = 1;
static Integer[] spcNumbers = { 
  3, 4, 5, 6, 8, 9, 10
}; 

public static String SURVIVOR = "Survivor"; // static constants for modes of play
public static String TIME_TRIAL = "Time trial";
static final int modeInitialIndex = 0;
static String[] modes = {
  TIME_TRIAL, SURVIVOR
};


private static int spc; // default spc. symbols per card, 3 - 10 is allowed, with the exception of 7
private static String mode;

private static int nSyms; // number of cards in deck, also number of unique symbols in deck

static PImage[] allImgs;
private static String[] deckInfo;
private static final int nImages = 100;


private Deck deck;
private ArrayList<Card> cards;
private float dx = .71f; // deck position, * width
private float dy = .48f; // * height

private Player p1;
private float px = .235f; // player position, * width
private float py = .48f; // * height
private PFont font; // THIS IS THE ONLY FONT IN THE ENTIRE APPLICATION, IT DOESN'T CHANGE
static final float cardRadius = .22f; // * width, multiplication done in subclass constructors


private Splash splash; // this class manages the flow of the game, read more in its documentation
private Scores scores;
private boolean commitToScores;
private int scoreToCommit;
private int spcToCommit;
private String modeToCommit;
private Wait wait;
private final int WAIT_TIME = 1000; // milliseconds of wait time before game starts
private Intro intro; // this is displayed at first and then uninstantiated


private static int extraPerCard; // increment player's time limit by this amount each time he gets a match
private final int msPerSymbol = 3000; // allot this initial time for a player in survivor mode
private final int extraPerCardPerSymbol = 500;
private static final float extraTimeDecayOrder = .7f; // 1 is reciprocal decay as score increases, 2 is quadratic decay, etc
private static int timeLimit;
private static final int PENALTY = 1000; // 1000 ms = 1 second, time penalty for an incorrect card


private static APMediaPlayer mPlayer; // for sound playback, must be released when the sketch is destroyed
private static final String START = "sounds/start.ogg"; // relative paths to sound files passed to mPlayer
private static final String CORRECT = "sounds/correct.ogg";
private static final String WRONG = "sounds/wrong.ogg";


private final String LOG = this.getClass().getSimpleName();

/******************************************
 * Processing or java on my computer has a bug and requires the absolute path to load data,
 * when running on Android the path relative to the sketch folder must be used
 *******************************************/
//private static String path = "/Users/kylebebak/Desktop/Dropbox/Programming/Processing/XX__WebAndMobileApps/Projects/Match it/Tap_It/data/";
private static String path = new String();



public void setup() {

  
  orientation(LANDSCAPE);
  smooth();

  dx *= width; // deck and player positions
  dy *= height;
  px *= width;
  py *= height;
  /********** STRANGE BUGS OCCUR WHEN multiplying variables by width and height in setup, be careful **********/

  /***************************************
   * Import and parse symbol images
   ****************************************/
  allImgs = new PImage[nImages];
  for (int i = 0; i < nImages; i++)
    allImgs[i] = loadImage(path + "images/img" + Integer.toString(i) + ".png");

  font = loadFont(path + "Chalkboard-80.vlw");
  textFont(font); // this sets the text font for the entire application, it is never changed

  /***************************************
   * Initialize media player to play sounds
   ****************************************/
  mPlayer = new APMediaPlayer(this); // create new APMediaPlayer
  mPlayer.setLooping(false); // don't restart when end of playback has been reached
  mPlayer.setVolume(1.0f, 1.0f); // max left and right volumes, range is from 0.0 to 1.0


  /***************************************
   * Create directories and files for scores if they don't exist
   ****************************************/
  sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
  
  
  sdPath = sdPath + "/" + APPNAME;
  Log.v(LOG, "Detected sd path: [" +  sdPath + "]");


  /***************************************s
   * Initialize game screens, games goes intro on setup
   ****************************************/
  splash = new Splash(this);
  // command center for the game, menu screen

  Scores scoresInitializer = new Scores(this); 
  /* no-argument constructor creates directories and files if they're not currently on phone.
   sdPath MUST BE INITIALIZED before calling any of the scores constructors */

  scores = new Scores(this, 6, TIME_TRIAL);
  commitToScores = false;
  // initialize global scores to default game values, this has to be instantiated for addScore to be called

  intro = new Intro(this);
  // initialize intro screen
}





// called each time a new game starts, ensures a random sampling of the available images is used for each game
public void initializeGame() {

  shuffleArray(allImgs); // ensures that a random sampling of the available images are chosen for the deck

  /***************************************
   * Set game variables to values supplied by Sliders in Splash
   ****************************************/
  spc = splash.spc();
  mode = splash.mode();
  extraPerCard = round(extraPerCardPerSymbol * spc); // only matters for SURVIVOR mode
  timeLimit = round(msPerSymbol * spc);

  /***************************************
   * Import and parse deck info based on spc (symbols per card)
   ****************************************/
  deckInfo = loadStrings(path + "text/spc_" + Integer.toString(spc) + ".txt");
  nSyms = Integer.parseInt(deckInfo[0]);


  /***************************************
   * Instantiate cards and add them to cards ArrayList
   ****************************************/
  cards = new ArrayList<Card>();
  int[] cardIndices;
  String[] symbols;

  for (int c = 1; c <= nSyms; c++) {
    cardIndices = new int[spc];
    symbols = deckInfo[c].split(" ");

    for (int j = 0; j < spc; j++)
      cardIndices[j] = Integer.parseInt(symbols[j]);

    cards.add(new Card(this, spc, cardIndices, 0, 0));
    // it doesn't matter what the location of the added cards is
    // because addCard() changes the position of the card to the
    // current deck position
  }


  /***************************************
   * Instantiate deck, shuffle cards ArrayList, add cards to deck
   ****************************************/
  deck = new Deck(this, dx, dy);
  Collections.shuffle(cards);
  for (Card c : cards)
    deck.addCard(c);


  /***************************************
   * Instantiate players with a card each from the top of the deck
   ****************************************/
  p1 = new Player(px, py, deck.removeTop(), deck, timeLimit);


  /***************************************
   * Play start sound
   ****************************************/
  mPlayer.setMediaFile(START); // pass a String with relative path to start.ogg
  mPlayer.start(); // start playback
}


// go to wait screen before game is initialized
public void initializeWait() {
  wait = new Wait(this, WAIT_TIME);
}


// go to score screen specified by current slider values
public void initializeScores() {
  scores.initializeGameVariables(spc, mode);
}





public void draw() {

  /***************************************
   * Commit any pending scores to scores
   ****************************************/
  if (commitToScores) {
    scores.addScore(scoreToCommit, spcToCommit, modeToCommit);
    commitToScores = false;
  }

  /***************************************
   * What to draw and do if game HAS NOT YET STARTED
   ****************************************/
  if (intro != null) {
    intro.display();
    return;
  }

  /***************************************
   * What to draw and do if game state is splash screen
   ****************************************/
  if (!splash.inPlay() && !splash.inScores() && !splash.inWait()) {
    splash.displayAndUpdate();

    spc = splash.spc();
    mode = splash.mode();
  }


  /***************************************
   * What to draw and do if game is in wait screen
   ****************************************/
  if (splash.inWait()) {

    if (wait != null) {
      wait.displayAndUpdate();
      if (wait.timeIsUp()) {
        wait = null;
        initializeGame();
        splash.setWaitStatus(false);
        splash.setGameStatus(true);
      }
      return;
    }
  }

  /***************************************
   * What to draw and do if game is in play
   ****************************************/
  if (splash.inPlay()) {
    p1.displayAndUpdate(); // player is responsible for displaying everything in the game

      if (deck.getSize() == 0) {

      if (mode.equals(TIME_TRIAL)) {
        int timeElapsed = p1.timeElapsed();
        p1.fixAndDisplayTime(timeElapsed);
        commitToScores(timeElapsed, spc, mode);
        // ensure same time is committed to scores as is displayed on the screen
        restartSplash();
      }

      // if in survivor mode, deck must be recycled without reinstantiating player
      if (mode.equals(SURVIVOR)) {
        deck = new Deck(this, dx, dy);
        Collections.shuffle(cards);
        for (Card c : cards)
          if (c != p1.getCard()) // check reference equality, add all cards to deck except player's current card
              deck.addCard(c);

        p1.giveDeck(deck);
        mPlayer.setMediaFile(START); // play start sound in survivor mode every time player goes through deck
        mPlayer.start();
      }
    }


    if (p1.timeIsUp() && mode.equals(SURVIVOR)) {
      // time running out only matters in SURVIVOR mode
      commitToScores(p1.getScore(), spc, mode); // the number of cards the player got before time ran out 
      restartSplash();
    }
  }


  /***************************************
   * What to draw and do if scores state is active
   ****************************************/
  if (splash.inScores()) {
    scores.display();
  }
}



// helper function takes game back to splash screen so it can be restarted
public void restartSplash() {
  if (splash.inPlay()) {
    loadPixels(); // this must be called before get() because of a bug in android processing
    splash.setBackground(get()); // set the splash page background to be current game screen
    updatePixels();
  }

  splash.setWaitStatus(false);
  splash.setGameStatus(false);
  splash.setScoresStatus(false);
}




// implementing interaction, all of it is done with screen presses and drags
public void mousePressed() {

  /******** Don't register other mouse events until intro screen is finished *********/
  if (intro != null) {
    if (intro.checkClick())
      intro = null;
    return;
  }


  /******** Checking in game *********/
  if (splash.inPlay()) {
    if (p1.checkBack()) {
      if (mode.equals(SURVIVOR))
        commitToScores(p1.getScore(), spc, mode); // commit score on back click for survivor mode
      restartSplash();
      return;
    }
    p1.checkClick();
  }


  /******** Checking in scores *********/
  if (splash.inScores()) {
    int scoresClickedWhere = scores.checkClick();
    if (scoresClickedWhere == 0) {
      restartSplash();
      return;
    }

    if (scoresClickedWhere == 1) {
      scores.setErase(true);
      scores.displayEraseScores(); // this is called only once, not in draw loop
      return;
    }

    if (scoresClickedWhere == 2) {
      scores.setErase(false);
      return;
    }

    if (scoresClickedWhere == 3) {
      scores.eraseScores();
      scores.setErase(false);
      return;
    }
  }


  /******** Checking in splash *********/
  int splashClickedWhere = splash.checkClick(); // can only be called once per draw loop
  if (splashClickedWhere == 0) {
    initializeWait();
    return;
  }

  if (splashClickedWhere == 1) {
    initializeScores();
    return;
  }
}

public void mouseDragged() {
  if (splash.inPlay())
    p1.checkRotate();
}

public void mouseReleased() {
  if (splash.inPlay())
    p1.checkRelease();
}






// return a pointer to the image from the allImgs array specified by the input index
public static PImage imageAtIndex(int index) {
  return allImgs[index];
}


// return a pointer to the media player, so that other classes can call it
public static APMediaPlayer getPlayer() {
  return mPlayer;
}

/********************************************************************
 ********************************************************************
 ********************************************************************
 testing to see if this avoids a common crash issue i've been having,
 it simply makes sure that addScore is always called at the beginning
 of draw
 */
public void commitToScores(int score, int spc, String mode) {
  commitToScores = true; 
  scoreToCommit = score;
  spcToCommit = spc;
  modeToCommit = mode;
}


// implementing Fisher\u2013Yates shuffle
public void shuffleArray(PImage[] a)
{
  for (int i = a.length - 1; i >= 0; i--)
  {
    int index = (int) random(i + 1);
    // Simple swap
    PImage p = a[index];
    a[index] = a[i];
    a[i] = p;
  }
}



public class Player {

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
  public Player(float x, float y, Card c, Deck d, int timeLimit) {

    /********** Set dimensions of back arrow and score, relative to player card **********/
    this.x = x;
    this.y = y;
    r = TapIt.cardRadius;
    r *= width;

    back = loadImage(TapIt.path + "images/back.png");
    backDim *= width;
    backX = backX * r + x;
    backY *= backDim;
    scoreX = scoreX * r + x;
    scoreY = scoreY * r + y;

    textSize *= width;


    /********** Initialize timer, score, player card and deck **********/
    this.initialize();
    card = c;
    card.changePosition(x, y);
    deck = d;
    ft = new ArrayList<FadingText>();

    score = 0;
    this.timeLimit = timeLimit;
    timer = new Timer(TapIt.this, timeLimit);
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
    timer = new Timer(TapIt.this, timeLimit);
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
    background(BG);
    deck.displayDeck();
    deck.displayTopFront();

    // display back arrow
    imageMode(CENTER);
    image(back, backX, backY, backDim, backDim);

    // display player card
    card.updateTheta();
    card.displayFront();
    fill(textColor);
    textSize(textSize);
    text(score, scoreX, scoreY);

    if (TapIt.mode.equals(TapIt.SURVIVOR))
      text(timer.remainingTimeToStringMinutes(), scoreX, scoreY + textSize);

    if (TapIt.mode.equals(TapIt.TIME_TRIAL) && !displayFixed)
      text(timer.elapsedTimeToStringMinutes(), scoreX, scoreY + textSize);
    if (TapIt.mode.equals(TapIt.TIME_TRIAL) && displayFixed)
      text(timer.timeToStringMinutes(fixedTime), scoreX, scoreY + textSize);

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
    if (dist(mouseX, mouseY, x, y) > r) 
      locked = true; // lock out rotation

    // in survivor mode, extraPerCard decreases as player gets more points, slowly approaching zero
    float epcMultiplier = pow( (float) TapIt.nSyms, TapIt.extraTimeDecayOrder ) / 
    pow( (float) max(TapIt.nSyms, score), TapIt.extraTimeDecayOrder );

    int symbolIndex = deck.indexAtPosition(mouseX, mouseY);
    if (symbolIndex == -1) 
      return; // no symbol was clicked on


    /*********** CORRECT CLICK ************/
    if (card.hasSymbol(symbolIndex)) { 
      score++;

      if (TapIt.mode.equals(TapIt.SURVIVOR)) {
        timer.addTime(round(TapIt.extraPerCard * epcMultiplier));
        ft.add(new FadingText(TapIt.this, correctTextColor, "+" + nf(TapIt.extraPerCard * epcMultiplier / (float) 1000, 1, 1), 
        x + random(-r, r), y + random(-r, r)));
      }
      if (TapIt.mode.equals(TapIt.TIME_TRIAL))
        ft.add(new FadingText(TapIt.this, correctTextColor, "+1", x + random(-r, r), y + random(-r, r)));

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

      ft.add(new FadingText(TapIt.this, wrongTextColor, "-" + nf(TapIt.PENALTY / (float) 1000, 1, 1), 
      x + random(-r, r), y + random(-r, r)));

      TapIt.getPlayer().setMediaFile(TapIt.WRONG); // play wrong sound whenever player gets one wrong
      TapIt.getPlayer().start();
    }
  }



  // player is checking for a click within the back arrow
  public boolean checkBack() {
    // check if player clicked back button to return to splash screen, calls method form main Tap_It program
    return dist(mouseX, mouseY, backX, backY) < backDim / 2.0f;
  }

  // this is called in main class whenever mouse is dragged
  public void checkRotate() {
    if (locked) 
      return;
    float alpha = (mouseX - x) * (mouseY - pmouseY) - (mouseY - y) * (mouseX - pmouseX);
    card.incrementOmega(alpha * rotationSpeed);
  }

  public void checkRelease() {
    locked = false;
  }
}

public int sketchWidth() { return displayWidth; }
  public int sketchHeight() { return displayHeight; }  
  
}
