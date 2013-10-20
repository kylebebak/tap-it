package bebak.kyle.tap_it;

import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Represents "Displayable" object, which will be rendering intro sequence
 * shown before the start of the game.
 * 
 * This class has a timer which is started when the class is constructed. 
 * 
 * The class can display itself and check to see if it has been pressed 
 */
public class Intro extends Displayable 
{

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
  
  /**
   * Name of the app, displayed during the intro.
   */
  private final String mDisplayName;


  /**
   * Initializes Intro object with the given width and height of the rectangular area where it will be rendered into.
   * 
   * @param regionWidth
   * @param regionHeight
   * @param displayName name of the app (game) which will be displayed during the intro.
   */
  public Intro(int regionWidth, int regionHeight, String displayName) {
	mDisplayName = displayName;
	textSize *= regionHeight;
    titleX *= regionWidth;
    titleY *= regionHeight;

    /********* Compute numbers of images to be shown *********/
    float w = regionWidth * (1 - borderSize);
    float h = regionHeight * (1 - borderSize);

    int ratio = MatchIt.round(w / h);
    int nVertical = MatchIt.round(MatchIt.sqrt(nImgTarget / (float) ratio));
    int nHorizontal = nVertical * ratio;

    nImg = nVertical * nHorizontal;



    /********* Compute image dimensions and locations *********/
    x = new float[nImg];
    y = new float[nImg];
    for (int i = 0; i < nImg; i++)
      x[i] = w * (i % nHorizontal + 1) / (nHorizontal + 1) + (borderSize / 2.0f) * regionWidth;
    for (int j = 0; j < nImg; j++) 
      y[j] = h * (j / nHorizontal + 1) / (nVertical + 1) + (borderSize / 2.0f) * regionHeight;

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
      a[i] = Utils.random(0, 2 * MatchIt.PI);

    timer = new Timer(0);
    timer.start();
    titleFinished = false;
  }



  /**
   * Renders contents of the intro widget into given PGraphics.
   * 
   * @param pg Should be PGraphics previously "opened" with beginDraw().
   */
  public void display(PGraphics pg) {
	pg.pushStyle();
	
	pg.background(0);
	pg.imageMode(MatchIt.CENTER);


    /***** Check for whether title screen is finished *****/
    long currentTime = timer.elapsedTime();
    if (currentTime >= titleTime && !titleFinished) {
      timer = new Timer(0);
      timer.start();
      titleFinished = true;
    }


    /***** display all matched images *****/
    for (int i = 0; i < nImg; i++) {

      updateImg(i); // make images jiggle
      pg.pushMatrix();
      pg.translate(dx[i] * pg.width + x[i], dy[i] * pg.height + y[i]);
      pg.rotate(a[i]);

      // if title isn't finished yet, display images in background but don't highlight matches
      if (!titleFinished)
    	  pg.image(MatchIt.allImgs[imgIndices[i]], 0, 0, imgW, imgH);

      // highlight matches
      else 
      {
    	// TODO: DK: I am not sure, whether this conversion from  long to int will break anything or not.
        int currentMatch = (int) ((currentTime / matchTime) % nMatches);
        //long currentMatch = (currentTime / matchTime) % nMatches;
        int matchPositionA = matchPositions.get(2 * currentMatch); // indices of current matched pair of symbols
        int matchPositionB = matchPositions.get(2 * currentMatch + 1);

        float tintStrength = 255 * MatchIt.abs(MatchIt.sin(1.0f * MatchIt.PI * currentTime / (float) matchTime));

        if (matchPositionA == i || matchPositionB == i) {
        	pg.tint(255 - tintStrength, 255, 255 - tintStrength);
        	pg.image(MatchIt.imageAtIndex(imgIndices[i]), 0, 0, imgW * matchSizeMultiplier, imgH * matchSizeMultiplier);
        }
        else {
        	pg.noTint();
        	pg.image(MatchIt.imageAtIndex(imgIndices[i]), 0, 0, imgW, imgH);
        }
      }

      pg.popMatrix();
    }


    /***** display game title until it disappears *****/
    if (!titleFinished) {
    	pg.textAlign(PApplet.CENTER, PApplet.CENTER);
    	pg.textSize(textSize);

      float transparency = currentTime / (float) titleTime;

      pg.rectMode(MatchIt.CORNER);
      pg.fill(0, 255 * MatchIt.sqrt(MatchIt.sqrt(1 - transparency)));
      pg.rect(0, 0, pg.width, pg.height);

      pg.fill(255, 255, 0, 255 * MatchIt.sqrt(1 - transparency));
      pg.text(mDisplayName, titleX, titleY);
      pg.textAlign(PApplet.LEFT, PApplet.BASELINE);
    }
    pg.popStyle();
  }// display(PGraphics);



  // helper function for making images jiggle
  private void updateImg(int index) {
    dx[index] += Utils.random(-ddp, ddp);
    dx[index] = PApplet.constrain(dx[index], -dpMax, dpMax);

    dy[index] += Utils.random(-ddp, ddp);
    dy[index] = PApplet.constrain(dy[index], -dpMax, dpMax);
  }

}