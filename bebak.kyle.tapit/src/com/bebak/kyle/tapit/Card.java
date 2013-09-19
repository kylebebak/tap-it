package com.bebak.kyle.tapit;

/**
 * DK: What is the purpose of this class? 
 * 
 * @author Kyle Bebak
 *
 */
public class Card {

  /**
 * 
 */
private final TapIt sketch;
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



  public Card(TapIt tapIt, int nSyms, int[] symIndices, float x, float y) {
    sketch = tapIt;
	r = TapIt.cardRadius; // making cardRadius final and static and initializing r here prevents a weird bug
    r *= sketch.width;
    borderWidth *= r;

    this.nSyms = nSyms;
    this.symIndices = symIndices;

    this.x = x;
    this.y = y;
    sr = 2.0f * r / TapIt.sqrt(nSyms); // new symbol radius
    sx = new float[nSyms];
    sy = new float[nSyms];
    sa = new float[nSyms];
    // initialize positions for symbols
    int c = 0;
card:
    while (c < nSyms) {
      float rad = sketch.random(0, r - sr / 2.0f); 
      // if the divisor of sr is greater than 1, some symbols will be outside card boundary
      float ang = sketch.random(0, 2.0f * TapIt.PI);
      float newx = rad * TapIt.cos(ang);
      float newy = rad * TapIt.sin(ang);

      for (int i = 0; i < c; i++) {
        if ( TapIt.dist(sx[i], sy[i], newx, newy) < 2.0f * sr) {
          sr *= radiusMultiplier; 
          continue card;
        }
      } 
      sx[c] = newx;
      sy[c] = newy;
      sa[c] = sketch.random(0, 2 * TapIt.PI);
      c++;
    }
  }




  public void displayFront() {
    sketch.imageMode(TapIt.CENTER);
    sketch.pushMatrix();
    sketch.translate(x, y);
    sketch.rotate(theta);

    sketch.fill(border);
    sketch.ellipse(0, 0, 2 * r, 2 * r);
    sketch.fill(front);
    sketch.noStroke();
    sketch.ellipse(0, 0, 2 * (r - borderWidth), 2 * (r - borderWidth));
    sketch.stroke(0);

    for (int c = 0; c < nSyms; c++) {
      sketch.pushMatrix();
      sketch.translate(sx[c], sy[c]);
      sketch.rotate(sa[c]);
      sketch.image(TapIt.imageAtIndex(symIndices[c]), 0, 0, 
      displaySizeMultiplier * 2 * sr, displaySizeMultiplier * 2 * sr);
      sketch.popMatrix();
    }

    sketch.popMatrix();
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
      if ( TapIt.dist(x, y, sx[i], sy[i]) < sr ) return symIndices[i];
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