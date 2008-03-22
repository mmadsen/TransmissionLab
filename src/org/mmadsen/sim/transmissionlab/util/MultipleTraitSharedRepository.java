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

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MultipleTraitSharedRepository is a version of a shared object repository designed to segregate
 * shared objects into buckets by trait number, since each trait will be analyzed and
 * reported separately, from a primary data perspective.
 *
 * a shared repository is a utility class for inter-module "sharing" of data within a model
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


public class MultipleTraitSharedRepository {
	private ConcurrentMap<String, Object> repositoryMap = null;
    private List<ConcurrentMap<String,Object>> traitRepositoryList = null;
    private int numTraitsToHold = 0;

    public MultipleTraitSharedRepository(int numTraitsToHold) {
        this.traitRepositoryList = new ArrayList<ConcurrentMap<String,Object>>();

        for(int i = 0; i < this.numTraitsToHold; i++) {
            this.traitRepositoryList.set(i, new ConcurrentHashMap<String, Object>());
        }
    }

	public void putEntry(int traitSlot, String key, Object value) {
        this.traitRepositoryList.get(traitSlot).put(key, value);
    }

	public Object getEntry(int traitSlot, String key) {
		return this.traitRepositoryList.get(traitSlot).get(key);
	}

	public void removeEntry(int traitSlot, String key) {
        this.traitRepositoryList.get(traitSlot).remove(key);
    }

	public Collection<Object> getAllEntries(int traitSlot) {
        return this.traitRepositoryList.get(traitSlot).values();
    }
}