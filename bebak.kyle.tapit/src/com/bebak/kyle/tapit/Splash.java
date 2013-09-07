package com.bebak.kyle.tapit;

import processing.core.PImage;

import com.bebak.kyle.tapit.TapIt.Slider;

public class Splash {

  /**
	 * 
	 */
	private final TapIt sketch;
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
  public Splash(TapIt tapIt) {
    sketch = tapIt;
	textSizeStart *= sketch.height;
    textSizeScores *= sketch.height;

    startX *= sketch.width;
    startY *= sketch.height;
    scoresX *= sketch.width;
    scoresY *= sketch.height;

    inWait = false;
    inPlay = false;
    inScores = false; 

    spc = sketch.new Slider(TapIt.round(.25f * sketch.width), TapIt.round(.5f * sketch.height), TapIt.round(.25f * sketch.width), TapIt.round(.11f * sketch.height), 
    symbolNumbers, TapIt.spcInitialIndex, "Images");

    mode = sketch.new Slider(TapIt.round(.66f * sketch.width), TapIt.round(.5f * sketch.height), TapIt.round(.1f * sketch.height), TapIt.round(.13f * sketch.height), 
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
      sketch.background(BG);
    else
      sketch.background(bgImg);

    sketch.rectMode(TapIt.CORNER);
    sketch.fill(0, opaqueness); // mostly background of previous game partially visible
    sketch.rect(0, 0, sketch.width, sketch.height);

    sketch.fill(255);

    sketch.textAlign(TapIt.CENTER, TapIt.CENTER);
    sketch.textSize(textSizeStart);
    sketch.text("Start", startX, startY);
    sketch.textSize(textSizeScores);
    sketch.text("Scores", scoresX, scoresY);
    sketch.textAlign(TapIt.LEFT, TapIt.BASELINE); // reset to default

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

    if (TapIt.dist(sketch.mouseX, sketch.mouseY, startX, startY) < 1.25f * textSizeStart) {
      inWait = true;
      return 0;
    }

    if (TapIt.dist(sketch.mouseX, sketch.mouseY, scoresX, scoresY) < 1.25f * textSizeScores) {
      inScores = true;
      return 1;
    }

    return -1;
  }
}