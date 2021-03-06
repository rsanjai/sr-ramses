/**
 * AADL-RAMSES
 * 
 * Copyright © 2014 TELECOM ParisTech and CNRS
 * 
 * TELECOM ParisTech/LTCI
 * 
 * Authors: see AUTHORS
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the Eclipse Public License as published by Eclipse,
 * either version 1.0 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * You should have received a copy of the Eclipse Public License
 * along with this program.  If not, see 
 * http://www.eclipse.org/org/documents/epl-v10.php
 */

package fr.tpt.aadl.ramses.analysis;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link fr.tpt.aadl.ramses.analysis.AnalysisSource#getIterationId <em>Iteration Id</em>}</li>
 *   <li>{@link fr.tpt.aadl.ramses.analysis.AnalysisSource#getScope <em>Scope</em>}</li>
 *   <li>{@link fr.tpt.aadl.ramses.analysis.AnalysisSource#getMethodName <em>Method Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see fr.tpt.aadl.ramses.analysis.AnalysisResultPackage#getAnalysisSource()
 * @model
 * @generated
 */
public interface AnalysisSource extends EObject {
	/**
	 * Returns the value of the '<em><b>Iteration Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Iteration Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Iteration Id</em>' attribute.
	 * @see #setIterationId(int)
	 * @see fr.tpt.aadl.ramses.analysis.AnalysisResultPackage#getAnalysisSource_IterationId()
	 * @model
	 * @generated
	 */
	int getIterationId();

	/**
	 * Sets the value of the '{@link fr.tpt.aadl.ramses.analysis.AnalysisSource#getIterationId <em>Iteration Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Iteration Id</em>' attribute.
	 * @see #getIterationId()
	 * @generated
	 */
	void setIterationId(int value);

	/**
	 * Returns the value of the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scope</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scope</em>' attribute.
	 * @see #setScope(String)
	 * @see fr.tpt.aadl.ramses.analysis.AnalysisResultPackage#getAnalysisSource_Scope()
	 * @model
	 * @generated
	 */
	String getScope();

	/**
	 * Sets the value of the '{@link fr.tpt.aadl.ramses.analysis.AnalysisSource#getScope <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scope</em>' attribute.
	 * @see #getScope()
	 * @generated
	 */
	void setScope(String value);

	/**
	 * Returns the value of the '<em><b>Method Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Method Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Method Name</em>' attribute.
	 * @see #setMethodName(String)
	 * @see fr.tpt.aadl.ramses.analysis.AnalysisResultPackage#getAnalysisSource_MethodName()
	 * @model
	 * @generated
	 */
	String getMethodName();

	/**
	 * Sets the value of the '{@link fr.tpt.aadl.ramses.analysis.AnalysisSource#getMethodName <em>Method Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Method Name</em>' attribute. Should be set with the plugin name.
	 * @see #getMethodName()
	 * @generated
	 */
	void setMethodName(String value);

} // AnalysisSource
