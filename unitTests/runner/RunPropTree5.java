package runner;


import java.util.List;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import phenoscape.queries.TestPropTree1;
import phenoscape.queries.TestPropTree5;

public class RunPropTree5 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Result r = org.junit.runner.JUnitCore.runClasses(TestPropTree5.class);
		System.err.println("Run count: " + r.getRunCount());
		System.err.println("Failure count: " + r.getFailureCount());
		List<Failure> fList = r.getFailures();
		for (Failure f : fList){
			System.err.println(f.getTestHeader());
			System.err.print("  ");
			System.err.println(f.getException());
			System.err.println("");
		}
		

	}

}
