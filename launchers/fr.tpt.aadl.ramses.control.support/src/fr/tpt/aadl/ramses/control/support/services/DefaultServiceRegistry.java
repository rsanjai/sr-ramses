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

package fr.tpt.aadl.ramses.control.support.services ;

import java.util.HashSet ;
import java.util.Set ;

import org.osate.annexsupport.AnnexParser ;
import org.osate.annexsupport.AnnexResolver ;
import org.osate.annexsupport.AnnexUnparser ;

import fr.tpt.aadl.ramses.control.support.analysis.Analyzer ;
import fr.tpt.aadl.ramses.control.support.generator.Generator ;
import fr.tpt.aadl.ramses.control.support.instantiation.AadlModelInstantiatior ;
import fr.tpt.aadl.ramses.control.support.instantiation.PredefinedAadlModelManager ;

public class DefaultServiceRegistry extends AbstractServiceRegistry implements ServiceRegistry
{
  @Override
  public AnnexParser getParser(String name)
  {
    return null ;
  }

  @Override
  public AnnexResolver getResolver(String name)
  {
    return null ;
  }

  @Override
  public AnnexUnparser getUnparser(String annexName)
  {
    return null ;
  }

  @Override
  public Set<String> getAvailableAnalysisNames()
  {
    return new HashSet<String>(0) ;
  }

  @Override
  public Set<String> getAvailableGeneratorNames()
  {
    return new HashSet<String>(0) ;
  }

  @Override
  public Analyzer getAnalyzer(String analyzerName)
  {
    return null ;
  }

  @Override
  public Generator getGenerator(String name)
  {
    return null ;
  }

  @Override
  public boolean isOSGi()
  {
    return false ;
  }

  @Override
  public void init(AadlModelInstantiatior modelInstantiatior,
                   PredefinedAadlModelManager predefinedAadlModels)
  {

  }

  @Override
  public AadlModelInstantiatior getModelInstantiatior()
  {
    return null ;
  }

  @Override
  public PredefinedAadlModelManager getPredefinedAadlModels()
  {
    return null ;
  }
}
