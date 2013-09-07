package com.bebak.kyle.tapit;

import java.io.File;
import java.util.HashMap;

import android.util.Log;

public class Scores {

  /**
	 * 
	 */
	private final TapIt sketch;
private String path;
  private String[] scores;
  private final String BLANK = "--   --"; // this means the high score here is blank
  private final int NSCORES = 20; 
  // this is the number of high scores that can be stored for a given mode, must be even

  private final int BG = Utils.color(0);
  private final int TEXTCOLOR = Utils.color(255);
  private String mode;
  private int spc;

  private float textSize;
  private float column1x = .325f; // * width
  private float column2x = .675f;

  private boolean inErase; // is scores screen in the erase confirmation screen?
  private float eraseTextSize = .065f; // * height
  private float eraseX = .1f; // * width
  private float eraseY = .875f; // * height
  private float confirmTextSize = .1f; // * height
  private float noX = .625f; // * width
  private float noY = .55f; // * height
  private float yesX = .375f; // * width
  private float yesY = .55f; // * height
  private int eraseScoresOpaqueness = 220;

  private Timer timer; 
  /* only instantiated to call methods which SHOULD BE STATIC, but processing
   treats all classes as inner classes wrapped in the main PApplet class, 
   and inner classes can't have static methods */

  // this keeps track of the index of most recently committed score
  private int mrcColor = Utils.color(255, 0, 0);
  // display most recently committed score for a given difficulty in different color
  private HashMap<String, Integer> mrcIndices;

  
  /**
   * Just constant holding the name of current class for logging purposes.
   * 
   * For calling Log.v(LOG, "My log message created by curent class");
   */
  private final String LOG = this.getClass().getSimpleName();  


  /*********** CONSTRUCTOR ACCEPTS GAME PARAMETERS ************/
  public Scores(TapIt tapIt, int spc, String mode) {
    sketch = tapIt;
	initializeGameVariables(spc, mode); // this must be called so that scores.length is defined!

    mrcIndices = new HashMap<String, Integer>();
    for (Integer i : TapIt.spcNumbers) {
      mrcIndices.put(TapIt.sdPath + "/Survivor" + "/" + Integer.toString(i) + ".txt", -1);
      mrcIndices.put(TapIt.sdPath + "/TimeTrial" + "/" + Integer.toString(i) + ".txt", -1);
    }

    inErase = false;
    textSize = sketch.height / (scores.length / 2 + 2);
    eraseTextSize *= sketch.height;
    eraseX *= sketch.width;
    eraseY *= sketch.height;
    confirmTextSize *= sketch.height;
    noX *= sketch.width;
    noY *= sketch.height;
    yesX *= sketch.width;
    yesY *= sketch.height;
    
    column1x *= sketch.width;
    column2x *= sketch.width;

    timer = new Timer(sketch, 0);
  }


  /*********** THIS BLANK CONSTRUCTOR TO BE USED ONLY AS AN INITIALIZER 
 * @param tapIt TODO************/
  public Scores(TapIt tapIt) {   

    sketch = tapIt;
	path = TapIt.sdPath;
    String[] newScores = new String[NSCORES];
    for (int i = 0; i < newScores.length; i++)
      newScores[i] = BLANK;

    createDirectory(path + "/Survivor");
    createDirectory(path + "/TimeTrial");
    for (Integer i : TapIt.spcNumbers) {
      createFile(path + "/Survivor" + "/" + Integer.toString(i) + ".txt", newScores);
      createFile(path + "/TimeTrial" + "/" + Integer.toString(i) + ".txt", newScores);
    }
  }



  /*********** set the path based on the input game parameters ************/
  private void buildPath(int spc, String mode) {
    path = new String();
    if (mode.equals(TapIt.SURVIVOR))
      path = TapIt.sdPath + "/Survivor" + "/" + spc + ".txt";
    if (mode.equals(TapIt.TIME_TRIAL))
      path = TapIt.sdPath + "/TimeTrial" + "/" + spc + ".txt";
  }

  /*********** initialize or change game varibles that Scores displays ************/
  public void initializeGameVariables(int spc, String mode) {
    this.mode = mode;
    this.spc = spc;
    buildPath(spc, mode);
    if ( ! (new File(path).exists()) ){
    	scores = new String[] { "hello 100", "wowow 200" };
    	Log.v(LOG, "file: [" + path +"] doesn't exist, so i simulate scores");
    }
    else{
    	// on my HTC device (where I didn't have sd-card mounted, the line below would crash
    	// the app.
    	scores = sketch.loadStrings(path);
    }
  }





  // display scores
  public void display() {
    // what to display in scores screen
    if (!inErase) {

      sketch.background(BG);
      sketch.fill(TEXTCOLOR);
      sketch.textAlign(TapIt.CENTER, TapIt.CENTER);
      sketch.textSize(textSize);

      // display score screen title
      sketch.text(spc + " : " + mode, sketch.width / 2, textSize);

      /* all scores, including times, are stored as integers in the score text files, 
       so display converts them to a human readable string 
       (for example, from an integer number of milliseconds to a string of minutes and seconds)
       before displaying them, depending on the mode */

      // 1st column of scores
      for (int i = 0; i < scores.length / 2; i++)
        sketch.text(scoreToDisplay(i), column1x, (i + 2) * textSize);

      // 2nd column of scores
      for (int i = scores.length / 2; i < scores.length; i++) 
        sketch.text(scoreToDisplay(i), column2x, (i + 2 - scores.length / 2) * textSize);

      // option to erase scores for this screen
      sketch.textSize(eraseTextSize);
      sketch.fill(mrcColor);
      sketch.text("Erase", eraseX, eraseY);
    }

    sketch.textAlign(TapIt.LEFT, TapIt.BASELINE); // return text alignment to default setting
  }

