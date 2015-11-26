/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework.configuration;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;

import dk.alexandra.fresco.framework.util.Pair;


public class ConfigurationGenerator {

    public OutputStream generate(int partyNumber, OutputStream out,
            List<Pair<String, Integer>> hosts)
    throws Exception {
        XMLConfiguration config = new XMLConfiguration();
        for (int inx = 0; inx < hosts.size(); inx++) {
            config.addProperty("parties.party(-1).hostname",
                    hosts.get(inx)
                    .getFirst());
            config.addProperty("parties.party.port", hosts
                    .get(inx)
                    .getSecond());
        }

        config.save(out, "UTF-8");
        return out;
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            usage();
        }

        if (args.length % 2 != 0) {
            System.out.println("Incorrect number of arguments: "
                    + Arrays.asList(args) + ".");
            System.out.println();
            usage();
        }

        List<Pair<String, Integer>> hosts = new LinkedList<Pair<String, Integer>>();
        int inx = 0;
        try {

            for (; inx < args.length; inx = inx + 2) {
                hosts.add(new Pair<String, Integer>(args[inx], new Integer(
                        args[inx + 1])));
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid argument for port: \"" + args[inx + 1]
                                                                      + "\".");
            System.exit(1);
        }

        inx = 1;
        try {
            for (; inx < hosts.size() + 1; inx++) {
                String filename = "party-" + inx + ".ini";
                OutputStream out = new FileOutputStream(filename);
                new ConfigurationGenerator().generate(inx, out, hosts);
                out.close();
                System.out.println("Created configuration file: " + filename
                        + ".");
            }
        } catch (Exception ex) {
            System.out.println("Could not write to file: party-" + inx
                    + ".ini.");
            System.exit(1);
        }

    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  java "
                + ConfigurationGenerator.class.getSimpleName() + " [arg]*");
        System.out.println("");
        System.out.println("  arg: [hostname] [port]");
        System.exit(1);
    }
}
