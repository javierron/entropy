package logarithm;

public class LnCalculator implements LogCalculator {
	
	@Override
	public double calc(double x) {
		if (x < 0.0) {
			throw new IllegalArgumentException("Expected x>=0.0");
		}
		return x > 0.0 ? Math.log(x) : 0.0;
	}
}
