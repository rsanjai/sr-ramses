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

package fr.tpt.aadl.ramses.util.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.PropertyAssociation;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionInstance ;
import org.osate.aadl2.instance.FeatureCategory ;
import org.osate.aadl2.instance.FeatureInstance;
import org.osate.aadl2.instance.InstanceReferenceValue;
import org.osate.aadl2.instance.SystemInstance;

import fr.tpt.aadl.ramses.control.support.generator.TargetProperties ;

public class RoutingProperties implements TargetProperties {
  
  public Set<FeatureInstance> globalPort = new HashSet<FeatureInstance>();

	public Set<ComponentInstance> processes = new HashSet<ComponentInstance>();

	public Set<ComponentInstance> processors = new HashSet<ComponentInstance>();
	
	public Set<ComponentInstance> buses = new HashSet<ComponentInstance>();
	
	public Map<ComponentInstance, List<FeatureInstance>> portPerProcess = 
	                      new HashMap<ComponentInstance, List<FeatureInstance>>();

	public Map<FeatureInstance, ComponentInstance> processorPort = 
	                            new HashMap<FeatureInstance, ComponentInstance>();

	public Map<ComponentInstance, Set<ComponentInstance>> processPerProcessor =
	                    new HashMap<ComponentInstance, Set<ComponentInstance>>();
	
	public void setRoutingProperties(SystemInstance system)
	{
		for(ComponentInstance subComponent: system.getComponentInstances())
		{
			processComponentInstance(subComponent);
		}
	}

	private void processComponentInstance(ComponentInstance component)
	{

		if(component.getCategory().equals(ComponentCategory.PROCESS))
		{
			processes.add(component);
			getPartitionProperties(component);
		}
		else if(component.getCategory().equals(ComponentCategory.PROCESSOR))
			processors.add(component);
		else if(component.getCategory().equals(ComponentCategory.BUS))
			buses.add(component);
		else
		{
			for(ComponentInstance subComponent: component.getComponentInstances())
			{
				processComponentInstance(subComponent);
			}
		}
	}

	private void getPartitionProperties(ComponentInstance process)
	{
		List<FeatureInstance> processPorts = new ArrayList<FeatureInstance>();
		portPerProcess.put(process, processPorts);
		for(ComponentInstance subComponent: process.getComponentInstances())
		{
			if(subComponent.getCategory().equals(ComponentCategory.THREAD))
			{
				for(FeatureInstance f:subComponent.getFeatureInstances())
				{
					if(RoutingProperties.needsRoutage(f))
					{
						processPorts.add(f);
						globalPort.add(f);
						processorPort.put(f, getProcessorBinding(process));
					}
				}
			}
		}
	}

	private ComponentInstance getProcessorBinding(ComponentInstance process)
	{
		PropertyAssociation deploymentPropertyAssociation=null;
		for(PropertyAssociation pa : process.getOwnedPropertyAssociations())
		{
			if(pa.getProperty().getName() == null)
			{
				continue ;
			}

			if(pa.getProperty().getName().equalsIgnoreCase("Actual_Processor_Binding"))
			{
				deploymentPropertyAssociation=pa;
				break;
			}
		}

		for(ModalPropertyValue aModalPropertyValue : deploymentPropertyAssociation
				.getOwnedValues())
		{
			if(aModalPropertyValue.getOwnedValue() instanceof ListValue)
			{
				ListValue list = (ListValue) aModalPropertyValue.getOwnedValue() ;

				for(PropertyExpression pe : list.getOwnedListElements())
				{
					if(pe instanceof InstanceReferenceValue)
					{
						InstanceReferenceValue rv = (InstanceReferenceValue) pe ;
						ComponentInstance ci = (ComponentInstance) rv.getReferencedInstanceObject();
						ComponentInstance processor = null ;
						if(ci.getCategory().equals(ComponentCategory.VIRTUAL_PROCESSOR))
							processor = getParentProcessor(ci);
						else if(ci.getCategory().equals(ComponentCategory.PROCESSOR))
							processor = ci;


						if(processPerProcessor.get(processor)!=null)
							processPerProcessor.get(processor).add(process);
						else
						{
							Set<ComponentInstance> processes  =new HashSet<ComponentInstance>();
							processes.add(process);
							processPerProcessor.put(processor, processes);
						}

						return processor ;
					}
				}
			}
		}
		return null;
	}

	private ComponentInstance getParentProcessor(ComponentInstance ci) {
		if(ci.getContainingComponentInstance().getCategory().equals(ComponentCategory.PROCESSOR))
			return ci.getContainingComponentInstance();
		else
			return getParentProcessor(ci);
	}

	
	public static boolean needsRoutage(FeatureInstance fi)
  {
    boolean result = false;
    if(fi.getCategory().equals(FeatureCategory.DATA_PORT)
        || fi.getCategory().equals(FeatureCategory.EVENT_PORT)
        || fi.getCategory().equals(FeatureCategory.EVENT_DATA_PORT))
    {
      List<FeatureInstance> dstList = getFeatureDestinations(fi);
      if(dstList!=null)
      {
        for(FeatureInstance dst : dstList)
          if(false == areCollocated(fi,dst))
            return true;
      }
      List<FeatureInstance> srcList = getFeatureSources(fi);
      if(dstList!=null)
      {
        for(FeatureInstance src : srcList)
          if(false == areCollocated(fi,src))
            return true;
      }
    }
    return result;
  }
  
  public static boolean areCollocated(FeatureInstance src, FeatureInstance dst)
  {
    ComponentInstance srcProcess=null, dstProcess=null;
    if(src.getContainingComponentInstance().getCategory()
        .equals(ComponentCategory.THREAD))
      srcProcess = src.getContainingComponentInstance()
        .getContainingComponentInstance();
    if(dst.getContainingComponentInstance().getCategory()
        .equals(ComponentCategory.THREAD))
      dstProcess = dst.getContainingComponentInstance()
        .getContainingComponentInstance();
    if(srcProcess==null || dstProcess==null)
      return false;
    return srcProcess.equals(dstProcess);
  }
  
  public static List<FeatureInstance> getFeatureSources(FeatureInstance port)
  {
    // The parameter "port" must be port of a thread component
    if(!port.getContainingComponentInstance().getCategory()
        .equals(ComponentCategory.THREAD))
      return null;

    List<FeatureInstance> result = new ArrayList<FeatureInstance>();
    for(ConnectionInstance ci: port.getDstConnectionInstances())
    {
      FeatureInstance fi = (FeatureInstance)ci.getSource();
      if(fi.getContainingComponentInstance().getCategory()
          .equals(ComponentCategory.THREAD))
      {
        result.add(fi);
      }
    }
    return result;
  }
  
  public static List<FeatureInstance> getFeatureDestinations(FeatureInstance port)
  {
    // The parameter "port" must be port of a thread component
    if(!port.getContainingComponentInstance().getCategory()
        .equals(ComponentCategory.THREAD))
      return null;

    List<FeatureInstance> result = new ArrayList<FeatureInstance>();
    for(ConnectionInstance ci: port.getSrcConnectionInstances())
    {
      FeatureInstance fi = (FeatureInstance)ci.getDestination();
      if(fi.getContainingComponentInstance().getCategory()
          .equals(ComponentCategory.THREAD))
      {
        result.add(fi);
      }
    }
    return result;
  }

}