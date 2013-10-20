package bebak.kyle.tap_it;


public class Wait {
  /*
This class has a time limit which is passed to the constructor, and a timer 
   which is started when the class is constructed. An instance of a class can
   be queried to see if its allotted time is up. The class can also display and update
   itself to show how much of its allotted time remains */

  /**
	 * 
	 */
	private final MatchIt matchIt;
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

  /**
   * Just constant holding the name of current class for logging purposes.
   * 
   * For calling Log.v(LOG, "My log message created by curent class");
   */
  private final String LOG = this.getClass().getSimpleName();  
  
  
  /******** CONSTRUCTOR, pass timeLimit in milliseconds ********/
  public Wait (MatchIt matchIt, int timeLimit) {
    this.matchIt = matchIt;
	barWidth *= this.matchIt.width;
    barHeight *= this.matchIt.height;
    barX *= this.matchIt.width;
    barY *= this.matchIt.height;
    textSize *= this.matchIt.height;

    this.timeLimit = timeLimit;
    timer = new Timer( timeLimit);
    timer.start();
  }


  // to be called in the draw loop
  public void displayAndUpdate() {
    this.matchIt.background(BG);
    this.matchIt.rectMode(MatchIt.CORNER);

    // draw waiting text
    this.matchIt.textAlign(MatchIt.CENTER, MatchIt.BASELINE);
    this.matchIt.textSize(textSize);
    this.matchIt.fill(TEXTCOLOR);
    this.matchIt.text("Ready?", barX, barY - barHeight * 1.5f);
    this.matchIt.textAlign(MatchIt.LEFT, MatchIt.BASELINE);

    // calculate bar width based on elapsed time, draw bar
    float bw = (MatchIt.min(timeLimit, timer.elapsedTime()) / (float) timeLimit) * barWidth; 
    this.matchIt.fill(barColor);
    this.matchIt.rect(barX - barWidth / 2.0f, barY - barHeight / 2.0f, bw, barHeight, barHeight * roundedFraction);
  }

  // query Wait to see if its allotted time is up
  public boolean timeIsUp() {
    return timer.timeIsUp();
  }
}