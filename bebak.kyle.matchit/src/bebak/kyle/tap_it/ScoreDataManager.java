package bebak.kyle.tap_it;

/**
 * The sole purpose of this class is to abstract all operations on persisting
 * or loading from persisted storage of score information.
 * 
 * <p>
 * Basically what we want to achieve is: in every place of the game where we need
 * to load scores/persist scores, we just call this class methods. This class methods
 * provide abstract operations like:
 * <li>loadScores()
 * <li>persistScores()
 * <li>addScore(x,y,z);
 * <li>updateScore(id, x,y,z);
 * 
 * <p>
 * The benefit of introducing this class is that we "abstract away" from the implemenation:
 * whether scores are persisted as files, or as SQLite db or persisted in remote online data storage.
 * 
 * <p>
 * All we care about: initialize this ScoreDataManager and after access it via it's public methods.
 * </p>
 * 
 * @author Ernesto Guevara
 *
 */
public class ScoreDataManager {
	 
	 /**
	  * Loads into ScoreDataManager scores available in the persistent storage.
	  */
	 public void loadScores(){
		 
	 }
	 
	 /**
	  * Stores current scores into persistent storage.
	  */
	 public void persistScores(){
	 
	 }
	 
	 /**
	  * Adds score to memory buffer. If not persisted, score will be lost upon app termination/exit/restart
	  * @param x
	  * @param y
	  * @param z
	  */
	 public void addScore(int x,int y, int z){
		 
	 }
	 
	 /**
	  * Updates score with given id in memory. Again if you want to make sure this is saved, call to
	  * {@link #persistScores()}.
	  * @param id
	  * @param x
	  * @param y
	  * @param z
	  */
	 public void updateScore(int id, int x, int y, int z){
		 
	 }

}
