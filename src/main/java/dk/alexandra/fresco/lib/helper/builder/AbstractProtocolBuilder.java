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
package dk.alexandra.fresco.lib.helper.builder;

import java.util.Stack;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.lib.helper.AppendableProtocolProducer;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * 
 * Internally AbstractProtocolBuilder keeps an AppendableProtocolProducer (These are
 * the basic structural ProtocolProducers such as Sequential- and
 * ParallelProtocolProducers), here called the "current ProtocolProducer". Each
 * instruction to the builder results in a corresponding ProtocolProducer being
 * appended to the current ProtocolProducer.
 * 
 * When a new scope is declared a corresponding AppendableProtocolProducer is
 * constructed and appended to the current ProtocolProducer. The current
 * ProtocolProducer is then pushed on a stack and the newly created
 * AppendableProtocolProducer is made the new current ProtocolProducer. When the current
 * scope is ended the ProtocolBuilder returns to the previous scope by popping
 * the top of stack making it the new current ProtocolProducer.
 * 
 * 
 * @author psn
 * 
 */
public abstract class AbstractProtocolBuilder implements ProtocolBuilder {

	//A parent is only set if the method setParent is called. 
	//It forces this builder to use the internal stack of the parent.
	private AbstractProtocolBuilder parent;
	
	private AppendableProtocolProducer curpp = new SequentialProtocolProducer();
	private Stack<AppendableProtocolProducer> producerStack = 
			new Stack<AppendableProtocolProducer>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.alexandra.fresco.lib.ProtocolBuilder#beginParScope()
	 */
	@Override
	public void beginParScope() {
		if(parent != null){
			parent.beginParScope();
			return;
		}
		ParallelProtocolProducer ppp = new ParallelProtocolProducer();
		append(ppp);
		pushProducer(ppp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.alexandra.fresco.lib.ProtocolBuilder#beginSeqScope()
	 */
	@Override
	public void beginSeqScope() {
		if(parent != null){
			parent.beginSeqScope();
			return;
		}
		SequentialProtocolProducer spp = new SequentialProtocolProducer();
		append(spp);
		pushProducer(spp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.alexandra.fresco.lib.ProtocolBuilder#endCurScope()
	 */
	@Override
	public void endCurScope() {
		if(parent != null){
			parent.endCurScope();
			return;
		}
		if (producerStack.isEmpty()) {
			throw new MPCException("Cannot end root scope");
		}
		//curpp.merge();
		popProducer();
	}

	/**
	 * Appends a ProtocolProducer to the current ProtocolProducer.
	 * 
	 * @param pp
	 *            the ProtocolProducer to append
	 */
	protected void append(ProtocolProducer pp) {
		if(parent != null){
			parent.append(pp);
			return;
		}
		this.curpp.append(pp);
	}

	/**
	 * Pops the top of the stack and makes it the new current ProtocolProducer.
	 */
	protected void popProducer() {
		if(parent != null){
			parent.popProducer();
			return;
		}
		this.curpp = producerStack.pop();
	}

	/**
	 * Pushes the current ProtocolProducer on the stack and makes the given
	 * AppendableProtocolProducer the new current ProtocolProducer.
	 * 
	 * @param nextProducer
	 *            the AppendableProtocolProducer to be made the new current
	 *            ProtocolProducer
	 */
	protected void pushProducer(AppendableProtocolProducer nextProducer) {
		if(parent != null){
			parent.pushProducer(nextProducer);
			return;
		}
		producerStack.push(curpp);
		this.curpp = nextProducer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.alexandra.fresco.lib.ProtocolBuilder#getProtocol()
	 */
	@Override
	public ProtocolProducer getProtocol() {
		if(parent != null){
			return parent.getProtocol();
		}
		if (producerStack.isEmpty()) {
			ProtocolProducer res = curpp;
			reset();
			return res;
		}
		Reporter.warn("Builder did not close all scopes.");
		return producerStack.firstElement();
	}
	
	@Override
	public void reset() {
		if(parent != null){
			parent.reset();
			return;
		}
		curpp = new SequentialProtocolProducer();
		producerStack = new Stack<AppendableProtocolProducer>();
	}
	
	/**
	 * Sets the parent of this builder meaning that the internal stack of the builder will be the parents. 
	 * @param parent
	 */
	protected void setParentBuilder(AbstractProtocolBuilder parent){
		this.parent = parent;
	}
}
