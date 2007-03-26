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

/**
 * 
 */
package org.mmadsen.sim.transmissionlab.interfaces;

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
