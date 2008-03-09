package org.mmadsen.sim.transmissionlab.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Feb 23, 2008
 * Time: 3:18:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiverseClassPair<F,S> {
    private F first = null;
    private S second = null;

    public DiverseClassPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() { return this.first; }

    public S getSecond() { return this.second; }

    public String toString() {
        return "(" + this.first.toString() + ", " + this.second.toString() + ")";
    }
}

