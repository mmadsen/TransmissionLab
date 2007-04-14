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

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Apr 14, 2007
 * Time: 11:57:12 AM
 * To change this template use File | Settings | File Templates.
 */
/**
	 * TraitCount is a value class for tracking trait frequencies.
	 * We use a value class rather than just primitive types held
	 * in collections because we want to make it easy to get a custom
	 * sort order, based on trait frequency (in this case, make it easy
	 * to recover the "top N" traits, by frequency in descending order.
	 * Thus, we implement Comparable and store the count and trait ID.
	 * @author mark
	 *
	 */

public class TraitCount implements Comparable {
    private Integer trait = null;
    private Integer count = 0;

    public TraitCount(Integer t) {
        this.trait = t;
        this.count = 1;
    }

    public void increment() {
        this.count++;
    }

    public void decrement() {
        this.count--;
    }

    public Integer getTrait() {
        return this.trait;
    }

    public Integer getCount() {
        return this.count;
    }

    public int getCountAsPrimitiveInt() {
        return this.count;
    }

    public int compareTo(Object arg0) {
        // MEM (v1.3): removed the explicit sign reversal, which was hackish
        // and possibly fragile, in favor of an explicit Collections.reverse() in process().
        return this.count.compareTo(((TraitCount)arg0).getCount());
    }

}