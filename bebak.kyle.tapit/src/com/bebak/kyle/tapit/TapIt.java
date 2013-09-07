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

private static final String DISPLAY_NAME = "Tap  It";
private static final String APPNAME = "TapIt";
static String sdPath = null;


private static final int spcInitialIndex = 1;
static Integer[] spcNumbers = { 
  3, 4, 5, 6, 8, 9, 10
}; 

public static String SURVIVOR = "Survivor"; // static constants for modes of play
public static String TIME_TRIAL = "Time trial";
private static final int modeInitialIndex = 0;
private static String[] modes = {
  TIME_TRIAL, SURVIVOR
};


private static int spc; // default spc. symbols per card, 3 - 10 is allowed, with the exception of 7
private static String mode;

private static int nSyms; // number of cards in deck, also number of unique symbols in deck

private static PImage[] allImgs;
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
private static final float cardRadius = .22f; // * width, multiplication done in subclass constructors


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
  splash = new Splash();
  // command center for the game, menu screen

  Scores scoresInitializer = new Scores(this); 
  /* no-argument constructor creates directories and files if they're not currently on phone.
   sdPath MUST BE INITIALIZED before calling any of the scores constructors */

  scores = new Scores(this, 6, TIME_TRIAL);
  commitToScores = false;
  // initialize global scores to default game values, this has to be instantiated for addScore to be called

  intro = new Intro();
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

    cards.add(new Card(spc, cardIndices, 0, 0));
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



public class FadingText {
  
  /** A simple class that can instantiate fading text
   and keep track of its lifetime, like a toast in the android os **/

  private int age;
  private float x;
  private float y;
  private String text;
  private int textColor;

  private final int LIFE = 75;
  private float textSize = .09f; // * width 


  public FadingText(int textColor, String text, float x, float y) {
    textSize *= width;

    age = 0; 
    this.text = text;
    this.x = x;
    this.y = y;
    this.textColor = textColor;
  }

  public boolean isDead() {
    return (age >= LIFE);
  }

  public void displayAndUpdate() {
    textAlign(CENTER, CENTER); // center text for added or subtracted time toasts

    age++;
    fill(textColor, 255.0f * sq(sq(1 - age / (float) LIFE)));
    textSize(textSize);
    text(text, x, y);

    textAlign(LEFT, BASELINE); // return textAlign to normal
  }
}

public class Intro {
  /* This class has a timer which is started when the class is constructed. 
   The class can display itself and check to see if it has been pressed */


  private int nImgTarget = 30;
  private int nMatches = 4;
  private int nImg;

  private float borderSize = .075f; // * width, * height

  private float[] x;
  private float[] y;
  private float imgW;
  private float imgH;
  private float padding = .25f;

  private int[] imgIndices;
  private ArrayList<Integer> matchPositions;

  // variables for jiggling images
  private float[] dx;
  private float[] dy;
  private float[] a;

  // variables to control acceleration of jiggling and bound it
  private float ddp = .0003f;
  private float dpMax = .01f;

  private float textSize = .225f; // * height
  private float titleX = .5f; // * width
  private float titleY = .4f; // * height

  // variables to control highlighting matched symbols
  private Timer timer;
  private int titleTime = 1800;
  private int matchTime = 1500;
  private float matchSizeMultiplier = 1.5f;

  private boolean titleFinished;


