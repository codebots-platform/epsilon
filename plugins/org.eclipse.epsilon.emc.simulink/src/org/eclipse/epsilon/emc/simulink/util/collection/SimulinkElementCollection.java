/*********************************************************************
* Copyright (c) 2008 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.emc.simulink.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.epsilon.emc.simulink.model.SimulinkModel;
import org.eclipse.epsilon.emc.simulink.model.element.ISimulinkElement;
import org.eclipse.epsilon.emc.simulink.model.element.ISimulinkModelElement;
import org.eclipse.epsilon.emc.simulink.util.manager.SimulinkManager;

public class SimulinkElementCollection extends AbstractSimulinkCollection<ISimulinkElement, Double, SimulinkManager> {

	public SimulinkElementCollection(SimulinkModel model) {
		super(null, new SimulinkManager(model));	
	}
	
	public SimulinkElementCollection(Object primitive, SimulinkModel model) {
		super(primitive, new SimulinkManager(model));
	}
	
	@Override
	public Object[] toArray() {
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}
	
	@Override
	public ListIterator<ISimulinkModelElement> listIterator() {
		return new SimulinkElementListIterator();
	}

	@Override
	public ListIterator<ISimulinkModelElement> listIterator(int index) {
		return new SimulinkElementListIterator(index);
	}

	@Override
	public List<ISimulinkModelElement> subList(int fromIndex, int toIndex) {
		return new SimulinkElementCollection(getPrimitive().subList(fromIndex, toIndex), getManager().getModel());
	}

	@Override
	protected Iterator<ISimulinkModelElement> getInternalIterator() {
		return new SimulinkElementIterator();
	}

	@Override
	protected boolean isInstanceOf(Object object) {
		return object instanceof ISimulinkModelElement;
	}

	@Override
	protected boolean isInstanceOfPrimitive(Object object) {
		return object instanceof Double;
	}

	@Override
	protected boolean isInstanceOfPrimitiveArray(Object object) {
		return object instanceof Double[];
	}
	
	protected class SimulinkElementIterator extends AbstractBlockIterator<ISimulinkElement, Double, SimulinkManager>{
		
		SimulinkElementIterator(){
			super(getPrimitive(), getManager());
		}
		
	}
	
	protected class SimulinkElementListIterator extends AbstractListIterator<ISimulinkElement, Double, SimulinkManager> {
		
		SimulinkElementListIterator(){
			super(getPrimitive(), getManager());
		}
		
		SimulinkElementListIterator(int index){
			super(index, getPrimitive(), getManager());
		}
		
	}

	
}