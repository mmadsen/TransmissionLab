/*
 * Copyright (c) 2007, Mark E. Madsen, Alex Bentley, and Carl P. Lipo. All Rights Reserved.
 *
 * This code is offered for use under the terms of the Creative Commons-GNU General Public License
 * http://creativecommons.org/licenses/GPL/2.0/
 *
 * Our intent in licensing this software under the CC-GPL is to provide freedom for researchers, students,
 * and other interested parties to replicate our research results, pursue their own research, etc.  You are, however,
 * free to use the code contained in this package for whatever purposes you wish, provided you adhere to the
 * open license terms specified in LICENSE and GPL.txt
 *
 * See the files LICENSE and GPL.txt in the top-level directory of this source archive for the license
 * details and grant.
 */

package org.mmadsen.sim.transmissionlab.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;

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
