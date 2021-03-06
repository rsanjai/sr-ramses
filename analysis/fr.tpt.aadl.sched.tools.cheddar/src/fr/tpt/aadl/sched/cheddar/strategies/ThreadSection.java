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
 
 package fr.tpt.aadl.sched.cheddar.strategies ;

import java.util.HashMap ;

import org.osate.aadl2.ComponentCategory ;
import org.osate.aadl2.NumberValue;
import org.osate.aadl2.instance.ComponentInstance ;
import org.osate.utils.PropertyUtils ;

import fr.tpt.aadl.ramses.util.properties.AadlUtil ;
import fr.tpt.aadl.ramses.util.properties.AadlUtil.DispatchProtocol ;
import fr.tpt.aadl.sched.cheddar.CheddarContext ;
import fr.tpt.aadl.sched.cheddar.model.CheddarAddressSpace ;
import fr.tpt.aadl.sched.cheddar.model.CheddarTask ;

public class ThreadSection extends CheddarModelConversionPart<CheddarTask>
{
  protected HashMap<String, CheddarTaskType> taskTypeToXML ;

  public enum CheddarTaskType
  {
    PERIODIC_TYPE, APERIODIC_TYPE, SPORADIC_TYPE, PARAMETRIC_TYPE
  } ;

  {
    taskTypeToXML = new HashMap<String, CheddarTaskType>() ;
    taskTypeToXML.put("Periodic", CheddarTaskType.PERIODIC_TYPE) ;
    taskTypeToXML.put("Aperiodic", CheddarTaskType.APERIODIC_TYPE) ;
    taskTypeToXML.put("Sporadic", CheddarTaskType.SPORADIC_TYPE) ;
    taskTypeToXML.put("Unknown", CheddarTaskType.PARAMETRIC_TYPE) ;
  }

  /**
   * Blocking time : maximum wait on a resource
   *  not required but can be specified to refine schedulability analysis
   *
   *
   */

  protected void setTypeConversion(String AADLActivationType,
                                   CheddarTaskType cheddarType)
  {
    taskTypeToXML.put("Periodic", cheddarType) ;
  }

  public ThreadSection(
                       CheddarContext sections)
  {
    super(ComponentCategory.THREAD, sections) ;
  }

  @Override
  protected CheddarTask addInstanceSubNodes(ComponentInstance aadlInstance)
  {
    CheddarTask t = new CheddarTask(aadlInstance) ;
    ComponentInstance process = aadlInstance.getContainingComponentInstance() ;
    CheddarAddressSpace cheddarProcess =
          (CheddarAddressSpace) getMappingOf(process) ;
    DispatchProtocol dispatchE = AadlUtil.getInfoTaskDispatch(aadlInstance) ;
    CheddarTaskType dispatch = taskTypeToXML.get(dispatchE.name()) ;
    double capacity = getInfoTaskCapacity(aadlInstance) ;
    long deadline = AadlUtil.getInfoTaskDeadline(aadlInstance) ;
    t.setName(process.getName() + "." + aadlInstance.getName()) ;
    t.setOwner(cheddarProcess) ;
    t.setCapacity((float) capacity) ;
    t.setStart_time(getInfoTaskStartTime(aadlInstance)) ;
    t.setQueueing_policy(getInfoTaskPolicy(aadlInstance, "SCHED_FIFO")) ;
    t.setDeadline(deadline) ;
    t.setBlocking_time(getInfoTaskBlockingTime(aadlInstance)) ;
    // t.setPriority(AadlUtil.getInfoTaskPriority(aadlInstance)) ;
    t.setText_memory_size(AadlUtil.getInfoTaskMemorySize(aadlInstance).intValue()) ;
    t.setStack_memory_size(AadlUtil.getInfoTaskStackSize(aadlInstance).intValue()) ;
    t.setPeriod(AadlUtil.getInfoTaskPeriod(aadlInstance)) ;
    t.setJitter(AadlUtil.getInfoTaskJitter(aadlInstance)) ;
    t.setActivationRule(getInfoTaskActivationRule(aadlInstance)) ;
    t.setType(dispatch) ;
    return t ;
  }

  protected double getInfoTaskCapacity(ComponentInstance task)
  {
    NumberValue nv = PropertyUtils.getMaxRangeValue(task, "Compute_Execution_Time");
    if(nv != null)
    {
      return  nv.getScaledValue(AadlUtil.getPrecision(task));
    }
    else
    {
      //return computeTaskCapacity(task);
      return 0d ;
    }
  }

  /*
  private int computeTaskCapacity(ComponentInstance task)
  {
    ExecutionGraphRegistry registry = sections.getExecutionRegistry();
    ExecutionGraph au = registry.getExecutionForTask(task);
  
    return au.getWCET();
  }*/

  protected int getInfoTaskStartTime(ComponentInstance task)
  {
    return 0 ;
  }

  protected int getInfoTaskBlockingTime(ComponentInstance task)
  {
    return 0 ;
  }

  protected String getInfoTaskPolicy(ComponentInstance task,
                                     String defaultValue)
  {
    return defaultValue ;
  }

  protected String getInfoTaskActivationRule(ComponentInstance task)
  {
    return null ;
  }
}