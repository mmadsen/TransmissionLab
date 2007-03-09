/**
 * 
 */
package org.mmadsen.sim.transmission.interfaces;

import java.util.Collection;

/**
 * @author mark
 * Interface ISharedDataManager represents any object that contains a centralized data repository
 * for objects to use in a synchronized, concurrency-safe way.  Ordinarily, model classes will
 * implement this interface and thus should implement something as a storage mechanism.  One of the 
 * points of having an interface contract, however, is that the "clients" ought not to have any 
 * knowledge of what this implementation is.  
 *
 */

public interface ISharedDataManager {
	public void storeSharedObject(String key, Object value);
	public Object retrieveSharedObject(String key);
	public Collection<Object> retrieveAllAsCollection();
	public void removeSharedObject(String key);
}