  /******** no argument constructor, everything is implemented within the class ********/
  public Intro() {

    textSize *= height;
    titleX *= width;
    titleY *= height;

    /********* Compute numbers of images to be shown *********/
    float w = width * (1 - borderSize);
    float h = height * (1 - borderSize);

    int ratio = round(w / h);
    int nVertical = round(sqrt(nImgTarget / (float) ratio));
    int nHorizontal = nVertical * ratio;

    nImg = nVertical * nHorizontal;



    /********* Compute image dimensions and locations *********/
    x = new float[nImg];
    y = new float[nImg];
    for (int i = 0; i < nImg; i++)
      x[i] = w * (i % nHorizontal + 1) / (nHorizontal + 1) + (borderSize / 2.0f) * width;
    for (int j = 0; j < nImg; j++) 
      y[j] = h * (j / nHorizontal + 1) / (nVertical + 1) + (borderSize / 2.0f) * height;

    imgW = (w / ((float) nHorizontal + 1)) * (1 - padding);
    imgH = (h / ((float) nVertical + 1)) * (1 - padding);


    /********* Choose nImg - nMatches random imgIndices *********/
    ArrayList<Integer> indicesToShuffle = new ArrayList<Integer>();
    for (int i = 0; i < 100; i++)
      indicesToShuffle.add(i);
    Collections.shuffle(indicesToShuffle);

    imgIndices = new int[nImg];
    for (int i = 0; i < imgIndices.length - nMatches; i++)
      imgIndices[i] = indicesToShuffle.get(i);


    /********* Choose nMatches indices which have already been chosen and shuffle 
     them into imgIndices, also save indices of matches *********/
    ArrayList<Integer> matchIndices = new ArrayList<Integer>();
    for (int i = imgIndices.length - nMatches; i < imgIndices.length; i++) {
      imgIndices[i] = imgIndices[i - (imgIndices.length - nMatches)];
      matchIndices.add(imgIndices[i]);
    }

    // reuse indices ArrayList instantiated above to call Collections.shuffle()
    indicesToShuffle = new ArrayList<Integer>();
    for (int i = 0; i < imgIndices.length; i++)
      indicesToShuffle.add(imgIndices[i]);
    Collections.shuffle(indicesToShuffle);

    // put shuffled indices with matches back into imgIndices
    for (int i = 0; i < imgIndices.length; i++)
      imgIndices[i] = indicesToShuffle.get(i);


    /********* Find positions of matches, keep pair of positions for a given match adjacent *********/
    matchPositions = new ArrayList<Integer>();
    for (int j = 0; j < matchIndices.size(); j++) {
      int matchIndex = matchIndices.get(j);
      for (int i = 0; i < imgIndices.length; i++)
        if (imgIndices[i] == matchIndex)
          matchPositions.add(i);
    }


    /********* Initialize jiggling variables *********/
    dx = new float[nImg];
    dy = new float[nImg];
    a = new float[nImg];

    for (int i = 0; i < nImg; i++)
      a[i] = random(0, 2 * PI);

    timer = new Timer(TapIt.this, 0);
    timer.start();
    titleFinished = false;
  }




  public void display() {

    background(0);
    imageMode(CENTER);


    /***** Check for whether title screen is finished *****/
    int currentTime = timer.elapsedTime();
    if (currentTime >= titleTime && !titleFinished) {
      timer = new Timer(TapIt.this, 0);
      timer.start();
      titleFinished = true;
    }


    /***** display all matched images *****/
    for (int i = 0; i < nImg; i++) {

      updateImg(i); // make images jiggle
      pushMatrix();
      translate(dx[i] * width + x[i], dy[i] * height + y[i]);
      rotate(a[i]);

      // if title isn't finished yet, display images in background but don't highlight matches
      if (!titleFinished)
        image(allImgs[imgIndices[i]], 0, 0, imgW, imgH);

      // highlight matches
      else 
      {
        int currentMatch = (currentTime / matchTime) % nMatches;
        int matchPositionA = matchPositions.get(2 * currentMatch); // indices of current matched pair of symbols
        int matchPositionB = matchPositions.get(2 * currentMatch + 1);

        float tintStrength = 255 * abs(sin(1.0f * PI * currentTime / (float) matchTime));

        if (matchPositionA == i || matchPositionB == i) {
          tint(255 - tintStrength, 255, 255 - tintStrength);
          image(TapIt.imageAtIndex(imgIndices[i]), 0, 0, imgW * matchSizeMultiplier, imgH * matchSizeMultiplier);
        }
        else {
          noTint();
          image(TapIt.imageAtIndex(imgIndices[i]), 0, 0, imgW, imgH);
        }
      }

      popMatrix();
    }


    /***** display game title until it disappears *****/
    if (!titleFinished) {
      textAlign(CENTER, CENTER);
      textSize(textSize);

      float transparency = currentTime / (float) titleTime;

      rectMode(CORNER);
      fill(0, 255 * sqrt(sqrt(1 - transparency)));
      rect(0, 0, width, height);

      fill(255, 255, 0, 255 * sqrt(1 - transparency));
      text(TapIt.DISPLAY_NAME, titleX, titleY);
      textAlign(LEFT, BASELINE);
    }
  }



