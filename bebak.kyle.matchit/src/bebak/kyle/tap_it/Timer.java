package bebak.kyle.tap_it;

public class Timer {
  /**
	 * 
	 */
	private final MatchIt sketch;
private int startTime = 0, totalElapsed = 0;
  private boolean running = false;
  private int timeLeft = 0;

  /**
   * Just constant holding the name of current class for logging purposes.
   * 
   * For calling Log.v(LOG, "My log message created by curent class");
   */
  private final String LOG = this.getClass().getSimpleName();  
  
  /****************************
   * for a countdown timer, constructor sets time left in milliseconds
   ****************************/
  public Timer(MatchIt matchIt, int timeLeft) {
    sketch = matchIt;
	this.timeLeft = timeLeft;
  }

  /************ for countdown timer *************/
  public void addTime(int time) {
    timeLeft += time;
  }

  public void subtractTime(int time) {
    timeLeft -= time;
  }

  public boolean timeIsUp() {
    return (remainingTime() < 0);
  }

  /************ for count up timer *************/
  public void addTimeCountUp(int time) {
    startTime -= time;
  }

  public void subtractTimeCountUp(int time) {
    startTime += time;
  }

  /****************************
   * start timer, stop timer, reset timer
   ****************************/
  public void start() {
    if (running) 
      return;

    startTime = sketch.millis();
    running = true;
  }

  public void stop() {
    if (!running)
      return;

    totalElapsed += (sketch.millis() - startTime);
    running = false;
  }

  public void reset() {
    totalElapsed = 0;

    if (running) 
      startTime = sketch.millis();
    else
      startTime = 0;
  }


  /****************************
   * get elapsed time or remaining time in milliseconds, check whether timer is running
   ****************************/
  public int elapsedTime() {
    if (running) 
      return (sketch.millis() - startTime + totalElapsed);
    else 
      return (totalElapsed);
  }

  public int remainingTime() {
    return timeLeft - elapsedTime();
  }

  public boolean isRunning() {
    return running;
  }


  /****************************
   * get different units of an arbitrary time, input in milliseconds
   ****************************/
  public int thousandths(int time) {
    return (time % 1000);
  }

  public int second(int time) {
    return (time / 1000) % 60;
  }

  public int minute(int time) {
    return (time / (1000*60)) % 60;
  }

  public int hour(int time) {
    return (time / (1000*60*60)) % 24;
  }




  /****************************
   * get a string represenation of an arbitrary time, elapsed time, or remaining time.
   * Accurate from hours down to units of .01 seconds
   ****************************/
  public String timeToStringHours(int time) {

    return MatchIt.nf(hour(time), 1) + ":" + MatchIt.nf(minute(time), 2) + ":" + MatchIt.nf(second(time), 2) + 
      "." + MatchIt.nf(thousandths(time) / 10, 2);
  }

  public String elapsedTimeToStringHours() {
    return timeToStringHours(elapsedTime());
  }

  public String remainingTimeToStringHours() {
    if (remainingTime() < 0)
      return "-" + timeToStringHours(-remainingTime());

    return timeToStringHours(remainingTime());
  }


  // Accurate from 1 digit of minutes down to units of .01 seconds
  public String timeToStringMinutes(int time) {

    return MatchIt.nf(minute(time), 1) + ":" + MatchIt.nf(second(time), 2) + 
      "." + MatchIt.nf(thousandths(time) / 10, 2);
  }

  public String elapsedTimeToStringMinutes() {
    return timeToStringMinutes(elapsedTime());
  }

  public String remainingTimeToStringMinutes() {
    if (remainingTime() < 0)
      return "-" + timeToStringMinutes(-remainingTime());

    return timeToStringMinutes(remainingTime());
  }
}