  // just called once to lay on top of scores screen so scores are dimly visible in background
  public void displayEraseScores() {

    sketch.rectMode(TapIt.CORNER);
    sketch.fill(0, eraseScoresOpaqueness);
    sketch.rect(0, 0, sketch.width, sketch.height);

    sketch.textAlign(TapIt.CENTER, TapIt.CENTER);
    sketch.textSize(confirmTextSize);

    // display confirmation question
    sketch.fill(mrcColor);
    sketch.text("Erase all scores?", .5f * sketch.width, noY - 2.0f * confirmTextSize);
    sketch.fill(TEXTCOLOR);
    sketch.text("Yes", yesX, yesY);
    sketch.text("No", noX, noY);

    sketch.textAlign(TapIt.LEFT, TapIt.BASELINE); // return text alignment to default setting
  }



  // helper function that formats (colors) and returns score string to be displayed
  private String scoreToDisplay(int index) {
    String score = scores[index];

    int mrcIndex = (int) mrcIndices.get(path);
    if (index == mrcIndex)
      sketch.fill(mrcColor);
    else
      sketch.fill(TEXTCOLOR);


    if (score.equals(BLANK))
      return score; 

    if (mode.equals(TapIt.SURVIVOR))
      return score;
    // text() accepts integer input, automatically parses it to a string

    if (mode.equals(TapIt.TIME_TRIAL))
      return timer.timeToStringMinutes(Integer.parseInt(score));

    return new String(); // just in case
  }





  /* this method can be used to add a new score: 
   either an integer number of points for survivor mode, 
   or an integer number of milliseconds elapsed for time trial */
  public void addScore(int score, int spc, String mode) {
    buildPath(spc, mode);
    scores = sketch.loadStrings(path);

    int mrcIndex = -1;
    for (int i = 0; i < scores.length; i++) {

      if (scores[i].equals(BLANK)) {
        // check this first, to make sure parseInt isn't called on an unparsable input
        mrcIndex = i;  
        break;
      }

      if (mode.equals(TapIt.SURVIVOR) && score > Integer.parseInt(scores[i])) {
        // score in text file must be converted to an integer to allow comparison
        mrcIndex = i;
        break;
      }

      if (mode.equals(TapIt.TIME_TRIAL) && score < Integer.parseInt(scores[i])) {
        // in time trial smaller score (time) is better, in survivor bigger score (cards) is better
        mrcIndex = i;
        break;
      }
    }

    mrcIndices.put(path, mrcIndex);

    if (mrcIndex == -1)
      return; // new score wasn't higher than any of the scores on the high score list


    // if score was high enough, move all scores down one notch until you get to appropriate position
    for (int i = scores.length - 1; i > mrcIndex; i--) 
      scores[i] = scores[i - 1];

    // insert new high score into appropriate position
    scores[mrcIndex] = Integer.toString(score);


    /* write new scores to the text file after they've been updated.
     a fatal problem could occur if the program crashed while these strings
     were being written to the text file. to make this bulletproof i need a function
     in this class that checks all the high scores files in setup in the main 
     program and makes sure they exist and are properly formatted */
    sketch.saveStrings(path, scores);
  } 



  // allow main class to move scores screen between erase confirmation scores screen
  public void setErase(boolean inErase) {
    this.inErase = inErase;
  }

  /* if !inErase, return 0 for back click, 1 for erase. if inErase,
   return 2 for no click, 3 for yes
   */
  public int checkClick() {
    if (!inErase) {
      if (TapIt.dist(sketch.mouseX, sketch.mouseY, eraseX, eraseY) < eraseTextSize * 1.25f)
        return 1;
      return 0;
    } 
    else {
      if (TapIt.dist(sketch.mouseX, sketch.mouseY, noX, noY) < eraseTextSize * 1.25f)
        return 2;
      if (TapIt.dist(sketch.mouseX, sketch.mouseY, yesX, yesY) < eraseTextSize * 1.25f)
        return 3;
    }
    return -1; // nothing was clicked
  }

  public void eraseScores() {
    buildPath(spc, mode);

    String[] newScores = new String[NSCORES];
    for (int i = 0; i < newScores.length; i++)
      newScores[i] = BLANK;

    overwriteFile(path, newScores);
    scores = sketch.loadStrings(path);
    mrcIndices.put(path, -1); // most recently committed score for this mode no longer exists
  }






  /* for building directories and files in which data is stored, calling this function will only do
   something the first time the app is run, or if for some reason the data directory is deleted
   from the sd card of the user's phone. the following line is in the manifest file:
   
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
   
   this gives the app permission to write to the sd card. i would rather write to the data folder,
   i.e. internal storage inside the app which is not visible to the rest of the apps on the phone,
   but processing has no way of doing this. you can read from the data directory but you can't
   write to it...
   */
  public void createDirectory(String directory) {
    try {
      File file = new File(directory);
      if (!file.exists()) {
        file.mkdirs();
        TapIt.println("directory created : " + directory);
      }
    }
    catch(Exception e) { 
      e.printStackTrace();
    }
  }

  public void createFile(String fileName, String[] lines) {
    try {
      File file = new File(fileName);
      if (!file.exists()) {
        sketch.saveStrings(fileName, lines);
        TapIt.println("file created : " + fileName);
      }
    }
    catch(Exception e) { 
      e.printStackTrace();
    }
  }

  public void overwriteFile(String fileName, String[] lines) {
    try {
      sketch.saveStrings(fileName, lines);
      TapIt.println("file overwritten : " + fileName);
    }
    catch(Exception e) { 
      e.printStackTrace();
    }
  }
}