  // helper function for making images jiggle
  private void updateImg(int index) {
    dx[index] += random(-ddp, ddp);
    dx[index] = constrain(dx[index], -dpMax, dpMax);

    dy[index] += random(-ddp, ddp);
    dy[index] = constrain(dy[index], -dpMax, dpMax);
  }



  // return true if screen is pressed anywhere, this is simply to dereference Intro object
  public boolean checkClick() {
    noTint(); // remove tint so that it doesn't carry over into game when intro is dereferenced
    return true;
  }
}

public class Player {

  /*********************************
   The Player is reponsible for displaying everything in the game, while the main class
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
        ft.add(new FadingText(correctTextColor, "+" + nf(TapIt.extraPerCard * epcMultiplier / (float) 1000, 1, 1), 
        x + random(-r, r), y + random(-r, r)));
      }
      if (TapIt.mode.equals(TapIt.TIME_TRIAL))
        ft.add(new FadingText(correctTextColor, "+1", x + random(-r, r), y + random(-r, r)));

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

      ft.add(new FadingText(wrongTextColor, "-" + nf(TapIt.PENALTY / (float) 1000, 1, 1), 
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

public class Slider<Item> {
  
  /*
  The constructor for this DISCRETE slider accepts integer inputs for its rectangle specifications,
   and then a generic array of objects that are the permitted values for the slider. The slider always 
   returns one of these objects when getValue() is called, and it displays itself by calling
   Object.toString() on whichever object is currently chosen. The initial index and name of the slider
   are also specified in the constructor
   */
   
  int topcornerx, topcornery; // location of slider
  int dimx, dimy, dim; // width and height, slider horizontal if dim bigger
  float xbutton, ybutton; // value returned by slider, button position
  int ticks, tickval;
  Object[] values;

  float tickspacing, tickspacingclick; // computing these variables in setup makes slider run faster
  float minxpos, maxxpos, minypos, maxypos, buttonsize, buttonsizex, buttonsizey;
  int colorbox, coloractive, colorbutton, textcolor;
  boolean toggleMove, over, lockout; // lockout prevents 2 sliders from being activated at once
  String slidername;
  final float roundedFraction = .1f; // how rounded are button edges?
  final float buttonSizeFraction = .085f; // make active region of button larger or smaller than drawn button
  float TEXTSIZE = .025f; // * width
  


