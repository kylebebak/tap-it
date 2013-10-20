package bebak.kyle.tap_it;

import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

import android.os.Environment;

import android.util.Log;

import apwidgets.APMediaPlayer;

public class MatchIt extends PApplet {



/****************************************/

static final String DISPLAY_NAME = "Match  It"; // displayed in intro screen
private static final String APPNAME = "MatchIt"; 
// name of high scores directory on phone's sd card 
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
static String mode;

static int nSyms; // number of cards in deck, also number of unique symbols in deck

static PImage[] allImgs;
private static String[] deckInfo;
private static final int nImages = 100;


private Deck deck;
private ArrayList<Card> cards;
/**
 * deck position, * width
 * Deck x coordinate on screen. However
 * when declare variable contains offset of
 * the left edge of the screen in percents.
 * 
 */
private float dx = .71f; 
private float dy = .48f; // * height

private Player p1;

/**
 *  player X position, * width
 */
private float px = .235f;

/**
 * player Y position.
 */
private float py = .48f; // * height


private PFont font; // THIS IS THE ONLY FONT IN THE ENTIRE APPLICATION, IT DOESN'T CHANGE
static final float cardRadius = .22f; // * width, multiplication done in subclass constructors


private Menu menu; // this class manages the flow of the game, read more in its documentation
private Scores scores;
private boolean commitToScores;
private int scoreToCommit;
private int spcToCommit;
private String modeToCommit;
private Wait wait;
private final int WAIT_TIME = 1000; // milliseconds of wait time before game starts

/**
 * This is reference to the "Displayable" Intro class. This class represents intro 
 * drawn on screen, before playable game screen is loaded.
 * <p>
 * This variable also has second duty where it acts as a flag:
 * <li>when NULL this it is NOT displayed in the main game loop.
 * <li>when NOT NULL, it also means that intro MUST be displayed inside of main game loop.
 * </p>
 */
private Intro intro; // this is displayed at first and then uninstantiated


static int extraPerCard; // increment player's time limit by this amount each time he gets a match
private final int msPerSymbol = 3000; // allot this initial time for a player in survivor mode
private final int extraPerCardPerSymbol = 300;
static final float extraTimeDecayOrder = .5f; // 1 is reciprocal decay as score increases, 2 is quadratic decay, etc
private static int timeLimit;
static final int PENALTY = 750; // 1000 ms = 1 second, time penalty for an incorrect card


private static APMediaPlayer mPlayer; // for sound playback, must be released when the sketch is destroyed
private static final String START = "sounds/start.ogg"; // relative paths to sound files passed to mPlayer
static final String CORRECT = "sounds/correct.ogg";
static final String WRONG = "sounds/wrong.ogg";


private final String LOG = this.getClass().getSimpleName(); // "MatchIt"

/******************************************
 * Processing or java on my computer has a bug and requires the absolute path to load data,
 * when running on Android the path relative to the sketch folder must be used
 *******************************************/
static String path = new String();



public void setup() {

  
  orientation(LANDSCAPE);
  smooth();
  // dx = 0.71f;
  // dx = 0.71 * 800px = 500px
  dx *= width; // deck and player positions
  dy *= height;
  px *= width;
  py *= height;
  /********** STRANGE BUGS OCCUR WHEN multiplying variables by width and height in setup, be careful **********/

  /***************************************
   * Import and parse symbol images
   ****************************************/
  allImgs = new PImage[nImages];
  for (int i = 0; i < nImages; i++){
    allImgs[i] = loadImage(path + "images/img" + Integer.toString(i) + ".png");
  }



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
  //Log.v("MatchIt.java", "Warning, this is a warning");
  //Log.e("MatchIt.java", "Didn't detect sd path");


  /***************************************s
   * Initialize game screens, games goes intro on setup
   ****************************************/
  menu = new Menu(this);
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
   * Set game variables to values supplied by Sliders in Menu
   ****************************************/
  spc = menu.spc();
  mode = menu.mode();
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
  deck = new Deck( dx, dy, width);
  Collections.shuffle(cards);
  for (Card c : cards)
    deck.addCard(c);


  /***************************************
   * Instantiate players with a card each from the top of the deck
   ****************************************/
  p1 = new Player(this, px, py, deck.removeTop(), deck, timeLimit);


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
   * What to draw and do if game state is menu screen
   ****************************************/
  if (!menu.inPlay() && !menu.inScores() && !menu.inWait()) {
    menu.displayAndUpdate();

    spc = menu.spc();
    mode = menu.mode();
  }


  /***************************************
   * What to draw and do if game is in wait screen
   ****************************************/
  if (menu.inWait()) {

    if (wait != null) {
      wait.displayAndUpdate();
      if (wait.timeIsUp()) {
        wait = null;
        initializeGame();
        menu.setWaitStatus(false);
        menu.setGameStatus(true);
      }
      return;
    }
  }

  /***************************************
   * What to draw and do if game is in play
   ****************************************/
  if (menu.inPlay()) {
    p1.displayAndUpdate(); // player is responsible for displaying everything in the game

      if (deck.getSize() == 0) {

      if (mode.equals(TIME_TRIAL)) {
        int timeElapsed = p1.timeElapsed();
        p1.fixAndDisplayTime(timeElapsed);
        commitToScores(timeElapsed, spc, mode);
        // ensure same time is committed to scores as is displayed on the screen
        restartMenu();
      }

      // if in survivor mode, deck must be recycled without reinstantiating player
      if (mode.equals(SURVIVOR)) {
        deck = new Deck( dx, dy, width);
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
      restartMenu();
    }
  }


  /***************************************
   * What to draw and do if scores state is active
   ****************************************/
  if (menu.inScores()) {
    scores.display();
  }
}



// helper function takes game back to menu screen so it can be restarted
public void restartMenu() {
  if (menu.inPlay()) {
    loadPixels(); // this must be called before get() because of a bug in android processing
    menu.setBackground(get()); // set the menu page background to be current game screen
    updatePixels();
  }

  menu.setWaitStatus(false);
  menu.setGameStatus(false);
  menu.setScoresStatus(false);
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
  if (menu.inPlay()) {
    if (p1.checkBack()) {
      if (mode.equals(SURVIVOR))
        commitToScores(p1.getScore(), spc, mode); // commit score on back click for survivor mode
      restartMenu();
      return;
    }
    p1.checkClick();
  }


  /******** Checking in scores *********/
  if (menu.inScores()) {
    int scoresClickedWhere = scores.checkClick();
    if (scoresClickedWhere == 0) {
      restartMenu();
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


  /******** Checking in menu *********/
  int menuClickedWhere = menu.checkClick(); // can only be called once per draw loop
  if (menuClickedWhere == 0) {
    initializeWait();
    return;
  }

  if (menuClickedWhere == 1) {
    initializeScores();
    return;
  }
}

public void mouseDragged() {
  if (menu.inPlay())
    p1.checkRotate();
}

public void mouseReleased() {
  if (menu.inPlay())
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



public int sketchWidth() { return displayWidth; }
  public int sketchHeight() { return displayHeight; }  
  
}
