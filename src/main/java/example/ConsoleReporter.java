package example;

/**
 * Reports lint results to standard output.
 */
public class ConsoleReporter implements Reporter {

	@Override
	public void report(String className, LintResult result) {
		if (result == null || !result.hasErrors()) {
			System.out.println("No violations for " + className);
			return;
		}

		for (Violation violation : result.getViolations()) {
			System.out.println(violation);
		}
	}
}
