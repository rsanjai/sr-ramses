/**
 * AADL-RAMSES
 * 
 * Copyright Â© 2012 TELECOM ParisTech and CNRS
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

/*
 * author: Etienne Borde
 *
 */

package fr.tpt.aadl.ramses.control.atl ;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.m2m.atl.emftvm.ExecEnv;

import fr.tpt.aadl.ramses.control.support.instantiation.AadlModelInstantiatior;
import fr.tpt.aadl.ramses.control.support.instantiation.PredefinedAadlModelManager;


public class Aadl2ConstraintValidationEMFTVMLauncher extends AadlModelValidator
{
	
	private static Logger _LOGGER = Logger.getLogger(Aadl2ConstraintValidationEMFTVMLauncher.class) ;
	
	public Aadl2ConstraintValidationEMFTVMLauncher(AadlModelInstantiatior modelInstantiator,
			PredefinedAadlModelManager predefinedResourcesManager)
	{
		super(modelInstantiator, predefinedResourcesManager);
	}

	@Override
	protected Resource initTransformationOutput(Resource inputResource,
			                                        String outputDirPathName,
			                                        String resourceSuffix)
	{
		
		String aadlFileName = inputResource.getURI().path();
		if(aadlFileName.startsWith("file:"))
			aadlFileName = aadlFileName.substring("file:".length());
		else if (aadlFileName.startsWith("platform:/resource"))
			aadlFileName = aadlFileName.substring("platform:/resource".length());
		aadlFileName = aadlFileName.replaceFirst(
				".aaxl2", resourceSuffix+".xmi");
		URI uri = URI.createURI(aadlFileName);
		Resource outputResource = rs.getResource(uri, false);
		if(outputResource==null)
		  outputResource = rs.createResource(uri);
		else
		{
      try
      {
        outputResource.delete(null) ;
      }
      catch(IOException e)
      {
        String errMsg = "cannot delete the last output resource" ;
        _LOGGER.fatal(errMsg, e);
        throw new RuntimeException(errMsg, e);
      }
      outputResource = rs.createResource(uri) ;
		}
		
		return outputResource;
	}
	
}