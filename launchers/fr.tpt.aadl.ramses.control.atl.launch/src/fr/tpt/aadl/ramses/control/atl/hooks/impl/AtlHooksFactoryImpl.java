/**
 * AADL-RAMSES
 * 
 * Copyright © 2012 TELECOM ParisTech and CNRS
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

package fr.tpt.aadl.ramses.control.atl.hooks.impl;

import fr.tpt.aadl.ramses.control.atl.hooks.* ;

import org.apache.log4j.Logger ;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class AtlHooksFactoryImpl extends EFactoryImpl implements AtlHooksFactory
{
  private static Logger _LOGGER = Logger.getLogger(AtlHooksFactoryImpl.class) ;
  
  /**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public static AtlHooksFactory init()
  {
		try {
			AtlHooksFactory theAtlHooksFactory = (AtlHooksFactory)EPackage.Registry.INSTANCE.getEFactory("http://fr.tpt.aadl.ramses.transformation.atl.launch"); 
			if (theAtlHooksFactory != null) {
				return theAtlHooksFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new AtlHooksFactoryImpl();
	}

  /**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public AtlHooksFactoryImpl()
  {
		super();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  @Override
  public EObject create(EClass eClass)
  {
		switch (eClass.getClassifierID()) {
			case AtlHooksPackage.HOOK_ACCESS: return createHookAccess();
			default:
			{
				String msg = "The class '" + eClass.getName() + "' is not a valid classifier";
				_LOGGER.fatal(msg) ;
				throw new IllegalArgumentException(msg);
			}
		}
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public HookAccess createHookAccess()
  {
		HookAccessImpl hookAccess = new HookAccessImpl();
		return hookAccess;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public AtlHooksPackage getAtlHooksPackage()
  {
		return (AtlHooksPackage)getEPackage();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
  @Deprecated
  public static AtlHooksPackage getPackage()
  {
		return AtlHooksPackage.eINSTANCE;
	}
} //AtlHooksFactoryImpl