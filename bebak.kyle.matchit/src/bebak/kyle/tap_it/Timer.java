package bebak.kyle.tap_it;

/**
 * Simple implementation of countdown timer.
 * 
 */
public class Timer {
  /**
	 * 
	 */
	//private final MatchIt sketch;
  private long startTime = 0, totalElapsed = 0;
  private boolean running = false;
  private long timeLeft = 0;

  /**
   * Just constant holding the name of current class for logging purposes.
   * 
   * For calling Log.v(LOG, "My log message created by curent class");
   */
  private final String LOG = this.getClass().getSimpleName();  
  
  /****************************
   * for a countdown timer, constructor sets time left in milliseconds
   ****************************/
  /**
   * Initialize countdown timer.
   * 
   * @param timeLeft countdown time left in milliseconds.
   */
  public Timer(long timeLeft) {
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

    startTime = System.currentTimeMillis();
    running = true;
  }

  public void stop() {
    if (!running)
      return;

    totalElapsed += (System.currentTimeMillis() - startTime);
    running = false;
  }

  public void reset() {
    totalElapsed = 0;

    if (running) 
      startTime = System.currentTimeMillis();
    else
      startTime = 0;
  }


  /****************************
   * get elapsed time or remaining time in milliseconds, check whether timer is running
   ****************************/
  public long elapsedTime() {
    if (running) 
      return (System.currentTimeMillis() - startTime + totalElapsed);
    else 
      return (totalElapsed);
  }

  public long remainingTime() {
    return timeLeft - elapsedTime();
  }

  public boolean isRunning() {
    return running;
  }

  
  /****************************
   * get different components of an arbitrary time, input in milliseconds
   * Eg. 16:45:01.342
   *     HH:mm:ss:ttt
   ****************************/
  /**
   * Returns ttt component of time. (thousand's of the second of the given timestamp) 
   * @param time
   * @return
   */
  public long  thousandths(long time) {
    return (time % 1000);
  }

  /**
   * Returns ss component of the given time parameter (seconds of the given time)
   * @param time
   * @return
   */
  public long  second(long  time) {
    return (time / 1000) % 60;
  }

  /**
   * Returns mm component of the given time parameter (minutes of the given time)
   * @param time
   * @return
   */
  public long  minute(long  time) {
    return (time / (1000*60)) % 60;
  }

  /**
   * Returns HH component of the given time parameter (hours of the timestamp)
   * @param time
   * @return
   */
  public long  hour(long  time) {
    return (time / (1000*60*60)) % 24;
  }




  /****************************
   * get a string represenation of an arbitrary time, elapsed time, or remaining time.
   * Accurate from hours down to units of .01 seconds
   ****************************/
  public String timeToStringHours(long time) {

	StringBuilder sb = new StringBuilder();
	sb.append(Utils.nf(hour(time), 1));
	sb.append(':');
	sb.append(Utils.nf(minute(time), 2));
	sb.append(':');
	sb.append(Utils.nf(second(time), 2));
	sb.append('.');
	sb.append(Utils.nf(thousandths(time) / 10, 2));
	return sb.toString();
	
//    return MatchIt.nf(hour(time), 1) 
//    		+ ":" 
//    		+ MatchIt.nf(minute(time), 2) 
//    		+ ":" 
//    		+ MatchIt.nf(second(time), 2) 
//    		+ "." 
//    		+ MatchIt.nf(thousandths(time) / 10, 2);
  }

  public String elapsedTimeToStringHours() {
    return timeToStringHours(elapsedTime());
  }

  public String remainingTimeToStringHours() {
    if (remainingTime() < 0)
      return "-" + timeToStringHours(-remainingTime());

    return timeToStringHours(remainingTime());
  }


  /**
   * Returns string representation of given time (timestamp) in format
   * <li>59:33.88
   * <li>mm:ss.dd 
   * <p>where last one are two last "dd" are decimals of the second.
   * <p> Accurate from 1 digit of minutes down to units of .01 seconds
   * @param time
   * @return
   */
  public String timeToStringMinutes(long time) {
	return String.format("%s:%s.%s", 
					Utils.nf(minute(time), 1),
					Utils.nf(second(time), 2),
					Utils.nf(thousandths(time) / 10, 2)
					);
	
//    return    MatchIt.nf(minute(time), 1) 
//    		+ ":" 
//    		+ MatchIt.nf(second(time), 2) 
//    		+ "." 
//    		+ MatchIt.nf(thousandths(time) / 10, 2);
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