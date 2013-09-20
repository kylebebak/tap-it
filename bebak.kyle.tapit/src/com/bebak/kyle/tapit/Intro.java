package com.bebak.kyle.tapit;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class has a timer which is started when the class is constructed. 
 * 
 * The class can display itself and check to see if it has been pressed 
 */
public class Intro {

	private final TapIt sketch;
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
   * No argument constructor, everything is implemented within the class.
   * 
   * @param tapIt reference to the parent sketch.
   */ 
  // TODO: improve comment for constructor
  public Intro(TapIt tapIt) {

    sketch = tapIt;
	textSize *= sketch.height;
    titleX *= sketch.width;
    titleY *= sketch.height;

    /********* Compute numbers of images to be shown *********/
    float w = sketch.width * (1 - borderSize);
    float h = sketch.height * (1 - borderSize);

    int ratio = TapIt.round(w / h);
    int nVertical = TapIt.round(TapIt.sqrt(nImgTarget / (float) ratio));
    int nHorizontal = nVertical * ratio;

    nImg = nVertical * nHorizontal;



    /********* Compute image dimensions and locations *********/
    x = new float[nImg];
    y = new float[nImg];
    for (int i = 0; i < nImg; i++)
      x[i] = w * (i % nHorizontal + 1) / (nHorizontal + 1) + (borderSize / 2.0f) * sketch.width;
    for (int j = 0; j < nImg; j++) 
      y[j] = h * (j / nHorizontal + 1) / (nVertical + 1) + (borderSize / 2.0f) * sketch.height;

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
      a[i] = sketch.random(0, 2 * TapIt.PI);

    timer = new Timer(sketch, 0);
    timer.start();
    titleFinished = false;
  }




  public void display() {

    sketch.background(0);
    sketch.imageMode(TapIt.CENTER);


    /***** Check for whether title screen is finished *****/
    int currentTime = timer.elapsedTime();
    if (currentTime >= titleTime && !titleFinished) {
      timer = new Timer(sketch, 0);
      timer.start();
      titleFinished = true;
    }


    /***** display all matched images *****/
    for (int i = 0; i < nImg; i++) {

      updateImg(i); // make images jiggle
      sketch.pushMatrix();
      sketch.translate(dx[i] * sketch.width + x[i], dy[i] * sketch.height + y[i]);
      sketch.rotate(a[i]);

      // if title isn't finished yet, display images in background but don't highlight matches
      if (!titleFinished)
        sketch.image(TapIt.allImgs[imgIndices[i]], 0, 0, imgW, imgH);

      // highlight matches
      else 
      {
        int currentMatch = (currentTime / matchTime) % nMatches;
        int matchPositionA = matchPositions.get(2 * currentMatch); // indices of current matched pair of symbols
        int matchPositionB = matchPositions.get(2 * currentMatch + 1);

        float tintStrength = 255 * TapIt.abs(TapIt.sin(1.0f * TapIt.PI * currentTime / (float) matchTime));

        if (matchPositionA == i || matchPositionB == i) {
          sketch.tint(255 - tintStrength, 255, 255 - tintStrength);
          sketch.image(TapIt.imageAtIndex(imgIndices[i]), 0, 0, imgW * matchSizeMultiplier, imgH * matchSizeMultiplier);
        }
        else {
          sketch.noTint();
          sketch.image(TapIt.imageAtIndex(imgIndices[i]), 0, 0, imgW, imgH);
        }
      }

      sketch.popMatrix();
    }


    /***** display game title until it disappears *****/
    if (!titleFinished) {
      sketch.textAlign(TapIt.CENTER, TapIt.CENTER);
      sketch.textSize(textSize);

      float transparency = currentTime / (float) titleTime;

      sketch.rectMode(TapIt.CORNER);
      sketch.fill(0, 255 * TapIt.sqrt(TapIt.sqrt(1 - transparency)));
      sketch.rect(0, 0, sketch.width, sketch.height);

      sketch.fill(255, 255, 0, 255 * TapIt.sqrt(1 - transparency));
      sketch.text(TapIt.DISPLAY_NAME, titleX, titleY);
      sketch.textAlign(TapIt.LEFT, TapIt.BASELINE);
    }
  }



  // helper function for making images jiggle
  private void updateImg(int index) {
    dx[index] += sketch.random(-ddp, ddp);
    dx[index] = TapIt.constrain(dx[index], -dpMax, dpMax);

    dy[index] += sketch.random(-ddp, ddp);
    dy[index] = TapIt.constrain(dy[index], -dpMax, dpMax);
  }



  // return true if screen is pressed anywhere, this is simply to dereference Intro object
  public boolean checkClick() {
    sketch.noTint(); // remove tint so that it doesn't carry over into game when intro is dereferenced
    return true;
  }
}