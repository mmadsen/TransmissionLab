package org.mmadsen.sim.transmissionlab.util;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 17, 2008
 * Time: 3:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TypedEdge<E extends Enum<E>> {
    private E edgeType = null;
    public TypedEdge() { }
    public TypedEdge(E edgeType) {
        this.edgeType = edgeType;
    }

    public E getEdgeType() {
        return this.edgeType;
    }

    public void setEdgeType(E newEdgeType) {
        this.edgeType = newEdgeType;
    }
}
