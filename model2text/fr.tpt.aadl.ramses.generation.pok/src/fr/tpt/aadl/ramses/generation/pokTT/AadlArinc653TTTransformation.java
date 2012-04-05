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

package fr.tpt.aadl.ramses.generation.pokTT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.SystemImplementation;
import org.osate.aadl2.instance.SystemInstance;

import fr.tpt.aadl.ramses.control.support.generator.AadlToTargetSpecificAadl;
import fr.tpt.aadl.ramses.control.support.generator.GenerationException;
import fr.tpt.aadl.ramses.instantiation.StandAloneInstantiator;
import fr.tpt.aadl.ramses.transformation.atl.AtlTransfoLauncher;

public class AadlArinc653TTTransformation implements AadlToTargetSpecificAadl
{
	public static final String DEFAULT_ATL_FILE_PATH = "../../model2model/fr.tpt.aadl.ramses.transformation.atl/";

	public static final String[] ATL_FILE_NAMES_STEP1 = new String[] {
			"targets/arinc653_TT/CreateSchedulerEntities.asm",
			"targets/arinc653_TT/ExpandDelayedConnections.asm"};
			//"targets/arinc653_TT/ExpandArincConnections.asm" };
			
	//public static final String[] ATL_FILE_NAMES_STEP1 = new String[]{};

	public static final String[] ATL_FILE_NAMES_STEP2 = new String[] {
			"targets/arinc653/ExpandThreadsPorts.asm",
			"ExpandSubprogramCalls.asm",
			"targets/arinc653/ExpandThreadsDispatchProtocol.asm",
			"CreateThreadsBehavior.asm" };

	@Override
	public Resource transform(Resource inputResource, File resourceFilePath,
			Map<String, Resource> standardPropertySets, File generatedFilePath)
			throws GenerationException
	{
		try
		{
			if (resourceFilePath == null)
			{
				resourceFilePath = new File(DEFAULT_ATL_FILE_PATH);
			}
			
			
			Resource r1 = step1(inputResource, resourceFilePath, standardPropertySets, generatedFilePath);
			
			SystemInstance s = instanciate(r1);
			System.out.println("Instanciation: " + s.eResource().getURI().path());

			Resource r2 = step2(s.eResource(), resourceFilePath, standardPropertySets, generatedFilePath);
			
			return r2;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new GenerationException(e.getMessage());
		}
	}
	
	private Resource step1(Resource r, File resourceFilePath, Map<String, Resource> standardPropertySets,
			File generatedFilePath) throws Exception
	{
		System.out.println("********************************************");
		System.out.println("Expansion n°1: Delayed connections\n(resource: "
				+ r.getURI().path() + ")");
		Resource r1 = transform(r, resourceFilePath,
				standardPropertySets, generatedFilePath,
				ATL_FILE_NAMES_STEP1);
		
		return r1;
	}
	
	private Resource step2(Resource r1, 
			File resourceFilePath, Map<String, Resource> standardPropertySets,
			File generatedFilePath) throws Exception
	{
		System.out.println("********************************************");
		
		System.out.println("Expansion n°2: Threads behavior\n(resource: "
				+ r1.getURI().path() + ")");

		Resource r2 = transform(r1, resourceFilePath, standardPropertySets,
				generatedFilePath, ATL_FILE_NAMES_STEP2);
		
		System.out.println("**************   D O N E   *****************");

		return r2;
	}
	
	private SystemInstance instanciate (Resource r)
	{
		StandAloneInstantiator instantiator = StandAloneInstantiator
				.getInstantiator();
		ArrayList<Resource> res = new ArrayList<Resource>();
		res.add(r);

		SystemInstance s = instantiator.instantiate(res, findSystemName(r));
		return s;
	}
	
	private void serialize(SystemInstance s)
	{
		StandAloneInstantiator instantiator = StandAloneInstantiator
				.getInstantiator();
		
		String aadlGeneratedFileName = s.eResource().getURI()
				.toFileString()
				.replaceFirst("extended_node_impl_Instance.aaxl2", "extended_inst.aadl2");

		instantiator.serialize(s.eResource(), aadlGeneratedFileName);
	}

	private Resource transform(Resource inputResource, File resourceFilePath,
			Map<String, Resource> standardPropertySets, File generatedFilePath,
			final String[] modules) throws Exception
	{
		AtlTransfoLauncher atlTransfo = new AtlTransfoLauncher();
		atlTransfo.setResourcesDirectory(resourceFilePath);

		ArrayList<File> atlFiles = new ArrayList<File>(modules.length);

		for (String fileName : modules)
		{
			atlFiles.add(new File(resourceFilePath + "/" + fileName));
		}

		String aaxlGeneratedFileName = inputResource.getURI().toFileString()
				.replaceFirst(".aaxl2", "_extended.aaxl2");

		Resource expandedResult = atlTransfo.doGeneration(inputResource,
				standardPropertySets, atlFiles, aaxlGeneratedFileName);

		String aadlGeneratedFileName = inputResource.getURI().toFileString();
		aadlGeneratedFileName = aadlGeneratedFileName.replaceFirst(".aaxl2",
				"_extended.aadl2");

		StandAloneInstantiator instantiator = StandAloneInstantiator
				.getInstantiator();
		instantiator.serialize(expandedResult, aadlGeneratedFileName);

		return expandedResult;
	}

	public String findSystemName(Resource r)
	{
		AadlPackage aadlPackage = (AadlPackage) r.getContents().get(0);

		for (NamedElement member : aadlPackage.getOwnedPublicSection()
				.getOwnedMembers())
		{
			if (member instanceof SystemImplementation)
			{
				return member.getName();
			}
		}
		return null;
	}

	@Override
	public void setParameters(Map<Enum<?>, Object> parameters)
	{
		throw new UnsupportedOperationException();
	}
}