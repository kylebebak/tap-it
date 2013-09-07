package com.bebak.kyle.tapit;


public class Wait {
  /*
This class has a time limit which is passed to the constructor, and a timer 
   which is started when the class is constructed. An instance of a class can
   be queried to see if its allotted time is up. The class can also display and update
   itself to show how much of its allotted time remains */

  /**
	 * 
	 */
	private final TapIt tapIt;
private int timeLimit;
  private Timer timer;

  private float barWidth = .55f; // * width
  private float barHeight = .075f; // * height
  private float barX = .5f;
  private float barY = .5f;
  private float roundedFraction = .5f;
  private float textSize = .08f; // * height

  private final int TEXTCOLOR = Utils.color(255);
  private int barColor = Utils.color(255, 255, 0);
  private int BG = Utils.color(0);

  /******** CONSTRUCTOR, pass timeLimit in milliseconds ********/
  public Wait (TapIt tapIt, int timeLimit) {
    this.tapIt = tapIt;
	barWidth *= this.tapIt.width;
    barHeight *= this.tapIt.height;
    barX *= this.tapIt.width;
    barY *= this.tapIt.height;
    textSize *= this.tapIt.height;

    this.timeLimit = timeLimit;
    timer = new Timer(this.tapIt, timeLimit);
    timer.start();
  }


  // to be called in the draw loop
  public void displayAndUpdate() {
    this.tapIt.background(BG);
    this.tapIt.rectMode(TapIt.CORNER);

    // draw waiting text
    this.tapIt.textAlign(TapIt.CENTER, TapIt.BASELINE);
    this.tapIt.textSize(textSize);
    this.tapIt.fill(TEXTCOLOR);
    this.tapIt.text("Ready?", barX, barY - barHeight * 1.5f);
    this.tapIt.textAlign(TapIt.LEFT, TapIt.BASELINE);

    // calculate bar width based on elapsed time, draw bar
    float bw = (TapIt.min(timeLimit, timer.elapsedTime()) / (float) timeLimit) * barWidth; 
    this.tapIt.fill(barColor);
    this.tapIt.rect(barX - barWidth / 2.0f, barY - barHeight / 2.0f, bw, barHeight, barHeight * roundedFraction);
  }

  // query Wait to see if its allotted time is up
  public boolean timeIsUp() {
    return timer.timeIsUp();
  }
}