/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.tinytables.storage;

import java.io.Serializable;
import java.util.TreeMap;

import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

public interface TinyTablesStorage extends Serializable {

	/**
	 * Store a {@link TinyTable} for the protocol with the given <code>id</code>
	 * .
	 * 
	 * @param id
	 * @param table
	 */
	public void storeTinyTable(int id, TinyTable table);

	/**
	 * Looks up an entry in the TinyTable with the given id and index given by
	 * the inputs, eg. entry <i>t<sub>i<sub>0</sub>i<sub>1</sub> ...
	 * i<sub>j</sub></i> of the TinyTable with id <i>n</i> can be found as
	 * <code>lookupTinyTable(n, i<sub>0</sub>, i<sub>1</sub>, ..., i<sub>j</sub>)</code>
	 * .
	 * 
	 * @param id
	 * @param inputs
	 * @return
	 */
	public boolean lookupTinyTable(int id, boolean... inputs);

	public TinyTable getTinyTable(int id);

	/**
	 * Store a boolean for the protocol with the given ID. Can be used by a
	 * player to store a mask that he has picked during preprocessing.
	 * 
	 * @param id
	 * @param r
	 */
	public void storeMaskShare(int id, boolean r);

	public boolean getMaskShare(int id);

	/**
	 * Store an array of booleans for the protocol with the given ID. Note that
	 * these booleans are not stored after the preprocessing phase, and should
	 * only be used for data needed in the preprocessing.
	 * 
	 * @param id
	 * @param booleans
	 */
	public void storeTemporaryBooleans(int id, boolean[] booleans);

	public boolean[] getTemporaryBooleans(int id);

	/**
	 * The receiver of the OT protocol to be performed for the protocol with the
	 * given ID can store his sigma's. The OT-protocols will be executed when
	 * the evaluation of the prepro-protocol is finished, and the results is
	 * used to create the TinyTables for the AND protocols.
	 * 
	 * @param id
	 * @param sigmas
	 */
	public void storeOTSigma(int id, OTSigma[] sigmas);

	public TreeMap<Integer, OTSigma[]> getOTSigmas();

	/**
	 * The sender of the OT protocols to be performed can store his inputs. The
	 * OT-protocols will be executed when the evaluation of the prepro-protocol
	 * is finished, and the results is used to create the TinyTables for the AND
	 * protocols.
	 * 
	 * @param id
	 * @param inputs
	 */
	public void storeOTInput(int id, OTInput[] inputs);

	public TreeMap<Integer, OTInput[]> getOTInputs();

}