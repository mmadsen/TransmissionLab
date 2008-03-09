package org.mmadsen.sim.transmissionlab.util;

import org.apache.commons.collections15.Factory;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 4, 2008
 * Time: 9:58:05 AM
 * To change this template use File | Settings | File Templates.
 *
 * Graph "Edges" are really simple in JUNG2, they can have structure or not.  In this
 * case edges are just carrying a numerical ID -- in other words, the edge *is* an auto-incrementing integer,
 * unique within a given run, since this factory is implemented as a Singleton pattern.
 *
 * Get an instance to this singleton by calling GraphEdgeFactory.getInstance().  
 *
 */
public final class GraphEdgeFactory implements Factory<Integer> {
    private int edgeID = 0;
    private static GraphEdgeFactory instance = null;

    //
    private GraphEdgeFactory() {

    }

    public static GraphEdgeFactory getInstance() {
        if (instance == null ) { instance = new GraphEdgeFactory(); }
        return instance;
    }

    public Integer create() {
        return this.edgeID++;
    }
}
