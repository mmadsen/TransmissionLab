package org.mmadsen.sim.transmissionlab.interfaces;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 19, 2008
 * Time: 8:49:39 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IStructuredPopulationWriter {
    public enum WriterType {GraphML, Pajek};

    public void saveGraphToFile(String filename, WriterType outputFormat);
}
