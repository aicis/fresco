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
package dk.alexandra.fresco.framework.sce.resources.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class TestThreads{

  private ThreadPoolImpl threadPool;
  
  @Before
  public void setup() {
    threadPool = new ThreadPoolImpl(5, 5);
  }
  
  @Test 
  public void testGetters(){
    Assert.assertThat(threadPool.getVMThreadCount(), Is.is(5));
  }
  
  
  
  @Test
  public void testSubmitVMTask(){
    Task task = new Task(0);
    threadPool.submitVMTask(task);

    try {
      Thread.sleep(200);
    } catch (InterruptedException ignored) {
    }
    Assert.assertTrue(task.getStatus());
  }
/* TODO
  @Test
  public void testSubmitTask(){
    Task task = new Task(0);
    threadPool.submitTask(task);

    try {
      Thread.sleep(200);
    } catch (InterruptedException ignored) {
    }
    Assert.assertTrue(task.getStatus());
  }  
  
  @Test
  public void testShutdownTaskPool(){
    Task task = new Task(2000);
    threadPool.submitTask(task);
    Task vmTask = new Task(2000);
    threadPool.submitVMTask(vmTask);
    
    
    threadPool.shutdown();
    Assert.assertFalse(task.getStatus());
    Assert.assertFalse(vmTask.getStatus());
  }  
*/
  
  @Test
  public void testShutdownVMPool(){
    
    Task task = new Task(2000);
    threadPool.submitVMTask(task);
    threadPool.shutdownVMPool();
    Assert.assertFalse(task.getStatus());
  }
  /* TODO
  @Test
  public void testTaskPoolList() throws InterruptedException{
    List<Task> tasks = new ArrayList<Task>();
    for (int i = 0; i< 3; i++) {
      tasks.add(new Task(0));
    }
    threadPool.submitTasks((Collection<? extends Callable<Task>>) tasks);
    
    try{
      Thread.sleep(500);
    } catch(Exception ignored) {
      
    }
    for(Task task: tasks) {
      Assert.assertTrue(task.getStatus());  
    }
  }  

  @Test
  public void testTaskVMPoolList() throws InterruptedException{
    List<Task> tasks = new ArrayList<Task>();
    for (int i = 0; i< 3; i++) {
      tasks.add(new Task(0));
    }
    threadPool.submitVMTasks((Collection<? extends Callable<Task>>) tasks);
    
    try{
      Thread.sleep(500);
    } catch(Exception ignored) {
      
    }
    for(Task task: tasks) {
      Assert.assertTrue(task.getStatus());  
    }
  } 
  */
  private class Task implements Callable<Object> {
    
    private boolean called = false;
    private long runtime;
    public Task(long runtime) {
      this.runtime = runtime;
    }
    
    @Override
    public Object call(){
      try {
        Thread.sleep(runtime);
      } catch (InterruptedException e) {
      }
      called = true;
      return null;
    }
    
    public boolean getStatus() {
      return called;
    }
  }
  
  
}
