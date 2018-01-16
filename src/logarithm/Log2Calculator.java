package logarithm;

public class Log2Calculator implements LogCalculator {
	
	@Override
	public  double calc(double x) {
		if (x < 0.0) {
			throw new IllegalArgumentException("Expected x>=0.0");
		}
		return x > 0.0 ? Math.log(x) / Math.log(2.0) : 0.0;
	}
}
