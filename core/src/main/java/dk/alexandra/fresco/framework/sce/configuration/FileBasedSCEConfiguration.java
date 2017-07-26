/*
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
package dk.alexandra.fresco.framework.sce.configuration;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.framework.sce.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileBasedSCEConfiguration implements SCEConfiguration {

  private String propertiesLocation = "properties/sce.properties";
  private boolean loaded = false;

  private String protocolSuite;
  private Map<Integer, Party> parties;
  private int myId;

  private FileBasedSCEConfiguration(String propertiesLocation) {
    this.propertiesLocation = propertiesLocation;
  }

  private synchronized void loadProperties() {
    InputStream is;
    try {
      is = Util.getInputStream(propertiesLocation);
      Properties prop = new Properties();
      prop.load(is);

      //load myId
      this.myId = Integer.parseInt(prop.getProperty("myId", "-1"));
      if (this.myId == -1) {
        throw new ConfigurationException(
            "The property 'myId' must be set and be within the range 1 to the total amount of parties");
      }

      //load parties
      Map<Integer, Party> parties = new HashMap<>();
      int i = 1;
      while (true) {
        String party = prop.getProperty("party" + i);
        if (party == null) {
          // no more parties
          break;
        }
        String[] ipAndPort = party.split(",");
        if (ipAndPort.length != 2 && ipAndPort.length != 3) {
          throw new IllegalArgumentException(
              "a party configuration should contain something similar to 'party1=192.168.0.1,8000' or possibly 'party1=192.168.0.1,8000,w+1qn2ooNMCN7am9YmYQFQ==' if encrypted and authenticated channels are wanted");
        }
        String secretSharedKey = null;
        if (ipAndPort.length == 3) {
          secretSharedKey = ipAndPort[2];
        }
        Party p = new Party(i, ipAndPort[0], Integer.parseInt(ipAndPort[1]), secretSharedKey);
        parties.put(i, p);
        i++;
      }
      this.parties = parties;

      //load runtime
      String protocolSuite = prop.getProperty("protocolSuite");
      if (protocolSuite == null) {
        throw new ConfigurationException("The property 'protocolSuite' must be set");
      }
      this.protocolSuite = protocolSuite;

      // load evaluator
      String evaluator = prop.getProperty("evaluator");
      if (evaluator == null) {
        throw new ConfigurationException(
            "The property 'evaluator' must be set to one of these values: " + Arrays
                .toString(EvaluationStrategy.values()));
      }


      String storage = prop.getProperty("storage");
      if (storage == null) {
        throw new ConfigurationException(
            "The property 'storage' must be set to one of these values: " + Arrays
                .toString(StorageStrategy.values()));
      }

      loaded = true;
    } catch (IOException e) {
      throw new MPCException(
          "Could not locate the SecureComputationEngine properties file. ", e);
    }
  }

  public static FileBasedSCEConfiguration getInstance(String propertiesDir) {
    String propertiesPath = propertiesDir + "/" + "sce.properties";
    return new FileBasedSCEConfiguration(propertiesPath);
  }

  @Override
  public int getMyId() {
    if (!loaded) {
      loadProperties();
    }
    return this.myId;
  }

  @Override
  public Map<Integer, Party> getParties() {
    if (!loaded) {
      loadProperties();
    }
    return this.parties;
  }

  @Override
  public String toString() {
    return "FileBasedSCEConfiguration ["
        + "propertiesLocation=" + propertiesLocation
        + ", loaded=" + loaded
        + ", protocolSuite=" + protocolSuite
        + ", parties=" + parties
        + ", myId=" + myId + "]";
  }
}
