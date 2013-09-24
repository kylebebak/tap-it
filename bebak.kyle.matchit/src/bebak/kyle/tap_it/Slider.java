package bebak.kyle.tap_it;

public class Slider<Item> {
  
  /*
  The constructor for this DISCRETE slider accepts integer inputs for its rectangle specifications,
   and then a generic array of objects that are the permitted values for the slider. The slider always 
   returns one of these objects when getValue() is called, and it displays itself by calling
   Object.toString() on whichever object is currently chosen. The initial index and name of the slider
   are also specified in the constructor
   */
   
  /**
	 * 
	 */
	private final MatchIt parent;
int topcornerx, topcornery; // location of slider
  int dimx, dimy, dim; // width and height, slider horizontal if dim bigger
  float xbutton, ybutton; // value returned by slider, button position
  int ticks, tickval;
  Object[] values;

  float tickspacing, tickspacingclick; // computing these variables in setup makes slider run faster
  float minxpos, maxxpos, minypos, maxypos, buttonsize, buttonsizex, buttonsizey;
  int colorbox, coloractive, colorbutton, textcolor;
  boolean toggleMove, over, lockout; // lockout prevents 2 sliders from being activated at once
  String slidername;
  final float roundedFraction = .1f; // how rounded are button edges?
  final float buttonSizeFraction = .085f; // make active region of button larger or smaller than drawn button
  float TEXTSIZE = .025f; // * width
  


  /** Constructor **/
  Slider (MatchIt matchIt, int topcornerx, int topcornery, int dimx, int dimy, 
  Item[] values, int initialIndex, String slidername) {
    parent = matchIt;
	this.topcornerx = topcornerx; 
    this.topcornery = topcornery;
    this.dimx = dimx; 
    this.dimy = dimy;
    this.slidername = slidername;

    this.values = new Object[values.length];
    for (int i = 0; i < values.length; i++) 
      this.values[i] = values[i];
    ticks = values.length;
    tickval = MatchIt.constrain(initialIndex, 0, values.length - 1);

    dim = MatchIt.max(dimx, dimy);
    buttonsize = dim * buttonSizeFraction;

    if (dimx >= dimy) {
      buttonsizex = buttonsize;
      buttonsizey = dimy;
      minxpos = topcornerx - buttonsizex / 2.0f;
      maxxpos = topcornerx + dimx + buttonsizex / 2.0f;
      minypos = topcornery; 
      maxypos = topcornery + buttonsizey;
      xbutton = topcornerx + MatchIt.round( dim * tickval / (float) values.length);
      ybutton = topcornery + buttonsizey / 2.0f;
    } 
    else {
      buttonsizex = dimx;
      buttonsizey = buttonsize;
      minxpos = topcornerx; 
      maxxpos = topcornerx + buttonsizex;
      minypos = topcornery - buttonsizey / 2.0f; 
      maxypos = topcornery + dimy + buttonsizey / 2.0f;
      xbutton = topcornerx + buttonsizex / 2.0f;
      ybutton = topcornery + MatchIt.round( dim * tickval / (float) values.length);
    }

    colorbox = 0xff0093CB; 
    coloractive = 0xff00FFFD; 
    colorbutton = 0xffFFFFFF;
    textcolor = Utils.color(255);
    TEXTSIZE *= parent.width;

    tickspacing = dim / (float) (ticks - 1);
    tickspacingclick = dim / (float) ticks;
    if (dimx >= dimy)
      xbutton = topcornerx + tickval * tickspacing;
    else
      ybutton = topcornery + tickval * tickspacing;
  }

  public void update() {
    if (parent.mouseX > minxpos && parent.mouseX < maxxpos && 
      parent.mouseY > minypos && parent.mouseY < maxypos) over=true;
    else over = false;

    if (over && parent.mousePressed && !lockout) toggleMove = true;

    if (parent.mousePressed && !over && toggleMove==false) lockout = true;

    if (parent.mousePressed == false) {
      toggleMove = false;
      lockout = false;
    }

    if (toggleMove) {

      if (dimx >= dimy) {
        tickval = MatchIt.constrain(MatchIt.floor((parent.mouseX - topcornerx) / tickspacingclick), 0, ticks - 1);
        xbutton = topcornerx + tickval * tickspacing;
      }
      else {
        tickval = MatchIt.constrain(MatchIt.floor((parent.mouseY - topcornery) / tickspacingclick), 0, ticks - 1);
        ybutton = topcornery + tickval * tickspacing;
      }
    }
  }



  /** Display and update **/
  public void display() {

    parent.stroke(0);
    parent.rectMode(MatchIt.CORNER);

    if ((over || toggleMove) && !lockout) parent.fill(coloractive);
    else parent.fill(colorbox);

    if (dimx >= dimy) parent.rect(topcornerx - 1.5f * buttonsizex, topcornery, dim + 3 * buttonsizex, buttonsizey, dim * roundedFraction);
    else parent.rect(topcornerx, topcornery - 1.5f * buttonsizey, buttonsizex, dim + 3 * buttonsizey, dim * roundedFraction);

    parent.rectMode(MatchIt.CENTER);
    parent.fill(colorbutton, 185);
    parent.rect(xbutton, ybutton, buttonsizex, buttonsizey);

    parent.textAlign(MatchIt.LEFT);
    parent.textSize(TEXTSIZE);
    parent.fill(textcolor);

    String sliderText = new String();
    if (slidername.length() == 0) 
      sliderText = values[tickval].toString();
    else 
      sliderText = slidername + "  :  " + values[tickval].toString();
      
    if (dimx >= dimy)
      parent.text(sliderText, minxpos, minypos - .01f * parent.height);
    else 
      parent.text(sliderText, minxpos, minypos - 1.5f * buttonsizey - .01f * parent.height);
  }



  /***** Return current value of slider *****/
  public Item getValue() {
    return (Item) values[tickval];
  }

  public void changeTextColor(int c) {
    textcolor = c;
  }

  public void changeSliderColor(int cbox, int cactive, int cbutton) {
    colorbox = cbox; 
    coloractive = cactive; 
    colorbutton = cbutton;
  }
}