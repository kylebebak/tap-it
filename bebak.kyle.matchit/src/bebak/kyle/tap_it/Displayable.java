package bebak.kyle.tap_it;

import processing.core.PGraphics;

/**
 * This is base class for objects which can be drawn on the screen.
 * If an object extends Displayable it means that it may be drawn with the display() method. 
 * 
 * @author Dimitry Kireyenkov <dimitry@languagekings.com>
 */
public abstract class Displayable {
	
	/**
	 * Renders visual representation of the object
	 * into PGraphics pg. The PGraphcs should be 
	 * already "opened" with beginDraw() methods.
	 * @param pg
	 */
	abstract void display(PGraphics pg);

}
