package example;

import java.util.Arrays;
import java.util.List;

/**
 * Thin CLI entry that parses args and delegates to the application.
 */
public class LintCli {

	public static void main(String[] args) throws Exception {
		Command command = parse(args);
		LintApplication app = new LintApplication(
				new DefaultRuleRegistry(),
				new ConsoleReporter(),
				new PlantUmlGenerator(),
				new LLMDesignAdvisor());
		app.run(command);
	}

	static Command parse(String[] args) {
		if (args == null || args.length == 0) {
			return new Command(Mode.LINT, List.of());
		}
		String first = args[0];
		List<String> targets = args.length > 1 ? Arrays.asList(Arrays.copyOfRange(args, 1, args.length))
				: List.of();

		if ("--llm".equals(first) || "--advice".equals(first)) {
			return new Command(Mode.ADVICE, targets);
		}
		if ("--uml".equals(first)) {
			return new Command(Mode.UML, targets);
		}
		return new Command(Mode.LINT, Arrays.asList(args));
	}
}
