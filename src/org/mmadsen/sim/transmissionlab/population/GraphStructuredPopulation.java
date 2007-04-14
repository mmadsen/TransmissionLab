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

package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Apr 7, 2007
 * Time: 7:29:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class GraphStructuredPopulation implements IAgentPopulation {
    private Graph agentGraph = null;
    private Map<IAgent,Vertex> agentVertexMap = null;
	private ArrayList<IAgent> agentList = null;
	private int numAgents = 0;
	private int initialVariant = 1;
	private Log log = null;
	private int currentMaxVariant = 0;
    

    public GraphStructuredPopulation(int numAgents, Log log) {
        this.numAgents = numAgents;
		this.log = log;
		this.agentList = new ArrayList<IAgent>();
        this.agentVertexMap = new HashMap<IAgent,Vertex>();
        int curVariant = this.initialVariant;
    }
    

    public int getPopulationSize() {
        return 0;
    }

    public List<IAgent> getAgentList() {
        return null;
    }

    public int getCurrentMaximumVariant() {
        return 0;
    }

    public void replaceAgentList(List<IAgent> newAgentList) {

    }

    private void generateAgents() {
        
    }




}
