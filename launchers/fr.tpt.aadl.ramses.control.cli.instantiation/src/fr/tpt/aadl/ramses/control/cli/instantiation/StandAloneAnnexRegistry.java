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

package fr.tpt.aadl.ramses.control.cli.instantiation;

import java.util.HashMap ;

import org.eclipse.core.runtime.IConfigurationElement ;
import org.eclipse.core.runtime.Platform ;
import org.osate.annexsupport.AnnexProxy ;
import org.osate.annexsupport.AnnexRegistry ;

public class StandAloneAnnexRegistry extends AnnexRegistry
{
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  protected void initialize(String extensionId) {
    if(Platform.isRunning())
      super.initialize(extensionId);
    else
    {
      extensions = new HashMap();
      extensions.put("behavior_specification", null);
    }
  }
  
  @Override
  protected AnnexProxy createProxy(IConfigurationElement configElem)
  {
    return new StandAloneAnnexProxy(configElem);
  }
}