  /** Constructor **/
  Slider (int topcornerx, int topcornery, int dimx, int dimy, 
  Item[] values, int initialIndex, String slidername) {
    this.topcornerx = topcornerx; 
    this.topcornery = topcornery;
    this.dimx = dimx; 
    this.dimy = dimy;
    this.slidername = slidername;

    this.values = new Object[values.length];
    for (int i = 0; i < values.length; i++) 
      this.values[i] = values[i];
    ticks = values.length;
    tickval = constrain(initialIndex, 0, values.length - 1);

    dim = max(dimx, dimy);
    buttonsize = dim * buttonSizeFraction;

    if (dimx >= dimy) {
      buttonsizex = buttonsize;
      buttonsizey = dimy;
      minxpos = topcornerx - buttonsizex / 2.0f;
      maxxpos = topcornerx + dimx + buttonsizex / 2.0f;
      minypos = topcornery; 
      maxypos = topcornery + buttonsizey;
      xbutton = topcornerx + round( dim * tickval / (float) values.length);
      ybutton = topcornery + buttonsizey / 2.0f;
    } 
    else {
      buttonsizex = dimx;
      buttonsizey = buttonsize;
      minxpos = topcornerx; 
      maxxpos = topcornerx + buttonsizex;
      minypos = topcornery - buttonsizey / 2.0f; 
      maxypos = topcornery + dimy + buttonsizey / 2.0f;
      xbutton = topcornerx + buttonsizex / 2.0f;
      ybutton = topcornery + round( dim * tickval / (float) values.length);
    }

    colorbox = 0xff0093CB; 
    coloractive = 0xff00FFFD; 
    colorbutton = 0xffFFFFFF;
    textcolor = Utils.color(255);
    TEXTSIZE *= width;

    tickspacing = dim / (float) (ticks - 1);
    tickspacingclick = dim / (float) ticks;
    if (dimx >= dimy)
      xbutton = topcornerx + tickval * tickspacing;
    else
      ybutton = topcornery + tickval * tickspacing;
  }

  public void update() {
    if (mouseX > minxpos && mouseX < maxxpos && 
      mouseY > minypos && mouseY < maxypos) over=true;
    else over = false;

    if (over && mousePressed && !lockout) toggleMove = true;

    if (mousePressed && !over && toggleMove==false) lockout = true;

    if (mousePressed == false) {
      toggleMove = false;
      lockout = false;
    }

    if (toggleMove) {

      if (dimx >= dimy) {
        tickval = constrain(floor((mouseX - topcornerx) / tickspacingclick), 0, ticks - 1);
        xbutton = topcornerx + tickval * tickspacing;
      }
      else {
        tickval = constrain(floor((mouseY - topcornery) / tickspacingclick), 0, ticks - 1);
        ybutton = topcornery + tickval * tickspacing;
      }
    }
  }



  /** Display and update **/
  public void display() {

    stroke(0);
    rectMode(CORNER);

    if ((over || toggleMove) && !lockout) fill(coloractive);
    else fill(colorbox);

    if (dimx >= dimy) rect(topcornerx - 1.5f * buttonsizex, topcornery, dim + 3 * buttonsizex, buttonsizey, dim * roundedFraction);
    else rect(topcornerx, topcornery - 1.5f * buttonsizey, buttonsizex, dim + 3 * buttonsizey, dim * roundedFraction);

    rectMode(CENTER);
    fill(colorbutton, 185);
    rect(xbutton, ybutton, buttonsizex, buttonsizey);

    textAlign(LEFT);
    textSize(TEXTSIZE);
    fill(textcolor);

    String sliderText = new String();
    if (slidername.length() == 0) 
      sliderText = values[tickval].toString();
    else 
      sliderText = slidername + "  :  " + values[tickval].toString();
      
    if (dimx >= dimy)
      text(sliderText, minxpos, minypos - .01f * height);
    else 
      text(sliderText, minxpos, minypos - 1.5f * buttonsizey - .01f * height);
  }



  /***** Return current value of slider *****/
  public Item getValue() {
    return (Item) values[tickval];
  }

  public void changeTextColor(int c) {
    textcolor = c;
  }

  public void changeSliderColor(int cbox, int cactive, int cbutton) {
    colorbox = cbox; 
    coloractive = cactive; 
    colorbutton = cbutton;
  }
}
public class Splash {

  /************************************
   This class is manages the flow of the game. It knows which state the game is in, and
   in each call to draw() the main routine queries this class so it knows whether to display
   and update the Player (i.e. the game), the Scores, or Splash. It has getters and setters
   for booleans that flag which state the game is in. If inPlay and inScores must not both
   be true at the same time. If neither is true, then the main routine displays and updates Splash
   ************************************/

