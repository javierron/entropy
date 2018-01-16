package org.zeromq;

import incremental.Calculator;
import incremental.StreamElement;
import incremental.damping.DampingEntropyCalculator;
import incremental.sliding.SlidingEntropyCalculator;
import incremental.util.StringClassGenerator;
import incremental.util.NumericClassGenerator;

import entropy.Units;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.zeromq.*;

/**
 * A simple demonstration of how to use the calculators. Recall that the entropy
 * of a 4-valued uniform random variable is log_2 4 = 2.0, and that its Gini
 * index is 1-1/4 = 3/4 = 0.75.
 * 
 * In general, the entropy of a uniform n-valued discrete random variable is
 * log_2 n, and the Gini index of this variable is 1-1/n.
 * 
 * @author Jean Paul Barddal (jpbarddal@gmail.com).
 * @author Blaz Sovdat (blaz.sovdat@gmail.com).
 *
 */
public class SimpleUsageExample {
	
	class item_data_t {
		public int type;
		public int hash_value;
	}
	
	
	public static void main(String[] args) {
		final int numClasses = 100;
		final int numValues = 10000;
		
		item_data_t next_item;

		
		//StringClassGenerator generator = new StringClassGenerator(numClasses);
		NumericClassGenerator generator = new NumericClassGenerator(numClasses);
		
		ZMQ.Context ctx = new ZMQ.Context(0);
        ZMQ.Socket client = ctx.socket(ZMQ.SUB);
		
        client.connect("tcp://localhost:5555");
        
        
        

		@SuppressWarnings("unchecked")
		Calculator<Integer>[] calculators = new Calculator[2];

		Units units = Units.NATS;
		//Units units = Units.BITS;
		
		
		
		calculators[0] = new SlidingEntropyCalculator<Integer>(200, units);
		calculators[1] = new DampingEntropyCalculator<Integer>(0.99999, units);

		for (int i = 0; i < numValues; i++) {
			
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(client.recv()));
				next_item = (item_data_t) in.readObject();
				in.close();
				
				
				StreamElement<Integer> value = generator.get_stream_element(next_item.hash_value);
				for (Calculator<Integer> calc : calculators) {
					calc.process(value);
				}
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}

		for (Calculator<Integer> calc : calculators) {
			System.out.println(calc.getClass().getName() + ": " + calc.getValue());
		}
	}
}
