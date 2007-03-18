/**
 * 
 */
package org.mmadsen.sim.transmissionlab.interfaces;

import org.apache.commons.collections.Transformer;

/**
 * @author mark
 * Interface IPopulationTransformationRule specifies the most generic contract for 
 * handling population-level processes in an agent-based model.  We operate with 
 * several assumptions:
 * 
 * (1) A rule is given a reference to an IAgentPopulation object, and must return
 * and IAgentPopulation object.  This means the rule as a whole follows the 
 * Transformer interface from Jakarta Commons Collections Functors.  
 * 
 * (2) A model, and consequently IDataCollector modules, cannot assume that the 
 * IAgentPopulation object is actually *the same* object between pre- and post-
 * execution of the rule.  Any classes external to a transformer must refresh their 
 * reference to the IAgentPopulation before proceeding.  The rationale for this 
 * stricture is that we can envision models where agent populations "die" in every
 * step and are replaced with a population "born" out of a sampling rule from the 
 * previous generation -- thus any references to agents or even to a List of agents
 * might be invalid.  
 * 
 * (3) Multiple rules are handled by creating a ChainedTransformer in the model, 
 * which hands the output of one transformer to the next transformer in the chain
 * as input.  The final transformer's return value is then the return value of 
 * transformer chain as a whole.  
 * 
 * No assumptions are made about the contents of a transformation rule at 
 * the level of this interface contract.  However, my intention is that rules 
 * be composed, to the extent possible, of smaller scale pieces.  Decision logic
 * within a rule should be composed of Predicates, and actions taken by a rule 
 * should be composed of Closures.  This can't be enforced in any generic way,
 * but I will provide examples of how to do this.  The rationale here is that 
 * since Closures and Predicates can be composed dynamically at runtime, we can 
 * easily envision agent and population rules that actually "evolve" as a population
 * process -- something that is very hard to model by writing procedural code.  This
 * opens the door to meaningfully examining cultural transmissionlab not just of "token"-like
 * traits, but traits which actually govern behavior -- but without hardcoding this into 
 * agent classes. 
 */
public interface IPopulationTransformationRule extends Transformer {
	// no public methods beyond Transformer required at this time
	// but having rules implement a custom interface gives us the 
	// opportunity to require more methods at a future time.
}
