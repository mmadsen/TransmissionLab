package org.mmadsen.sim.transmissionlab.interfaces;

import org.mmadsen.sim.transmissionlab.util.SimParameterOptionsMap;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Feb 23, 2008
 * Time: 3:33:35 PM
 * To change this template use File | Settings | File Templates.
 */

/*
    Interface which should be implemented by any class which expects to inject
    simulation parameters and options into the main Model for placement in the
    GUI panel.  This method allows the ISimulationModel class to retrieve a
    standard set of parameters and option Enums and add them to the
    GUI panel for configuration.
 */

public interface IParameterized {
    public SimParameterOptionsMap getSimParameterOptionsMap();
}
