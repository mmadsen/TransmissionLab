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

package org.mmadsen.sim.transmissionlab.interfaces;

import org.apache.commons.logging.Log;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModel;
import uchicago.src.sim.util.RepastException;

import java.io.FileWriter;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Apr 1, 2007
 * Time: 8:09:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ISimulationModel extends SimModel, ISharedDataManager {
    int getLengthSimulationRun();

    void setLengthSimulationRun(int lengthSimulationRun);

    Log getLog();

    void removeSharedObject(String key);

    Collection<Object> retrieveAllAsCollection();

    Object retrieveSharedObject(String key);

    void storeSharedObject(String key, Object value);

    Boolean getBatchExecution();

    void setBatchExecution(Boolean batchExecution);

    IAgentPopulation getPopulation();

    @SuppressWarnings({"UnusedDeclaration"})
    void setPopulation(IAgentPopulation population);

    String getUniqueRunIdentifier();

    Schedule getSchedule();

    FileWriter getFileWriterForPerRunOutput(String filename);
    FileWriter getFileWriterForMultipleRunOutput(String filename);
    Boolean testFileExistsInDataDirectory(String filename);

    /**
     * preModelLoadSetup() is implemented in order to allow models to
     * gather information from subclasses which require object instantiation
     * AFTER the model constructor runs but before the model is handed to loadModel()
     * in the repast core.  This is very useful for dynamically adding parameters
     * to the GUI parameter panel, for example.
     */
    void preModelLoadSetup();

    void setup();

    /**
     * specificModelSetup() *must* be implemented by subclasses of AbstractTLModel and is where
     * model-specific construction occurs that normally would go into the setup() method of
     * SimModelImpl subclasses.  In TransmissionLab models, the AbstractTLModel has a "templated"
     * version of setup() which sets up standard fields, collections, schedules, and other
     * infrastructure, and calls the concrete model class at an appropriate and "safe" moment
     * for the concrete model to do its own setup.  In particular, in this method, you will
     * want to instantiate IDataCollector classes and add them to the data collector list,
     * initialize all of the random number generators you need, etc.
     */
    void specificModelSetup();

    /**
     * resetSpecificModel() *must* be implemented by subclasses of AbstractTLModel and is where
     * model-specific cleanup occurs upon the "reset" button (or batch run iteration) being pressed.
     * This method doesn't have to necessarily have any content, but if you construct data
     * structures in the concrete model class which need to be garbage-collected and reinitialized
     * for each simulation run, this is the place to NULL them out.
     */
    void resetSpecificModel();

    void buildPostParameterInitialization();

    void buildSpecificPopulation();

    void buildSpecificPopulationRules();

    /**
     * buildSpecificPerRunIdentifier() must be implemented to produce a unique identifier for
     * each run of the model - this is especially important in batch mode 
     */
    void buildSpecificPerRunIdentifier();

    void begin();

    String getFileOutputDirectory();

    void setFileOutputDirectory(String fileOutputDirectory);

    long getRngSeed();

    Object getSimpleModelPropertyByName(String propertyName) throws RepastException;

    void setModelPropertyByName(String property, Object value) throws RepastException;
    
}
