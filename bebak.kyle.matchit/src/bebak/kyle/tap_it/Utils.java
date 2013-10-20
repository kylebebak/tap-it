package bebak.kyle.tap_it;

public class Utils {
	
	/**
	 * Simulates what processing color() method does.
	 * 
	 * In Android we cannot use original processing color() method, thus 
	 * we have to replace it with this "simulation method".
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	static int  color(int r, int g, int b){
		int c =  0xFF000000;
		c |= (0xFF & r) << 16;
		c |= (0xFF & g) << 8;
		c |= (0xFF & b);
		return c;
	}
	
	
	/**
	 * Method which mimics what does color(255) or color(128) in processing.
	 * 
	 * @param blackIntensityByte
	 * @return
	 */
	static int color(int blackIntensityByte){
		blackIntensityByte = blackIntensityByte & 0xFF;
		return color(blackIntensityByte, blackIntensityByte, blackIntensityByte);
	}
	
	static String nf(long num, int digits){
		throw new UnsupportedOperationException("Not implemented ");
//		return "hellO";
	}


	public static float random(float ddp, float f) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not impelmented");
	}
}
