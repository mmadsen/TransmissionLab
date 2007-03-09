package org.mmadsen.sim.transmission.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.mmadsen.sim.transmission.interfaces.IPopulationTransformationRule;

/**
 * @author mark
 * Class PopulationRuleset takes instances of IPopulationTransformationRule and 
 * constructs an ordered stack of rules to handle execution of model-level actions.
 * The rules are executed in the order they occur in the transformer chain, which 
 * is the order in which they're added to the PopulationRuleset.  New rules can be 
 * added at any time, or the ruleset simply reinitialized by giving a new List of 
 * rules which will replace the old one.  The underlying rules are not allowed to 
 * keep state between invocations, so it's safe to modify the ruleset as long as
 * transform() isn't being executed.  
 * 
 * The add/replace methods might look a little odd here, but it's because we're
 * using the ChainedTransformer's static factory method to create a ChainedTransformer 
 * out of a Collection of objects that conform to the Transformer interface.  Thus,
 * whenever somebody adds a rule or replaces the whole rule set, we just nullify 
 * the previous one and recreate it, hence the private recreateRuleSet() method.  
 * 
 */

public class PopulationRuleset implements Transformer {
	private ChainedTransformer ruleset = null;
	private List<IPopulationTransformationRule> ruleList = null;

	public PopulationRuleset() {
		super();
		ruleList = new ArrayList<IPopulationTransformationRule>();
	}
	
	public PopulationRuleset(List<IPopulationTransformationRule> rulelist) {
		super();
		this.ruleList = new ArrayList<IPopulationTransformationRule>(rulelist);
		this.recreateRuleSet();
	}
	
	/**
	 * Add a rule to the existing set, retaining previous rules and their ordering.  
	 * The new rule will be added to the end of the ruleset, and the underlying 
	 * ruleset recreated.
	 * @param rule - instance of an IPopulationTransformationRule
	 */
	
	public void addRule(IPopulationTransformationRule rule) {
		this.ruleList.add(rule);
		this.recreateRuleSet();
	}
	
	/**
	 * Replace the entire ruleset with a List of IPopulationTransformationRule objects.
	 * Clears out the existing list of rules, adds the new list, and recreates the 
	 * underlying ruleset.  
	 * @param rulelist - List<IPopulationTransformationRule>
	 */
	public void replaceRuleList(List<IPopulationTransformationRule> rulelist ) {
		this.ruleList.clear();
		this.ruleList.addAll(rulelist);
		this.recreateRuleSet();
	}

	public Object transform(Object population) {
		return this.ruleset.transform(population);
	}

	private void recreateRuleSet() {
		this.ruleset = null;
		this.ruleset = (ChainedTransformer) ChainedTransformer.getInstance(this.ruleList);
	}
	
}
