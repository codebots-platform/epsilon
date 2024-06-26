/*******************************************************************************
 * Copyright (c) 2023 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Horacio Hoyos Rodriguez - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.ecore.delegates.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EValidator.SubstitutionLabelProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.ecore.delegates.DelegateLabelProvider;
import org.eclipse.epsilon.ecore.delegates.EvlDelegateContext;
import org.eclipse.epsilon.ecore.delegates.ExeedLabelProvider;
import org.eclipse.epsilon.ecore.delegates.execution.EvlConstraint;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.evl.execute.UnsatisfiedConstraint;

/**
 * A basic {@link EpsilonValidationDelegate} implementation that caches errors to avoid multiple exception
 * reporting.
 * 
 * @since 2.5
 */
public class EvlValidationDelegate implements EpsilonValidationDelegate {

	public EvlValidationDelegate(
		EvlDelegateContext delegateContext,
		DelegateLabelProvider labelProvider) {
		this.delegateContext = delegateContext;
		this.labelProvider = labelProvider;
	}

	@Override
	public boolean validate(
		EClass eClass,
		EObject eObject,
		Map<Object, Object> context,
		EOperation invariant,
		String expression) {
		return this.validate(eClass, eObject, context, invariant.getName(), expression);
	}

	@Override
	public boolean validate(
		EClass eClass, 
		EObject eObject, 
		Map<Object, Object> context, 
		String constraint,
		String expression) {
		Target target = new Target(constraint, eObject);
		if (this.errors.contains(target)) {
			// No need to throw the same exception multiple times.
			return false;
		}
		EvlConstraint program = this.programs.computeIfAbsent(
				constraint,
				c -> (EvlConstraint) this.delegateContext.parse(
						toEvlContext(expression, eObject.eClass().getName(), c)));
		addLabelProvider(context);
		try {
			Optional<UnsatisfiedConstraint> result = program.execute(
					eObject,
					this.models.computeIfAbsent(eObject.eResource(), r -> new InMemoryEmfModel(r)));
			return !result.isPresent();
		} catch (Throwable e) {
			// Cache the execution exception (most probably due to sytax errors
			this.errors.add(target);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean validate(
		EDataType eDataType,
		Object value,
		Map<Object, Object> context,
		String constraint,
		String expression) {
		return true;
	}
	
	@Override
	public void reset() {
		this.errors.clear();
		this.models.clear();
		this.programs.clear();		
	}
	
	private class Target {
		
		public Target(String constraint, Object eObject) {
			super();
			this.constraint = constraint;
			this.eObject = eObject;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(constraint, eObject);
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Target other = (Target) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Objects.equals(constraint, other.constraint) && Objects.equals(eObject, other.eObject);
		}

		private final String constraint;
		private final Object eObject;
		private EvlValidationDelegate getEnclosingInstance() {
			return EvlValidationDelegate.this;
		}
	}
	
	private final Map<Resource, InMemoryEmfModel> models = new HashMap<>();
	private final Map<String, EvlConstraint> programs = new HashMap<>();
	private final Set<Target> errors = new HashSet<>();
	private final EvlDelegateContext delegateContext;
	private final DelegateLabelProvider labelProvider;
	
	private String toEvlContext(String expression, String context, String constraint) {
		StringBuilder result = new StringBuilder();
		result.append("context ");
		result.append(context);
		result.append("{");
		result.append("constraint ");
		result.append(constraint);
		result.append("{");
		result.append("check: ");
		result.append(expression);
		result.append("}");
		result.append("}");
		return result.toString();
	}
	
	private void addLabelProvider(Map<Object, Object> context) {
		SubstitutionLabelProvider delegate = null;
		if (context.containsKey(SubstitutionLabelProvider.class)) {
			delegate = (SubstitutionLabelProvider) context.get(SubstitutionLabelProvider.class);
			if (delegate instanceof ExeedLabelProvider) {
				return;
			}
			context.put(SubstitutionLabelProvider.class, this.labelProvider.delegate(delegate));
		} else {
			context.put(SubstitutionLabelProvider.class, this.labelProvider);
		}
	}

}
