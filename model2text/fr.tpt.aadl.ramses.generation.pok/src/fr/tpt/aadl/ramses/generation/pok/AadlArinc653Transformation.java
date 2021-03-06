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

package fr.tpt.aadl.ramses.generation.pok ;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger ;

import fr.tpt.aadl.ramses.control.atl.AadlToTargetSpecificAadl ;
import fr.tpt.aadl.ramses.control.support.instantiation.AadlModelInstantiatior ;
import fr.tpt.aadl.ramses.control.support.instantiation.PredefinedAadlModelManager ;

public class AadlArinc653Transformation extends
                                       AadlToTargetSpecificAadl
{
  private static Logger _LOGGER = Logger.getLogger(AadlArinc653Transformation.class);
  
  @Override
  public void setParameters(Map<Enum<?>, Object> parameters)
  {
    String msg = "setParameters not supported" ;
    _LOGGER.fatal(msg);
    throw new UnsupportedOperationException() ;
  }

  public AadlArinc653Transformation(AadlModelInstantiatior modelInstantiator,
                                    PredefinedAadlModelManager predefinedAadlModels,
                                    String module)
  {
    super(modelInstantiator, predefinedAadlModels) ;
    _atlFileNames = new ArrayList<String>();
    _atlFileNames.add("ACG/targets/shared/UninstanciateOverride");
    _atlFileNames.add("ACG/targets/shared/SubprogramCallsCommonRefinementSteps");
    _atlFileNames.add("ACG/targets/shared/PortsCommonRefinementSteps");
    _atlFileNames.add("ACG/targets/shared/DispatchCommonRefinementSteps");
    _atlFileNames.add("ACG/targets/shared/BehaviorAnnexCommonRefinementSteps");
    _atlFileNames.add("ACG/targets/arinc653/ExpandThreadsPorts");
    _atlFileNames.add("ACG/targets/arinc653/BlackboardCommunications");
    _atlFileNames.add("ACG/targets/arinc653/BufferCommunications");
    _atlFileNames.add("ACG/targets/arinc653/EventsCommunications");
    _atlFileNames.add("ACG/targets/arinc653/QueuingCommunications");
    _atlFileNames.add("ACG/targets/arinc653/SamplingCommunications");
    _atlFileNames.add("ACG/targets/arinc653/ExpandThreadsDispatchProtocol");
    _atlFileNames.add("ACG/targets/arinc653/HealthMonitoring");
    _atlFileNames.add("ACG/PeriodicDelayedCommunication/SharedRules");
    _atlFileNames.add(module);
  }
}