  private boolean inWait;
  private boolean inPlay;
  private boolean inScores;
  private Slider spc;
  private Slider mode;

  private PImage bgImg; // this background image
  private final int BG = Utils.color(0); // background color in absence of background image

  private float textSizeStart = .13f; // * height
  private float textSizeScores = .055f; // * height

  // coordinates for text on screen, coordinates for sliders ARE HARD-CODED BELOW IN CONSTRUCTOR
  private float startX = .48f; // * width
  private float startY = .22f; // * height
  private float scoresX = .85f;
  private float scoresY = .875f;
  private int opaqueness = 200; // out of 255


  private Integer[] symbolNumbers = TapIt.spcNumbers;
  private String[] modes = TapIt.modes;

  private int spcInt;
  private String modeString;


  // constrcutor, instantiates sliders and sets inScreen booleans to be false
  public Splash() {
    textSizeStart *= height;
    textSizeScores *= height;

    startX *= width;
    startY *= height;
    scoresX *= width;
    scoresY *= height;

    inWait = false;
    inPlay = false;
    inScores = false; 

    spc = new Slider(round(.25f * width), round(.5f * height), round(.25f * width), round(.11f * height), 
    symbolNumbers, TapIt.spcInitialIndex, "Images");

    mode = new Slider(round(.66f * width), round(.5f * height), round(.1f * height), round(.13f * height), 
    modes, TapIt.modeInitialIndex, "");
  } 


  // various getters and setters
  public boolean inWait() {
    return inWait;
  }

  public boolean inPlay() {
    return inPlay;
  }

  public boolean inScores() {
    return inScores;
  }


  public void setWaitStatus(boolean inWait) {
    this.inWait = inWait;
  }

  public void setGameStatus(boolean inPlay) {
    this.inPlay = inPlay;
  }

  public void setScoresStatus(boolean inScores) {
    this.inScores = inScores;
  }


  // set the background of the splash screen
  public void setBackground(PImage bgImg) {
    this.bgImg = bgImg;
  }

  // get states of the various sliders that control game variables
  public int spc() {
    return spcInt;
  }

  public String mode() {
    return modeString;
  }



  public void displayAndUpdate() {

    if (bgImg == null)
      background(BG);
    else
      background(bgImg);

    rectMode(CORNER);
    fill(0, opaqueness); // mostly background of previous game partially visible
    rect(0, 0, width, height);

    fill(255);

    textAlign(CENTER, CENTER);
    textSize(textSizeStart);
    text("Start", startX, startY);
    textSize(textSizeScores);
    text("Scores", scoresX, scoresY);
    textAlign(LEFT, BASELINE); // reset to default

    spc.update();
    mode.update();

    spc.display();
    mode.display();

    spcInt = (Integer) spc.getValue();
    modeString = (String) mode.getValue();
  }



  /* splash screen is checking for a click to initialize a new game (go to player screen)
   or go to scores screen */
  public int checkClick() {
    if (inPlay || inScores || inWait) // return unless game is actually in splash screen
      return -1;

    if (dist(mouseX, mouseY, startX, startY) < 1.25f * textSizeStart) {
      inWait = true;
      return 0;
    }

    if (dist(mouseX, mouseY, scoresX, scoresY) < 1.25f * textSizeScores) {
      inScores = true;
      return 1;
    }

    return -1;
  }
}

