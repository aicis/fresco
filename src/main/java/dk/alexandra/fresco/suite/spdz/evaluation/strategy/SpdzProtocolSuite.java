/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz.evaluation.strategy;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import edu.biu.scapi.generals.Logging;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SpdzProtocolSuite implements ProtocolSuite {

    private static Map<Integer, SpdzProtocolSuite> instances;

    private SecureRandom rand;
    private SpdzStorage[] store;
    private ResourcePool rp;
    private int gatesEvaluated = 0;
    private int macCheckThreshold = 100000;
    private BigInteger keyShare, p;
    private MessageDigest[] digs;
    private SpdzConfiguration spdzConf;

    //This is set whenever an output protocol was evaluated. It means that the sync point needs to do a MAC check.
    private boolean outputProtocolInBatch = false;
    private long totalMacTime;
    private long lastMacEnd;
    private long totalNoneMacTime;

    public void outputProtocolUsedInBatch() {
        outputProtocolInBatch = true;
    }

    public SpdzProtocolSuite() {
    }

    public synchronized static SpdzProtocolSuite getInstance(int id) {
        if (instances == null) {
            instances = new HashMap<Integer, SpdzProtocolSuite>();
        }
        if (instances.get(id) == null) {
            instances.put(id, new SpdzProtocolSuite());
        }
        return instances.get(id);
    }

    public SpdzStorage getStore(int i) {
        return store[i];
    }

    public BigInteger getKeyShare() {
        return keyShare;
    }

    public BigInteger getModulus() {
        return p;
    }

    public SpdzConfiguration getConf() {
        return this.spdzConf;
    }

    public MessageDigest getMessageDigest(int threadId) {
        return this.digs[threadId];
    }

    @Override
    public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
        spdzConf = (SpdzConfiguration) conf;
        int noOfThreads = resourcePool.getVMThreadCount();
        this.store = new SpdzStorage[noOfThreads];
        for (int i = 0; i < noOfThreads; i++) {
            switch (spdzConf.getPreprocessingStrategy()) {
                case DUMMY:
                    store[i] = new SpdzStorageDummyImpl(resourcePool.getMyId(), resourcePool.getNoOfParties());
                    break;
                case STATIC:
                    store[i] = new SpdzStorageImpl(resourcePool, i);
                    break;
                case FUELSTATION:
                    store[i] = new SpdzStorageImpl(resourcePool, i, true, spdzConf.fuelStationBaseUrl());
            }
        }
        this.rand = resourcePool.getSecureRandom();
        this.rp = resourcePool;

        try {
            this.digs = new MessageDigest[noOfThreads];
            for (int i = 0; i < this.digs.length; i++) {
                this.digs[i] = MessageDigest.getInstance("SHA-256");
            }
        } catch (NoSuchAlgorithmException e) {
            Reporter.warn("SHA-256 not supported as digest on this system. Might not influence "
                    + "computation if your chosen SCPS does not depend on a hash function.");
        }

        try {
            this.store[0].getSSK();
        } catch (MPCException e) {
            throw new MPCException("No preprocessed data found for SPDZ - aborting.", e);
        }

        // Initialize various fields global to the computation.
        this.keyShare = store[0].getSSK();
        this.p = store[0].getSupplier().getModulus();
        Util.setModulus(this.p);
    }

    @Override
    public void synchronize(int gatesEvaluated) throws MPCException {
        this.gatesEvaluated += gatesEvaluated;
        this.synchronizeCalls++;
        if (this.gatesEvaluated > macCheckThreshold || outputProtocolInBatch) {
            try {
                for (int i = 1; i < store.length; i++) {
                    store[0].getOpenedValues().addAll(store[i].getOpenedValues());
                    store[0].getClosedValues().addAll(store[i].getClosedValues());
                    store[i].reset();
                }
                MACCheck();
            } catch (IOException e) {
                throw new MPCException("Could not complete MACCheck.", e);
            }
            this.gatesEvaluated = 0;
        }
    }

    @Override
    public void finishedEval() {
        try {
            MACCheck();
            this.gatesEvaluated = 0;
        } catch (IOException e) {
            throw new MPCException("Could not complete MACCheck.", e);
        }
    }

    private int logCount = 0;
    private int synchronizeCalls;

    private void MACCheck() throws IOException {
        long start = System.currentTimeMillis();
        if (lastMacEnd > 0)
            totalNoneMacTime += start - lastMacEnd;

        SpdzStorage storage = store[0];
        if (logCount++ > 1000) {
            Logging.getLogger().info("MacChecking. noOfGates=" + this.gatesEvaluated
                    + ", openedValuesSize=" + storage.getOpenedValues().size()
                    + ", SynchronizeCalls=" + synchronizeCalls
                    + ", MacTime=" + totalMacTime
                    + ", noneMacTime=" + totalNoneMacTime);
            logCount = 0;
            synchronizeCalls = 0;
        }
        SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(rand, this.digs[0], storage);

        int batchSize = 128;
        NativeProtocol[] nextProtocols = new NativeProtocol[batchSize];
        SCENetworkImpl sceNetworks = new SCENetworkImpl(this.rp.getNoOfParties(), 0);

        do {
            int numOfProtocolsInBatch = macCheck.getNextProtocols(nextProtocols, 0);
            BatchedStrategy.processBatch(nextProtocols, numOfProtocolsInBatch, sceNetworks, 0,
                    this.rp);
        } while (macCheck.hasNextProtocols());

        //reset boolean value
        this.outputProtocolInBatch = false;
        totalMacTime += System.currentTimeMillis() - start;
        lastMacEnd = System.currentTimeMillis();
    }

    @Override
    public void destroy() {
        for (SpdzStorage store : this.store) {
            store.shutdown();
        }
    }
}
