package junit.localtests.delayedCreation;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class DelayedCreation {

	
	@Test
	public void testDelayedCallback() throws InterruptedException{
		POPSystem.initialize();
		
		A a1 = PopJava.newActive(A.class);
		assertEquals(1234, a1.getTestValue());
		Thread.sleep(2000);
		A a2 = PopJava.newActive(A.class);
		assertEquals(1234, a2.getTestValue());
		
		POPSystem.end();
	}
	
	@Test
	/**
	 * Reproduces a bug where asynch constructors would be slower than the application itself.
	 * Meaning, objects are still starting up when the application already terminated.
	 */
	public void testFastShutdown(){
		POPSystem.initialize();
		
		A [] array = new A[10];
		
		for(int i = 0; i < array.length; i++){
			array[i] = PopJava.newActive(A.class);
		}
		
		POPSystem.end();
	}
}
