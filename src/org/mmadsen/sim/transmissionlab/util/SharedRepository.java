package org.mmadsen.sim.transmissionlab.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class SharedRepository is a utility class for inter-module "sharing" of data within a model
 * Instances of the class are intended to be held within a centralized point, like the central 
 * model class, so that modules, rules, and other simulation classes can store and retrieve
 * information from it, essentially like a shared "blackboard."  (Basically, the idea comes from
 * tuple-space models...).
 * 
 * The internal representation is a ConcurrentHashMap to ensure that removal, replacement, and other 
 * map-altering operations are property synchronized, so we don't have to try/catch all over the place
 * for ConcurrentModificationExceptions, especially with asynchronous rulesets in the future.
 * 
 * Since this is centralized, we can't really be sure what the stored object types are, so casting
 * is needed on retrieval operations.
 * 
 * @author mark
 *
 */


public class SharedRepository {
	private ConcurrentMap<String, Object> repositoryMap = null;
	
	public SharedRepository() {
		repositoryMap = new ConcurrentHashMap<String, Object>();
	}
	
	public void putEntry(String key, Object value) {
		this.repositoryMap.put(key, value);
	}
	
	public Object getEntry(String key) {
		return this.repositoryMap.get(key);
	}
	
	public void removeEntry(String key) {
		this.repositoryMap.remove(key);
	}
	
	public Collection<Object> getAllEntries() {
		return this.repositoryMap.values();
	}
}
