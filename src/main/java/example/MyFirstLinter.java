package example;

public class MyFirstLinter {

	/**
	 * Legacy entry point; delegates to the current CLI for compatibility.
	 */
	public static void main(String[] args) throws Exception {
		LintCli.main(args);
	}
}
