package com.bebak.kyle.tapit;

import java.util.Collections;
import java.util.Stack;

import com.bebak.kyle.tapit.TapIt.Card;

public class Deck {

  /**
	 * 
	 */
	private final TapIt sketch;
private Stack<Card> cards;
  private float x;
  private float y;

  private float r = .22f; // * width
  private final int border = Utils.color(0, 255, 255);
  private float cardThickness = .00085f; // * width
  private final int maxNumEllipses = 9;

  // create an empty deck at the specified location
  public Deck(TapIt tapIt, float x, float y) {
    sketch = tapIt;
	r *= sketch.width;
    cardThickness *= sketch.width;
    
    cards = new Stack<Card>();
    this.x = x;
    this.y = y;
  }



  // add a card to the top of the deck
  public void addCard(Card c) {
    x += cardThickness;
    y += cardThickness;
    c.changePosition(x, y);
    c.resetAngles();
    cards.push(c);
  }

  // remove and return the top card
  public Card removeTop() {
    x -= cardThickness;
    y -= cardThickness;
    if (cards.size() == 0)
      return null;
    return cards.pop();
  }

  // return the number of cards in the deck
  public int getSize() {
    return cards.size();
  }

  // shuffle the __remaining__ cards in the deck
  public void shuffleDeck() {
    Collections.shuffle(cards);
    Stack<Card> newCards = new Stack<Card>();
    while (!cards.isEmpty ())
      newCards.push(this.removeTop());
    while (!newCards.isEmpty ())
      this.addCard(newCards.pop());
  }

  // return the X coord of the top card in the deck
  public float X() {
    return x;
  }

  // return the Y coord of the top card in the deck
  public float Y() {
    return y;
  }

  // change the location of the deck on the canvas
  public void changePosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  // return the index of symbol at the given x and y coordinates
  // for the top card in the deck or -1 if no symbol at these coordinates
  public int indexAtPosition(float x, float y) {
    return cards.peek().indexAtPosition(x - this.x, y - this.y);
  }




  // gives appearance of a stack of cards. THIS CAUSES SLOWDOWN IF TOO MANY ELLIPSES ARE DRAWN
  public void displayDeck() {
    sketch.fill(border);
    sketch.noSmooth(); // ellipses for cards will render faster
    
    int n = TapIt.min(maxNumEllipses, cards.size());
    
    float thickness = cardThickness;
    if (cards.size() > maxNumEllipses)
      thickness = cardThickness * (cards.size() / (float) maxNumEllipses);
    
    for (int c = 0; c < n - 1; c++) 
      sketch.ellipse(x - (n - c - 1) * thickness, 
      y - (n - c - 1) * thickness, 2 * r, 2 * r);
      
    sketch.smooth(); // make drawing smooth again
  }

  // display the front of the top card by calling the Card method
  public void displayTopFront() {
    if (cards.empty()) 
      return;
    cards.peek().displayFront();
  }

  // display the back of the top card by calling the Card method
  public void displayTopBack() {
    if (cards.empty())
      return;
    cards.peek().displayBack();
  }
}