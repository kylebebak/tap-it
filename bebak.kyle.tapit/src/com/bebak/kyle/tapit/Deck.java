package com.bebak.kyle.tapit;

import java.util.Collections;
import java.util.Stack;

import processing.core.PGraphics;

/**
 * Abstract structure, just a collection(stack) of Cards.
 * AND AS WELL DECK RENDERING METHODS.
 * 
 * Also has some convenience methods and getters/setters.
 * 
 */
public class Deck {

  private Stack<Card> cards;
  
  /**
   * Contain which ?topleft position of the card?
   * What is "the Origin" of the deck? Top left corner?
   */
  private float x, y;

  /**
   * Radius of the "card-circle" as percentage of the screen width.
   */
  private float r = .22f; // * width
  
  private final int border = Utils.color(0, 255, 255);
  
  /**
   * Offset of each new card added to the deck (relative to previous card) as
   * percentage of the screen width.
   */
  private float cardThickness = .00085f; // * width
  
  /**
   * Max number of ellipses which can be displayed.
   * 
   * When displaying deck on screen, we can only display
   * a limited amount of ellipses. Thus we need to  define constant
   * limiting amount of ellipses we can fit.
   */
  private final int maxNumEllipses = 9;
  
  /**
   * Just constant holding the name of current class for logging purposes.
   * 
   * For calling Log.v(LOG, "My log message created by curent class");
   */
  private final String LOG = this.getClass().getSimpleName();  

  
  /**
   * Creates deck at specified location.
   * 
   * What exactly Deck is? Is it just "abstract" collection of cards?
   * 
   * @param tapIt reference to parent sketch
   * @param x ?what is this? top left position?
   * @param y
   * @param width is screen with in pixels passed to the deck so it can initialize itself with proper size.
   */
  public Deck( float x, float y, float width ) {
    // TODO: First r is "percentage" and then it turns into pixels. Once variable should hold 
    // 		 only one type of value. Should split it into two different variables: one constant for
    //		 percentage and another actual pixel width.
	r *= width;
    cardThickness *= width;
    
    cards = new Stack<Card>();
    this.x = x;
    this.y = y;
  }



  /**
   * Add a card to the top of the deck and adjusts cards position
   * to lay neatly on top of the deck.
   * 
   * Also advances XY coordinates of the deck. (Which means?)
   * It's important to know that "c" card's location will be modified
   * as the result of addition.
   * 
   * @param c
   */
  public void addCard(Card c) {
    x += cardThickness;
    y += cardThickness;
    c.changePosition(x, y);
    c.resetAngles();
    cards.push(c);
  }

  /**
   * Remove and return the top card.
   * 
   * @return NULL if no cards available.
   * 		
   */
  public Card removeTop() {
	// TODO: this seems to be a bug: if there's no cards to remove from top, the x,y 
	//		 would still be reduced.
    x -= cardThickness;
    y -= cardThickness;
    if (cards.size() == 0){
      return null;
    }
    return cards.pop();
  }

  /**
   * Return the number of cards in the deck
   * 
   * @return
   */
  public int getSize() {
    return cards.size();
  }

  /**
   * Shuffle the __remaining__ cards in the deck
   */
  public void shuffleDeck() {
    Collections.shuffle(cards);
    Stack<Card> newCards = new Stack<Card>();
    while (!cards.isEmpty ())
      newCards.push(this.removeTop());
    while (!newCards.isEmpty ())
      this.addCard(newCards.pop());
  }

  /**
   * Return the X coord of the top card in the deck.
   * 
   * @return
   */
  public float X() {
    return x;
  }

  /**
   * Return the Y coord of the top card in the deck.
   * 
   * @return
   */
  public float Y() {
    return y;
  }

  /** 
   * Change the location of the deck on the canvas.
   * 
   * @param x
   * @param y
   */
  public void changePosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Return the index of symbol at the given x and y coordinates
   * for the top card in the deck or -1 if no symbol at these coordinates.
   * 
   * @param x
   * @param y
   * @return
   */
  public int indexAtPosition(float x, float y) {
    return cards.peek().indexAtPosition(x - this.x, y - this.y);
  }




  /**
   * Gives appearance of a stack of cards. 
   * 
   * THIS CAUSES SLOWDOWN IF TOO MANY ELLIPSES ARE DRAWN
   * 
   * In order to decouple this method from sketch, I have introduced parameter
   * 	PGraphics to this method. Now this method and this class can be reused in
   * 	any other part of this game or even another game. 
   * 
   * @param pg is graphics context (previously initialized with beginDraw()) in which drawing
   * 		should take place. Be sure to call pushStyle() / popStyle() to ensure that you're not changing
   * 		drawing style of the graphics for other callers.
   */
  public void displayDeck(PGraphics pg) {
    pg.fill(border);
    pg.noSmooth(); // ellipses for cards will render faster
    
    int n = TapIt.min(maxNumEllipses, cards.size());
    
    float thickness = cardThickness;
    if (cards.size() > maxNumEllipses)
      thickness = cardThickness * (cards.size() / (float) maxNumEllipses);
    
    for (int c = 0; c < n - 1; c++) 
      pg.ellipse(x - (n - c - 1) * thickness, 
      y - (n - c - 1) * thickness, 2 * r, 2 * r);
      
    pg.smooth(); // make drawing smooth again
  }

  /**
   * Display the front of the top card by calling the Card method.
   */
  public void displayTopFront() {
    if (cards.empty()) 
      return;
    cards.peek().displayFront();
  }

  /**
   * Display the back of the top card by calling the Card method.
   */
  public void displayTopBack() {
    if (cards.empty())
      return;
    cards.peek().displayBack();
  }
}