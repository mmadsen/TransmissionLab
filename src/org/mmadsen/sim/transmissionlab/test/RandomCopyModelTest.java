package org.mmadsen.sim.transmissionlab.test;


import org.junit.After;
import org.junit.Before;
import org.mmadsen.sim.transmissionlab.models.TransmissionLabModel;

import junit.extensions.PrivilegedAccessor;	

/**
 * @author mark
 *
 */
public class RandomCopyModelTest {

	@SuppressWarnings("unused")
	private TransmissionLabModel model = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.model = (TransmissionLabModel) PrivilegedAccessor.instantiate(TransmissionLabModel.class);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

}
