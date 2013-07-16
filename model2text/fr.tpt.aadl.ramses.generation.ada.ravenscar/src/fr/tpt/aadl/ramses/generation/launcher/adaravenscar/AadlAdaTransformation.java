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



package fr.tpt.aadl.ramses.generation.launcher.adaravenscar ;

import java.util.Map;

import fr.tpt.aadl.ramses.transformation.atl.AadlToTargetSpecificAadl;

public class AadlAdaTransformation extends
                                       AadlToTargetSpecificAadl
{
	  @Override
	  public void setParameters(Map<Enum<?>, Object> parameters)
	  {
	    throw new UnsupportedOperationException() ;
	  }


	  public AadlAdaTransformation()
	  {
	  	ATL_FILE_NAMES = new String[]
	        {
	  		 "ACG/targets/shared/UninstanciateOverride",
	  		 "ACG/targets/shared/SubprogramCallsCommonRefinementSteps",
	  		 "ACG/targets/shared/PortsCommonRefinementSteps",
	  		 "ACG/targets/shared/DispatchCommonRefinementSteps",
	  		 "ACG/targets/shared/BehaviorAnnexCommonRefinementSteps",
	  		 "ACG/targets/ravenscar/ExpandThreadsPorts",
		  	 "ACG/targets/ravenscar/RavenscarCommunications",
	  		 "ACG/targets/ravenscar/ExpandThreadsDispatchProtocol",
		  	 "ACG/PeriodicDelayedCommunication/SharedRules"
		  	 
		  	 /*
	  			"ACG/targets/shared/UninstanciateOverride",
		  		 "ACG/targets/shared/SubprogramCallsCommonRefinementSteps",
		  		 "ACG/targets/shared/PortsCommonRefinementSteps",
		  		 "ACG/targets/shared/DispatchCommonRefinementSteps",
		  		 "ACG/targets/shared/BehaviorAnnexCommonRefinementSteps",
		  		 "ACG/targets/arinc653/ExpandThreadsPorts",

			     "ACG/targets/arinc653/BlackboardCommunications",
			  	 "ACG/targets/arinc653/BufferCommunications",
			  	 "ACG/targets/arinc653/EventsCommunications",
			  	 "ACG/targets/arinc653/QueuingCommunications",
			  	 "ACG/targets/arinc653/SamplingCommunications",
			  	 "ACG/targets/arinc653/ExpandThreadsDispatchProtocol",
			  	 "ACG/PeriodicDelayedCommunication/SharedRules"
    */
	        };
	  }
	  
}
