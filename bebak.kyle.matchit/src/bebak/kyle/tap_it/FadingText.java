package bebak.kyle.tap_it;

public class FadingText {
  
  /**
	 * 
	 */
	private final MatchIt sketch;
/** A simple class that can instantiate fading text
   and keep track of its lifetime, like a toast in the android os **/

  private int age;
  private float x;
  private float y;
  private String text;
  private int textColor;

  private final int LIFE = 75;
  private float textSize = .09f; // * width 


  public FadingText(MatchIt matchIt, int textColor, String text, float x, float y) {
    sketch = matchIt;
	textSize *= sketch.width;

    age = 0; 
    this.text = text;
    this.x = x;
    this.y = y;
    this.textColor = textColor;
  }

  public boolean isDead() {
    return (age >= LIFE);
  }

  public void displayAndUpdate() {
    sketch.textAlign(MatchIt.CENTER, MatchIt.CENTER); // center text for added or subtracted time toasts

    age++;
    sketch.fill(textColor, 255.0f * MatchIt.sq(MatchIt.sq(1 - age / (float) LIFE)));
    sketch.textSize(textSize);
    sketch.text(text, x, y);

    sketch.textAlign(MatchIt.LEFT, MatchIt.BASELINE); // return textAlign to normal
  }
}