public int sketchWidth() { return displayWidth; }
  public int sketchHeight() { return displayHeight; }
  
  
  public class Card {

	  private int nSyms; // number of symbols per card, less than total nSyms for deck
	  private int[] symIndices; // index of each symbol
	  // there are SPC + (SPC - 1) ^ 2 distinct symbols and distinct cards in the deck

	  private float x; // center of card
	  private float y;
	  private float theta = 0; // rotation angle of card
	  private float omega = 0; // angular velocity of card

	  private float sr; // symbol radius
	  private float[] sx; // symbol position relative to the center of the card
	  private float[] sy;
	  private float[] sa;


	  private float r; // radius of circular card
	  private float borderWidth = .055f; // * radius
	  private final int front = Utils.color(210, 255, 210);
	  private final int border = Utils.color(0, 255, 255);
	  private final float radiusMultiplier = .9995f; 
	  // make sr smaller if symbols don't fit on card
	  private final float angleDrag = .12f;
	  private final float displaySizeMultiplier = .975f;



	  public Card(int nSyms, int[] symIndices, float x, float y) {
	    r = TapIt.cardRadius; // making cardRadius final and static and initializing r here prevents a weird bug
	    r *= width;
	    borderWidth *= r;

	    this.nSyms = nSyms;
	    this.symIndices = symIndices;

	    this.x = x;
	    this.y = y;
	    sr = 2.0f * r / sqrt(nSyms); // new symbol radius
	    sx = new float[nSyms];
	    sy = new float[nSyms];
	    sa = new float[nSyms];
	    // initialize positions for symbols
	    int c = 0;
	card:
	    while (c < nSyms) {
	      float rad = random(0, r - sr / 2.0f); 
	      // if the divisor of sr is greater than 1, some symbols will be outside card boundary
	      float ang = random(0, 2.0f * PI);
	      float newx = rad * cos(ang);
	      float newy = rad * sin(ang);

	      for (int i = 0; i < c; i++) {
	        if ( dist(sx[i], sy[i], newx, newy) < 2.0f * sr) {
	          sr *= radiusMultiplier; 
	          continue card;
	        }
	      } 
	      sx[c] = newx;
	      sy[c] = newy;
	      sa[c] = random(0, 2 * PI);
	      c++;
	    }
	  }




	  public void displayFront() {
	    imageMode(CENTER);
	    pushMatrix();
	    translate(x, y);
	    rotate(theta);

	    fill(border);
	    ellipse(0, 0, 2 * r, 2 * r);
	    fill(front);
	    noStroke();
	    ellipse(0, 0, 2 * (r - borderWidth), 2 * (r - borderWidth));
	    stroke(0);

	    for (int c = 0; c < nSyms; c++) {
	      pushMatrix();
	      translate(sx[c], sy[c]);
	      rotate(sa[c]);
	      image(TapIt.imageAtIndex(symIndices[c]), 0, 0, 
	      displaySizeMultiplier * 2 * sr, displaySizeMultiplier * 2 * sr);
	      popMatrix();
	    }

	    popMatrix();
	  }

	  public void displayBack() {
	    // not implemented
	  }




	  public boolean hasSymbol(int symbolIndex) {
	    for (int s : symIndices) 
	      if (s == symbolIndex) 
	        return true;
	    return false;
	  } 

	  // return the index of symbol at the given x and y coordinates 
	  // relative to the center of the card, or -1 if no symbol at these coordinates
	  public int indexAtPosition(float x, float y) {
	    for (int i = 0; i < nSyms; i++)
	      if ( dist(x, y, sx[i], sy[i]) < sr ) return symIndices[i];
	    return -1;
	  }

	  // change the location of the card on the canvas
	  public void changePosition(float x, float y) {
	    this.x = x;
	    this.y = y;
	  }

	  // reset the rotation angle to zero, this is called by deck on any card
	  // added to the deck. this ensures that player enduced rotations of a
	  // a card can't affect collision detection when the card goes back in the deck
	  public void resetAngles() {
	    theta = 0;
	    omega = 0;
	  }

	  // update omega and the rotation angle theta of the card
	  public void updateTheta() {
	    omega *= (1 - angleDrag);
	    theta += omega;
	  }

	  public void incrementOmega(float alpha) {
	    omega += alpha;
	  }

	  // return the indices of the symbols on this card
	  public int[] symbols() {
	    return symIndices.clone(); // clone returns shallows copies EXCEPT for primitives
	  }
	}  
  
}
