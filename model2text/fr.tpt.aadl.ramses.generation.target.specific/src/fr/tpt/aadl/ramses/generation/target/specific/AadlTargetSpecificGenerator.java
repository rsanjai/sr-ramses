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

package fr.tpt.aadl.ramses.generation.target.specific;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.SystemImplementation;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.xtext.aadl2.properties.linking.PropertiesLinkingService;

import fr.tpt.aadl.ramses.control.support.InstantiationManager;
import fr.tpt.aadl.ramses.control.support.RamsesConfiguration;
import fr.tpt.aadl.ramses.control.support.WorkflowPilot;
import fr.tpt.aadl.ramses.control.support.analysis.Analyzer;
import fr.tpt.aadl.ramses.transformation.atl.AadlToTargetSpecificAadl;
import fr.tpt.aadl.ramses.control.support.generator.GenerationException;
import fr.tpt.aadl.ramses.control.support.generator.Generator;
import fr.tpt.aadl.ramses.control.support.services.ServiceRegistry;
import fr.tpt.aadl.ramses.control.support.services.ServiceRegistryProvider;


public class AadlTargetSpecificGenerator implements Generator
{
  protected AadlToTargetSpecificAadl _targetTrans ;
  
  protected AadlTargetSpecificCodeGenerator _codeGen ;
  
  protected String _registryName = null ;
  
  protected AadlTargetSpecificGenerator()
  {
  }
  
  public AadlTargetSpecificGenerator(AadlToTargetSpecificAadl targetTrans,
                                     AadlTargetSpecificCodeGenerator codeGen)
  {
    _targetTrans = targetTrans ;
    _codeGen = codeGen ;
  }
  
  public void setRegistryName(String registryName)
  {
    _registryName = registryName ;
  }
  
  @Override
  public String getRegistryName()
  {
    return _registryName ;
  }

  @Override
  public String getPluginName()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  @Override
  public String getPluginId()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  @Override
  public void generate(SystemInstance systemInstance,
                       File resourceFilePath,
                       File generatedFilePath)
                                                      throws GenerationException
  {
    Resource inputResource = systemInstance.eResource() ;
    
    Resource r = _targetTrans.transform(inputResource, resourceFilePath,
                                        generatedFilePath);
    
    _codeGen.generate(r, generatedFilePath) ;
  }

  @Override
  public void generateWorkflow(SystemInstance systemInstance,
                          File resourceDir,
                          File generatedDir,
                          WorkflowPilot workflowPilot) throws GenerationException
  {
    Resource r = systemInstance.eResource() ;
    SystemInstance currentInstance = systemInstance;
    String systemToInstantiate = systemInstance.getSystemImplementation().getName();

    while(workflowPilot.hasNextOperation())
    {
      String operation = workflowPilot.getNextOperation();
      if(operation == null)
    	  return;
      if(operation.equals("analysis"))
      {
        String analysisName = workflowPilot.getNextAnalysisName();
        String analysisMode = workflowPilot.getNextAnalysisMode();
        System.out.println("Analysis launched : " + analysisName + " | Analysis mode : " + analysisMode);
        try {
          ServiceRegistry sr = ServiceRegistryProvider.getServiceRegistry();
          Analyzer a = sr.getAnalyzer(analysisName);
          if (a == null)
          {
        	  System.err.println("Unknown analysis: " + analysisName);
          }
          else
          {
        	  a.performAnalysis(currentInstance, ServiceRegistry.ANALYSIS_ERR_REPORTER_MANAGER,
                      new NullProgressMonitor()) ;
          }
          
        } catch (Exception e) {
        //} catch (AnalysisResultException e) {
          e.printStackTrace();
        }


        // Here is the line where to catch the analysis result
        // Then just send it to the xmlPilot



        if(analysisMode.equals("automatic"))
        {
          workflowPilot.setAnalysisResult(true);
          System.out.println(">> " + analysisName + " result set at true");
        }
        else if(analysisMode.equals("manual")) {
          int res = JOptionPane.showConfirmDialog(null, "Was the analysis " + analysisName + " successfull?", "Confirmation", JOptionPane.YES_NO_OPTION);
          if(res == JOptionPane.YES_OPTION) 
          {
        	workflowPilot.setAnalysisResult(true);
            System.out.println(">> " + analysisName + " result set at true");
          }
          else
          {
        	workflowPilot.setAnalysisResult(false);
            System.out.println(">> " + analysisName + " result set at false");
          }
        }
      }
      else if(operation.equals("transformation"))
      {
        List<String> resourceFileNameList = workflowPilot.getNextTransformationFileNameList();
        System.out.println("Transformation launched : " + resourceFileNameList);

        Resource result = _targetTrans.transformXML(r, resourceDir, resourceFileNameList, 
                                                    generatedDir);

        InstantiationManager instantiator = RamsesConfiguration.getInstantiationManager();
        PropertiesLinkingService pls = new PropertiesLinkingService ();
        SystemImplementation si = (SystemImplementation) pls.
        		findNamedElementInsideAadlPackage(systemToInstantiate, 
        				((AadlPackage) result.getContents().get(0)).getOwnedPublicSection());
        SystemInstance newInstance =
        		instantiator.instantiate(si);
        r = newInstance.eResource();
        currentInstance = newInstance;
      }
      else if(operation.equals("errorstate"))
      {
        System.err.println("\n\nXML piloting achieved in errorstate");
      }
      else if(operation.equals("generation"))
      {
        System.out.println("Generation launched");
        _codeGen.generate(r, generatedDir) ;
      }   
      else
      {
        System.err.println("Undefined operation : " + operation);

      }

      workflowPilot.goForward();
    }
  }
  
  @Override
  public void setParameters(Map<String, Object> parameters)
  {
    // AadlTargetSpecificGenerator does not take any parameters: nothing to do.
  }

}