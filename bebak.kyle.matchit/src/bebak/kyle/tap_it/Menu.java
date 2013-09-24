package bebak.kyle.tap_it;

import processing.core.PImage;


public class Menu {

  /**
	 * 
	 */
	private final MatchIt sketch;
/************************************
   This class is manages the flow of the game. It knows which state the game is in, and
   in each call to draw() the main routine queries this class so it knows whether to display
   and update the Player (i.e. the game), the Scores, or Menu. It has getters and setters
   for booleans that flag which state the game is in. If inPlay and inScores must not both
   be true at the same time. If neither is true, then the main routine displays and updates Menu
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


  private Integer[] symbolNumbers = MatchIt.spcNumbers;
  private String[] modes = MatchIt.modes;

  private int spcInt;
  private String modeString;


  // constrcutor, instantiates sliders and sets inScreen booleans to be false
  public Menu(MatchIt matchIt) {
    sketch = matchIt;
	textSizeStart *= sketch.height;
    textSizeScores *= sketch.height;

    startX *= sketch.width;
    startY *= sketch.height;
    scoresX *= sketch.width;
    scoresY *= sketch.height;

    inWait = false;
    inPlay = false;
    inScores = false; 

    spc = new Slider(sketch, MatchIt.round(.25f * sketch.width), MatchIt.round(.5f * sketch.height), MatchIt.round(.25f * sketch.width), MatchIt.round(.11f * sketch.height), 
    symbolNumbers, MatchIt.spcInitialIndex, "Images");

    mode = new Slider(sketch, MatchIt.round(.66f * sketch.width), MatchIt.round(.5f * sketch.height), MatchIt.round(.1f * sketch.height), MatchIt.round(.13f * sketch.height), 
    modes, MatchIt.modeInitialIndex, "");
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


  // set the background of the menu screen
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
      sketch.background(BG);
    else
      sketch.background(bgImg);

    sketch.rectMode(MatchIt.CORNER);
    sketch.fill(0, opaqueness); // mostly background of previous game partially visible
    sketch.rect(0, 0, sketch.width, sketch.height);

    sketch.fill(255);

    sketch.textAlign(MatchIt.CENTER, MatchIt.CENTER);
    sketch.textSize(textSizeStart);
    sketch.text("Start", startX, startY);
    sketch.textSize(textSizeScores);
    sketch.text("Scores", scoresX, scoresY);
    sketch.textAlign(MatchIt.LEFT, MatchIt.BASELINE); // reset to default

    spc.update();
    mode.update();

    spc.display();
    mode.display();

    spcInt = (Integer) spc.getValue();
    modeString = (String) mode.getValue();
  }



  /* menu screen is checking for a click to initialize a new game (go to player screen)
   or go to scores screen */
  public int checkClick() {
    if (inPlay || inScores || inWait) // return unless game is actually in menu screen
      return -1;

    if (MatchIt.dist(sketch.mouseX, sketch.mouseY, startX, startY) < 1.25f * textSizeStart) {
      inWait = true;
      return 0;
    }

    if (MatchIt.dist(sketch.mouseX, sketch.mouseY, scoresX, scoresY) < 1.25f * textSizeScores) {
      inScores = true;
      return 1;
    }

    return -1;
  }
}