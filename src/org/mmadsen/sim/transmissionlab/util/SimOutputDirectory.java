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

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Aug 18, 2007
 * Time: 11:40:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimOutputDirectory {
    private File perRunOutputDirectory = null;
    private File parentOutputDirectory = null;
    private ISimulationModel model = null;
	private Log log = null;


    public SimOutputDirectory(ISimulationModel m) {
        this.model = m;
        this.log = this.model.getLog();
        this.parentOutputDirectory = new File(this.model.getFileOutputDirectory());
        this.createPerRunOutputDirectoryFilehandle();
    }

    private void createPerRunOutputDirectoryFilehandle() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.model.getFileOutputDirectory());
        File dirParent = new File(sb.toString());
        this.perRunOutputDirectory = new File(dirParent, this.model.getUniqueRunIdentifier());

        try {
            this.perRunOutputDirectory.mkdir();
        }
        catch (SecurityException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }
    }

    public String getOutputDirectoryName() {
        return this.perRunOutputDirectory.toString();
    }

    public FileWriter getFileWriterForPerRunOutput(String filename) {
        File outputFile = new File(this.perRunOutputDirectory, filename);
        FileWriter outFileWriter = null;
        try {
            outFileWriter = new FileWriter(outputFile, true);
        }
        catch (IOException ioe) {
			log.info("IOException on filepath: "+ outputFile.toString() + ": " + ioe.getMessage());
		}
        return outFileWriter;   
    }

    public FileWriter getFileWriterForMultipleRunOutput(String filename) {
        File outputFile = new File(this.parentOutputDirectory, filename);
        FileWriter outFileWriter = null;
        try {
            outFileWriter = new FileWriter(outputFile, true);
        }
        catch (IOException ioe) {
			log.info("IOException on filepath: "+ outputFile.toString() + ": " + ioe.getMessage());
		}
        return outFileWriter;
    }

    public Boolean testFileExistsInDataDirectory(String filename) {
        File targetFile = new File(this.parentOutputDirectory, filename);
        return targetFile.exists();
    }
}
