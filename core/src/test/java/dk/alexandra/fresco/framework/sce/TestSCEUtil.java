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
package dk.alexandra.fresco.framework.sce;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.sce.util.Util;

public class TestSCEUtil{

  @Test(expected = InstantiationException.class) 
  public void testConstructor() throws InstantiationException{
    Util util = new Util();
  }
  
  @Test
  public void testGetInputStreamUtilClass() throws IOException {
    InputStream is = Util.getInputStream("");
    Assert.assertThat(is.available(), Is.is("Util.class".length()+1));
  }

  @Test
  public void testGetInputStreamNonExistingResource() throws IOException {
    try{
      InputStream is = Util.getInputStream("resources/circuits/md5.txt");
    }catch(FileNotFoundException e) {
      Assert.assertThat(e.getMessage(), Is.is("Could not locate the resource resources/circuits/md5.txt"));  
    }
  }
  
  @Test
  public void testGetInputStreamExistingResource(){
    try{
      InputStream is = Util.getInputStream("src/test/resources/circuits/md5.txt");
      try {
        Assert.assertThat(is.available(), Is.is(1781599)); // Magicnumber relates to the file above
      } catch (IOException e) {
        Assert.fail();
      }
    }catch(FileNotFoundException e) {
      Assert.fail();
    }
  }
  
  
}
