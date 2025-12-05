package example;

/**
 * Output channel for lint results.
 */
public interface Reporter {

	void report(String className, LintResult result);
}
