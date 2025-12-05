package example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default in-memory registry with a simple list of rules.
 */
public class DefaultRuleRegistry implements RuleRegistry {

	private final List<LintRule> rules = new ArrayList<>();

	public DefaultRuleRegistry() {
		rules.add(new NamingConventionRule());
		rules.add(new EqualsHashCodeRule());
		rules.add(new RedundantInterfaceRule());
		rules.add(new NonPublicConstructorRule());
	}

	@Override
	public List<LintRule> rules() {
		return Collections.unmodifiableList(rules);
	}

	public void register(LintRule rule) {
		if (rule != null) {
			rules.add(rule);
		}
	}
}
