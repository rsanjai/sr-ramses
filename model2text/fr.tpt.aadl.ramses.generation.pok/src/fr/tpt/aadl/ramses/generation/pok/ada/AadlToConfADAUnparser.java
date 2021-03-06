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

package fr.tpt.aadl.ramses.generation.pok.ada;

import java.io.File ;
import java.io.IOException ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.eclipse.core.runtime.IProgressMonitor ;
import org.eclipse.emf.common.util.EList ;
import org.osate.aadl2.ComponentCategory ;
import org.osate.aadl2.ConnectedElement ;
import org.osate.aadl2.DataPort ;
import org.osate.aadl2.DataSubcomponent ;
import org.osate.aadl2.DirectionType ;
import org.osate.aadl2.EventDataPort ;
import org.osate.aadl2.MemorySubcomponent ;
import org.osate.aadl2.NumberValue ;
import org.osate.aadl2.Port ;
import org.osate.aadl2.ProcessImplementation ;
import org.osate.aadl2.ProcessSubcomponent ;
import org.osate.aadl2.ProcessorSubcomponent ;
import org.osate.aadl2.Subcomponent ;
import org.osate.aadl2.SystemImplementation ;
import org.osate.aadl2.ThreadImplementation ;
import org.osate.aadl2.ThreadSubcomponent ;
import org.osate.aadl2.VirtualProcessorSubcomponent ;
import org.osate.aadl2.instance.ComponentInstance ;
import org.osate.aadl2.instance.ConnectionInstance ;
import org.osate.aadl2.instance.ConnectionReference ;
import org.osate.aadl2.instance.FeatureCategory ;
import org.osate.aadl2.instance.FeatureInstance ;
import org.osate.aadl2.instance.SystemInstance ;
import org.osate.aadl2.modelsupport.UnparseText ;
import org.osate.utils.PropertyUtils ;

import fr.tpt.aadl.ramses.control.atl.hooks.impl.HookAccessImpl ;
import fr.tpt.aadl.ramses.control.support.generator.AadlTargetUnparser ;
import fr.tpt.aadl.ramses.control.support.generator.GenerationException ;
import fr.tpt.aadl.ramses.control.support.generator.TargetProperties ;
import fr.tpt.aadl.ramses.control.support.services.ServiceProvider ;
import fr.tpt.aadl.ramses.control.support.utils.FileUtils ;
import fr.tpt.aadl.ramses.generation.ada.GenerationUtilsADA ;
import fr.tpt.aadl.ramses.generation.c.GenerationUtilsC ;
import fr.tpt.aadl.ramses.generation.utils.GeneratorUtils ;
import fr.tpt.aadl.ramses.generation.utils.RoutingProperties ;

public class AadlToConfADAUnparser implements AadlTargetUnparser
{
  private final static long DEFAULT_REQUIRED_STACK_SIZE = 16384 ;

  // TODO: factorization with ATL transformation into a naming class or enum. 
  public final static String BLACKBOARD_AADL_TYPE =
        "arinc653_runtime::Blackboard_Id_Type" ;

  public final static String QUEUING_AADL_TYPE =
        "arinc653_runtime::Queuing_Port_Id_Type" ;

  public final static String SAMPLING_AADL_TYPE =
        "arinc653_runtime::Sampling_Port_Id_Type" ;

  public final static String EVENT_AADL_TYPE =
        "arinc653_runtime::Event_Id_Type" ;

  public final static String BUFFER_AADL_TYPE =
        "arinc653_runtime::Buffer_Id_Type" ;

  public final static String SEMAPHORE_AADL_TYPE =
        "arinc653_runtime::Semaphore_Id_Type" ;

  private ProcessorProperties _processorProp ;

  private UnparseText deploymentHeaderCode = new UnparseText() ;
  
  private static Logger _LOGGER = Logger.getLogger(AadlToConfADAUnparser.class) ;

  public void process(ProcessorSubcomponent processor,
                      TargetProperties tarProp,
                      File runtimePath,
                      File outputDir,
                      IProgressMonitor monitor)
                                                      throws GenerationException
  {
    ProcessorProperties processorProp = new ProcessorProperties() ;
    ComponentInstance processorInst =
          (ComponentInstance) HookAccessImpl.getTransformationTrace(processor) ;
    RoutingProperties routing = (RoutingProperties) tarProp ;

    // Discard older processor properties !
    _processorProp = processorProp ;

    // Generate deployment.h
    genDeploymentHeader(processor, deploymentHeaderCode, routing) ;

    // Generate deployment.c
    UnparseText deploymentImplCode = new UnparseText() ;
    genDeploymentImpl(processor, deploymentImplCode, processorProp) ;

    // Generate routing.h
    UnparseText routingHeaderCode = new UnparseText() ;
    genRoutingHeader(processorInst, routingHeaderCode, routing) ;

    // Generate routing.c
    UnparseText routingImplCode = new UnparseText() ;
    genRoutingImpl(processorInst, routingImplCode, routing) ;

    try
    {
      FileUtils.saveFile(outputDir, "deployment.h", deploymentHeaderCode
                               .getParseOutput()) ;

      FileUtils.saveFile(outputDir, "deployment.c", deploymentImplCode
                               .getParseOutput()) ;

      FileUtils.saveFile(outputDir, "routing.h", routingHeaderCode
                               .getParseOutput()) ;

      FileUtils.saveFile(outputDir, "routing.c", routingImplCode
                               .getParseOutput()) ;
    }
    catch(IOException e)
    {
      String errMsg = "cannot save the generated files" ;
      _LOGGER.fatal(errMsg, e) ;
      throw new RuntimeException(errMsg, e) ;
    }
  }

	//TODO : be refactored with generic interfaces.
	private void blackBoardHandler(String id, FeatureInstance fi, PartitionProperties pp)
	{

		BlackBoardInfo blackboardInfo = new BlackBoardInfo() ;
		blackboardInfo.id = id;

		if(fi.getCategory() == FeatureCategory.DATA_PORT)
		{
			DataPort p  = (DataPort) fi.getFeature() ;
			
			String value = PropertyUtils.getStringValue(p.getDataFeatureClassifier(),
			                                            "Source_Name") ;
			if(value != null)
			{
			  blackboardInfo.dataType= value ;
			}
			else
			{
			  blackboardInfo.dataType = GenerationUtilsADA.getGenerationADAIdentifier(
			                        p.getDataFeatureClassifier().getQualifiedName()) ;
			}
		}
		pp.blackboardInfo.add(blackboardInfo);
	}

	//TODO : be refactored with generic interfaces.
	private void queueHandler(String id, FeatureInstance fi, PartitionProperties pp)
	{
		Port p = getProcessPort(fi) ;

		QueueInfo queueInfo = new QueueInfo() ;

		queueInfo.id = id;
		queueInfo.uniqueId = AadlToPokADAUtils.getFeatureLocalIdentifier(fi);
		queueInfo.direction = p.getDirection() ;

		if(fi.getCategory() == FeatureCategory.EVENT_DATA_PORT)
		{
			EventDataPort port  = (EventDataPort) fi.getFeature() ;
			String value = PropertyUtils.getStringValue(port.getDataFeatureClassifier(),
			                                            "Source_Name") ;
			if(value != null)
			{
			  queueInfo.dataType=value ;
			}
			else
			{
			  queueInfo.dataType = GenerationUtilsADA.getGenerationADAIdentifier(port.getDataFeatureClassifier().getQualifiedName()) ;
			}
		}

		if(getQueueInfo(p, queueInfo))
		{
			pp.queueInfo.add(queueInfo) ;
		}
	}

	private void bufferHandler(String id, FeatureInstance fi, PartitionProperties pp)
	{

		QueueInfo queueInfo = new QueueInfo() ;


		queueInfo.id = id;
		queueInfo.uniqueId = AadlToPokADAUtils.getFeatureLocalIdentifier(fi);

		if(fi.getCategory() == FeatureCategory.EVENT_DATA_PORT)
		{
			EventDataPort port  = (EventDataPort) fi.getFeature() ;
			String value = PropertyUtils.getStringValue(port.getDataFeatureClassifier(),
			                                            "Source_Name") ;
			if(value != null)
			{
			  queueInfo.dataType= value ;
			}
			else
			{
			  queueInfo.dataType = GenerationUtilsADA.getGenerationADAIdentifier(port.getDataFeatureClassifier().getQualifiedName()) ;
			}
		}

		if(getQueueInfo((Port)fi.getFeature(), queueInfo))
		{
			pp.bufferInfo.add(queueInfo) ;
		}
		else
		{
			queueInfo.type = "FIFO";
			queueInfo.size = 1;
			pp.bufferInfo.add(queueInfo) ;
		}

	}

	private Port getProcessPort(FeatureInstance fi)
	{
		ConnectionInstance ci = null ;
		ConnectionReference cf = null ;
		ConnectedElement c = null ;

		// TODO : queue information are not always in process, recursively fetch
		// these informations.
		if(DirectionType.OUT == fi.getDirection())
		{
			ci = fi.getSrcConnectionInstances().get(0) ;
			cf = ci.getConnectionReferences().get(0) ; 
			c = (ConnectedElement)(cf.getConnection().getDestination()) ;
		}
		else
		{
			ci = fi.getDstConnectionInstances().get(0) ;
			EList<ConnectionReference> crl = ci.getConnectionReferences() ;
			cf = crl.get(crl.size() -1) ;
			c = (ConnectedElement)(cf.getConnection()).getSource() ;
		}

		Port p = (Port) c.getConnectionEnd() ;

		return p ;
	}

	// Return false QueueInfo object is not completed.
	private boolean getQueueInfo(Port port, QueueInfo info)
	{
		boolean result = true ;

		// XXX temporary. Until ATL transformation modifications.
		//  info.id = RoutingProperties.getFeatureLocalIdentifier(fi) ;

		if(info.type == null)
    {
      String value = PropertyUtils.getEnumValue(port, "Queue_Processing_Protocol") ;
      if(value != null)
      {
        info.type = value ;
      }
      else
      {
        result = false ;
      }
    }
		
		if(info.size == -1)
    {
		  Long value = PropertyUtils.getIntValue(port, "Queue_Size") ;
      if(value != null)
      {
        info.size = value.intValue() ;
      }
      else
      {
        result = false ;
      }
    }
		
		return result ;
	}

	// TODO : be refactored with generic interfaces. 
	private void sampleHandler(String id, FeatureInstance fi, PartitionProperties pp)
	{
		Port p = getProcessPort(fi) ;

		SampleInfo sampleInfo = new SampleInfo() ;
		sampleInfo.direction = p.getDirection() ;

		if(fi.getCategory() == FeatureCategory.DATA_PORT)
		{
			DataPort port  = (DataPort) fi.getFeature() ;
			String value = PropertyUtils.getStringValue(port.getDataFeatureClassifier(),
			                                            "Source_Name") ;
			if(value != null)
			{
			  sampleInfo.dataType=value ;
			}
			else
			{
				sampleInfo.dataType = GenerationUtilsADA.getGenerationADAIdentifier(port.getDataFeatureClassifier().getQualifiedName()) ;
			}
		}

		sampleInfo.id = id;
		sampleInfo.uniqueId = AadlToPokADAUtils.getFeatureLocalIdentifier(fi);

		if(getSampleInfo(p, sampleInfo))
		{
			pp.sampleInfo.add(sampleInfo) ;
		}
	}

	private boolean getSampleInfo(Port port, SampleInfo info)
	{
		boolean result = true ;

		// XXX temporary. Until ATL transformation modifications.
		//  info.id = RoutingProperties.getFeatureLocalIdentifier(fi) ;

		if(info.refresh == -1)
    {
      Long value = PropertyUtils.getIntValue(port, "Sampling_Refresh_Period") ;
      if(value != null)
      {
        info.refresh = value ;
      }
      else
      {
        String errMsg =  "sampling port \'"+port.getQualifiedName()+"\' should have property" +
            " Sampling_Refresh_Period" ;
        _LOGGER.error(errMsg);
        ServiceProvider.SYS_ERR_REP.error(errMsg, true);
        result = false ;
      }
    }
		
		return result ;
	}

	private void genQueueMainImpl(UnparseText mainImplCode,
			PartitionProperties pp)
	{

		for(QueueInfo info : pp.queueInfo)
		{
			mainImplCode.addOutput("  CREATE_QUEUING_PORT (\"") ;
			mainImplCode.addOutput(info.uniqueId);
			mainImplCode.addOutputNewline("\",");
			mainImplCode.addOutput(info.dataType+
		      		"'size, ") ;
			mainImplCode.addOutput(Long.toString(info.size)) ;
			mainImplCode.addOutput(", ");

			String direction = null ;
			if(DirectionType.IN == info.direction)
			{
				direction = "DESTINATION" ;
			}
			else
			{
				direction = "SOURCE" ;
			}

			mainImplCode.addOutput(direction);
			mainImplCode.addOutput(", ") ;
			mainImplCode.addOutput(info.type.toUpperCase());
			mainImplCode.addOutputNewline(",");
			mainImplCode.addOutput("      ") ;
			mainImplCode.addOutput(info.id) ;
			mainImplCode.addOutputNewline(", ret);") ;
		}
	}

	private void genEventMainImpl(UnparseText mainImplCode,
			PartitionProperties pp)
	{
		for(String eventId: pp.eventNames)
		{
			mainImplCode.addOutput("  CREATE_EVENT (\"") ;
			mainImplCode.addOutput(eventId);
			mainImplCode.addOutput("\",");
			mainImplCode.addOutput(eventId+",");
			mainImplCode.addOutputNewline(" ret);");
		}
	}

	private void genBufferMainImpl(UnparseText mainImplCode,
			PartitionProperties pp)
	{
		for(QueueInfo info: pp.bufferInfo)
		{
			mainImplCode.addOutput("  CREATE_BUFFER (\"") ;
			mainImplCode.addOutput(info.id);
			mainImplCode.addOutput("\",");
			mainImplCode.addOutput(info.dataType+
		      		"'size, ") ;
			mainImplCode.addOutput(Long.toString(info.size)) ;
			mainImplCode.addOutput(", ");
			mainImplCode.addOutput(info.type.toUpperCase());
			mainImplCode.addOutput(",");
			mainImplCode.addOutput(info.id+",");
			mainImplCode.addOutputNewline("ret);");
		}
	}

	private void genSampleMainImpl(UnparseText mainImplCode,
			PartitionProperties pp)
	{
		for(SampleInfo info : pp.sampleInfo)
		{
			mainImplCode.addOutput("  CREATE_SAMPLING_PORT (\"") ;
			mainImplCode.addOutput(info.uniqueId);
			mainImplCode.addOutputNewline("\",");
			mainImplCode.addOutput(info.dataType+
		      		"'size, ") ;
			String direction = null ;
			if(DirectionType.IN == info.direction)
			{
				direction = "DESTINATION" ;
			}
			else
			{
				direction = "SOURCE" ;
			}

			mainImplCode.addOutput(direction);
			mainImplCode.addOutput(", ") ;
			mainImplCode.addOutput(Long.toString(info.refresh)) ;
			mainImplCode.addOutputNewline(",");
			mainImplCode.addOutput("      ") ;
			mainImplCode.addOutput(info.id) ;
			mainImplCode.addOutputNewline(", ret;") ;
		}
	}

	public void process(ProcessSubcomponent process,
	                    TargetProperties tarProp,
	                    File runtimePath,
                      File outputDir,
                      IProgressMonitor monitor) throws GenerationException
	{
		PartitionProperties pp = new PartitionProperties();

		ProcessImplementation processImpl = (ProcessImplementation) 
				process.getComponentImplementation() ;

		this.findCommunicationMechanism(processImpl, pp);

		// Generate main.h
		UnparseText mainHeaderCode = new UnparseText() ;
		genMainHeader(processImpl, mainHeaderCode, _processorProp, pp);
		
		// Generate main.c
		UnparseText mainCImplCode = new UnparseText() ;
		mainCImplCode.addOutputNewline("int main(){");
		mainCImplCode.incrementIndent();
		mainCImplCode.addOutputNewline("main_ada();");
		mainCImplCode.decrementIndent();
		mainCImplCode.addOutputNewline("}");
		
		// Generate main.ads
		UnparseText mainSpecificationCode = new UnparseText() ;
		genMainSpecification(processImpl, mainSpecificationCode, _processorProp, pp);

		// Generate main.adb
		UnparseText mainImplCode = new UnparseText() ;
		genMainImpl(processImpl, mainImplCode, pp) ;

		try
		{
			FileUtils.saveFile(outputDir, "main_ada.ads",
					mainSpecificationCode.getParseOutput()) ;

			FileUtils.saveFile(outputDir, "main.h",
					mainHeaderCode.getParseOutput()) ;
			
			FileUtils.saveFile(outputDir, "main_ada.adb",
					mainImplCode.getParseOutput()) ;
			
			FileUtils.saveFile(outputDir, "main.c",
					mainCImplCode.getParseOutput()) ;
		}
		catch(IOException e)
		{
		  String errMsg = "cannot save the generated files" ;
		  _LOGGER.fatal(errMsg, e) ;
      throw new RuntimeException(errMsg, e) ;
		}
	}

	private PartitionProperties genMainHeader(ProcessImplementation process,
			UnparseText mainHeaderCode,
			ProcessorProperties processorProp,
			PartitionProperties pp)
	{
		List<ThreadSubcomponent> bindedThreads =
				process.getOwnedThreadSubcomponents() ;

		String guard = GenerationUtilsC.generateHeaderInclusionGuard("main.h") ;
		mainHeaderCode.addOutputNewline(guard) ;

		/**** #DEFINE ****/

		mainHeaderCode.addOutputNewline("#define POK_GENERATED_CODE 1") ;

		if(processorProp.consoleFound == true)
		{
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_CONSOLE 1") ;
		}

		if(processorProp.stdioFound == true)
		{
			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_LIBC_STDIO 1") ;
		}

		if(processorProp.stdlibFound == true)
		{
			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_LIBC_STDLIB 1") ;
		}

		mainHeaderCode
		.addOutputNewline("#define POK_CONFIG_NB_THREADS " +
				Integer.toString(bindedThreads.size() + 1)) ;

		if(pp.hasBlackboard)
		{
			mainHeaderCode
			.addOutputNewline("#define POK_CONFIG_NB_BLACKBOARDS " +
					pp.blackboardInfo.size()) ;
			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_ARINC653_BLACKBOARD 1") ;

			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_BLACKBOARDS 1") ;
		}

		if(pp.hasEvent)
		{
			mainHeaderCode
			.addOutputNewline("#define POK_CONFIG_NB_EVENTS " +
					pp.eventNames.size()) ;
			mainHeaderCode
			.addOutputNewline("#define POK_CONFIG_ARINC653_NB_EVENTS " +
					pp.eventNames.size()) ;

			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_ARINC653_EVENT 1") ;

			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_EVENTS 1") ;

			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_ARINC653_EVENTS 1") ;
		}

		if(pp.hasQueue)
		{
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_ARINC653_QUEUEING 1") ;

			// XXX ARBITRARY
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_LIBC_STRING 1");
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_PARTITIONS 1");
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_PROTOCOLS 1") ;

		}

		if(pp.hasSample)
		{
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_ARINC653_SAMPLING 1");
			// XXX ARBITRARY
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_LIBC_STRING 1");
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_PARTITIONS 1");
			mainHeaderCode.addOutputNewline("#define POK_NEEDS_PROTOCOLS 1") ;
		}

		if(pp.hasBuffer)
		{

			mainHeaderCode
			.addOutputNewline("#define POK_CONFIG_NB_BUFFERS " +
					pp.bufferInfo.size()) ;

			mainHeaderCode
			.addOutputNewline("#define POK_NEEDS_BUFFERS 1") ;

			mainHeaderCode.addOutputNewline("#define POK_NEEDS_ARINC653_BUFFER 1");
		}

		mainHeaderCode
		.addOutputNewline("#define POK_CONFIG_STACKS_SIZE " +
				Long.toString(processorProp.requiredStackSizePerPartition.get(process)));

		// XXX Is there any condition for the generation of theses directives ?
		// XXX ARBITRARY
		mainHeaderCode
		.addOutputNewline("#define POK_NEEDS_ARINC653_TIME 1") ;
		mainHeaderCode
		.addOutputNewline("#define POK_NEEDS_ARINC653_PROCESS 1") ;
		mainHeaderCode
		.addOutputNewline("#define POK_NEEDS_ARINC653_PARTITION 1") ;

		// XXX Is there any condition for the generation of POK_NEEDS_MIDDLEWARE ?
		// XXX ARBITRARY
		mainHeaderCode
		.addOutputNewline("#define POK_NEEDS_MIDDLEWARE 1") ;

		/**** "#INCLUDE ****/

		mainHeaderCode.addOutputNewline("");

		// always files included:
		mainHeaderCode.addOutputNewline("#include <arinc653/types.h>");
		mainHeaderCode.addOutputNewline("#include <arinc653/process.h>");
		mainHeaderCode.addOutputNewline("#include <arinc653/partition.h>");
		mainHeaderCode.addOutputNewline("#include <arinc653/time.h>");

		// conditioned files included:
		if(pp.hasBlackboard)
		{
			mainHeaderCode.addOutputNewline("#include <arinc653/blackboard.h>");
		}

		if(pp.hasQueue)
		{
			mainHeaderCode.addOutputNewline("#include <arinc653/queueing.h>");

			// XXX ARBITRARY
			mainHeaderCode.addOutputNewline("#include <protocols/protocols.h>");
		}

		mainHeaderCode.addOutputNewline("\n#endif") ;

		return pp ;
	}

	
	//Generate global variables.
	private void genGlobalVariablesMainImpl(ProcessImplementation process, EList<ThreadSubcomponent> lthreads,
			UnparseText mainImplCode,
			PartitionProperties pp)
	{
		String guard = GenerationUtilsADA.generateHeaderInclusionGuard("main_ada.adb") ;
		mainImplCode.addOutput(guard) ;

		mainImplCode.addOutputNewline(GenerationUtilsADA.generateSectionMarkAda()) ;
		mainImplCode.addOutputNewline(GenerationUtilsADA
				.generateSectionTitleAda("GLOBAL VARIABLES")) ;

		// Generate thread names array.
		if(false == lthreads.isEmpty())
		{
			mainImplCode
			.addOutputNewline(
					"arinc_threads : array (1 .. POK_CONFIG_NB_THREADS) of Process_Id_Type;") ;
		}

		mainImplCode.addOutputNewline("  tattr : Process_Attribute_Type;") ;
		mainImplCode.addOutputNewline("  ret : Return_Code_Type;") ;
		
		// Generate blackboard names array.
		if(pp.hasBlackboard)
		{
			
			mainImplCode.addOutput("pok_blackboards_names: array(0..POK_CONFIG_NB_BLACKBOARDS-1) of String (1..80) := (") ;
			int counter = 0;
			for(BlackBoardInfo info : pp.blackboardInfo)
			{
				if(counter!=0)
					mainImplCode.addOutput(",") ;
				mainImplCode.addOutput(Integer.toString(counter)+" => ") ;
				mainImplCode.addOutput("\"") ;
				mainImplCode.addOutput(info.id) ;
				mainImplCode.addOutput("\"") ;
				counter++;
			}
			mainImplCode.addOutputNewline(");") ;
			mainImplCode.addOutputNewline("pragma Export(C, pok_blackboards_names, \"pok_blackboards_names\");");

			// Generate external variable (declared in deployment.adb).
			for(BlackBoardInfo info : pp.blackboardInfo)
			{
				mainImplCode.addOutput(info.id + " : ") ;
				mainImplCode.addOutputNewline("BLACKBOARD_ID_TYPE;") ;
			}
		}

		// Generate event names array.
		if(pp.hasEvent)
		{
			mainImplCode.addOutput("pok_arinc653_events_names is array(POK_CONFIG_NB_EVENTS) of String := (") ;

			for(String name : pp.eventNames)

			{
				mainImplCode.addOutput("\"") ;
				mainImplCode.addOutput(name) ;
				mainImplCode.addOutput("\"") ;
			}

			mainImplCode.addOutputNewline(");") ;

			// Generate external variable (declared in deployment.adb).
			for(String name : pp.eventNames)
			{
				mainImplCode.addOutput(name+" : ") ;
				mainImplCode.addOutputNewline(" EVENT_ID_TYPE;") ;
			}
		}

		// Generate buffer names array.
		if(pp.hasBuffer)
		{
			mainImplCode.addOutput("pok_buffers_names is array(POK_CONFIG_NB_BUFFERS) of String := (") ;

			for(QueueInfo info : pp.bufferInfo)
			{
				mainImplCode.addOutput("\"") ;
				mainImplCode.addOutput(info.id) ;
				mainImplCode.addOutput("\"") ;
			}

			mainImplCode.addOutputNewline(");") ;

			// Generate external variable (declared in deployment.adb).
			for(QueueInfo info : pp.bufferInfo)
			{
				mainImplCode.addOutput(info.id+" : ") ;
				mainImplCode.addOutputNewline(" Buffer_Id_Type;") ;				
			}

		}

		// Generate queue names array.
		if(pp.hasQueue)
		{
			for(QueueInfo info : pp.queueInfo)
			{
				mainImplCode.addOutput(info.id+" : ") ;
				mainImplCode.addOutputNewline(" Queuing_Port_Id_Type;") ;
			}
		}

		// Generate sample names array.
		if(pp.hasSample)
		{
			for(SampleInfo info : pp.sampleInfo)
			{   
				mainImplCode.addOutput(info.id+" : ") ;
				mainImplCode.addOutputNewline(" Sampling_Port_Id_Type;") ;
			}
		}

		genGlobalVariablesMainOptional(process, mainImplCode);

		mainImplCode.addOutputNewline(GenerationUtilsADA.generateSectionMarkAda()) ;
	}

	protected void genGlobalVariablesMainOptional(ProcessImplementation process,
			UnparseText mainImplCode){}

	private void genFileIncludedMainImpl(UnparseText mainHeaderCode)
	{
		// Files included.
		mainHeaderCode.addOutputNewline(GenerationUtilsADA.generateSectionMarkAda()) ;
		mainHeaderCode.addOutputNewline(GenerationUtilsADA
				.generateSectionTitleAda("INCLUSION")) ;
		// always files included:
        // for(String s: AadlToADAUnparser.Includes)
	    mainHeaderCode.addOutputNewline("with APEX; use APEX;");
	    mainHeaderCode.addOutputNewline("with APEX.Processes; use APEX.Processes;");
	    mainHeaderCode.addOutputNewline("with APEX.Partitions; use APEX.Partitions;");
	    mainHeaderCode.addOutputNewline("with APEX.Timing; use APEX.Timing;");
	    mainHeaderCode.addOutputNewline("with Activity; use Activity;");
	    mainHeaderCode.addOutputNewline("with Deployment; use Deployment;");
	    mainHeaderCode.addOutputNewline("with Gtypes; use Gtypes;");
	    mainHeaderCode.addOutputNewline("with Interfaces.C; use Interfaces.C;");
	}
	
	private void genThreadDeclarationMainImpl(ThreadSubcomponent thread,
			int threadIndex,
			UnparseText mainImplCode)
	{
			
		ThreadImplementation timpl =
				(ThreadImplementation) thread.getComponentImplementation() ;

		mainImplCode.addOutput("  tattr.ENTRY_POINT := ") ;
		mainImplCode.addOutput(GenerationUtilsADA
				.getGenerationADAIdentifier(timpl.getQualifiedName())) ;
		mainImplCode.addOutputNewline(GenerationUtilsADA.THREAD_SUFFIX +"'Address"+ ';') ;
		String period = null ;
		String timeCapacity = null ;

		Long  value = PropertyUtils.getIntValue(thread, "Period") ;
		if(value != null)
		{
		  period = Long.toString(value) ;
		  mainImplCode.addOutput("  tattr.Period := ") ;
      mainImplCode.addOutputNewline(period + ';') ;
		}
		else
		{
		  period = null ;
		  // If period is not set, don't generate.
		}
		
		mainImplCode.addOutput("  tattr.Deadline := ") ;
		mainImplCode.addOutputNewline("Hard" + ';') ;

		NumberValue nbValue =
        PropertyUtils.getMaxRangeValue(thread, "Compute_Execution_Time") ;
		if(nbValue != null)
		{
		  Double d = nbValue.getScaledValue("ms");
      timeCapacity = Integer.toString(d.intValue()) ;
      mainImplCode.addOutput("  tattr.Time_Capacity := ") ;
      mainImplCode.addOutputNewline(timeCapacity + ';') ;
		}
		else
		{
		  // If compute execution time is not set, don't generate.
			timeCapacity = null ;
		}
		
		String priority = null ;
		value = PropertyUtils.getIntValue(thread, "Priority") ;
		if(value != null)
		{
		  priority = Long.toString(value) ;
		  mainImplCode.addOutput("  tattr.Base_Priority := ") ;
      mainImplCode.addOutputNewline(priority + ';') ;
		}
		else
		{
			priority = null ;
			// If priority is not set, don't generate.
		}

		mainImplCode.incrementIndent();
		mainImplCode
		.addOutputNewline("tattr.Name := \""+thread.getName()+"\";");
		mainImplCode.decrementIndent();
		mainImplCode
		.addOutput("  Create_Process (tattr, arinc_threads(") ;
		mainImplCode.addOutput(Integer.toString(threadIndex)) ;
		mainImplCode.addOutputNewline("), ret);") ;

		mainImplCode
		.addOutput("  Start (arinc_threads(") ;
		mainImplCode.addOutput(Integer.toString(threadIndex)) ;
		mainImplCode.addOutputNewline("), ret);") ;
	}
	
	private void genBlackboardMainImpl(UnparseText mainImplCode,
			PartitionProperties pp)
	{
		// For each blackboard
		for(BlackBoardInfo info : pp.blackboardInfo)
		{
			mainImplCode.addOutput("  CREATE_BLACKBOARD (\"") ;
			mainImplCode.addOutput(info.id) ;
			mainImplCode.addOutput("\", " +info.dataType+
      		"'size, ") ;
			
			mainImplCode.addOutput(info.id);
			mainImplCode.addOutputNewline(", ret);") ;
		}
	}

	private void genMainImpl(ProcessImplementation process,
			UnparseText mainImplCode,
			PartitionProperties pp)
	{
		EList<ThreadSubcomponent> lthreads =
				process.getOwnedThreadSubcomponents() ;
		
		// Global files.
		genGlobalVariablesMainImpl(process, lthreads, mainImplCode, pp);

		
		// main function declaration.
		mainImplCode.addOutputNewline(GenerationUtilsADA
				.generateSectionTitleAda("MAIN BODY")) ;

		mainImplCode.addOutputNewline("procedure Main_Ada is") ;
		
		genMainImplEnd(process, mainImplCode);

		mainImplCode.addOutputNewline("begin") ;
		mainImplCode.incrementIndent();
		mainImplCode.addOutputNewline("Init_Global_Variables;");
		//not complete
		// Blackboard declarations.
		genBlackboardMainImpl(mainImplCode, pp) ;

		// Queue declarations.
		genQueueMainImpl(mainImplCode,pp) ;

		// Sample declarations.
		genSampleMainImpl(mainImplCode, pp) ;

		// Event declarations.
		genEventMainImpl(mainImplCode, pp) ;

		// Buffer declarations.    
		genBufferMainImpl(mainImplCode, pp) ;
		
		// For each declared thread
		// Zero stands for ARINC's IDL thread.
		int threadIndex = 1 ;

		// Thread declarations.
		for(ThreadSubcomponent thread : lthreads)
		{
			genThreadDeclarationMainImpl(thread, threadIndex, mainImplCode) ;
			threadIndex++ ;
		}
		
		mainImplCode
		.addOutputNewline("  Set_Partition_Mode (Normal, ret);") ;
		mainImplCode.decrementIndent();
		mainImplCode.addOutputNewline("end Main_Ada;") ;

		mainImplCode.addOutputNewline(GenerationUtilsADA.generateSectionMarkAda()) ;
		mainImplCode.addOutput("end Main_Ada;") ;
	}

	protected void genMainImplEnd(ProcessImplementation process,
			UnparseText mainImplCode){}

	private void findCommunicationMechanism(ProcessImplementation process,
			PartitionProperties pp)
	{
		EList<Subcomponent> subcmpts = process.getAllSubcomponents() ;

		for(Subcomponent s : subcmpts)
		{
			if(s instanceof DataSubcomponent)
			{
				if(s.getClassifier().getQualifiedName()
						.equalsIgnoreCase(BLACKBOARD_AADL_TYPE))
				{
					blackBoardHandler(s.getName(),
							(FeatureInstance) HookAccessImpl.
							getTransformationTrace(s), pp);
					pp.hasBlackboard = true;
				}
				else if(s.getClassifier().getQualifiedName()
						.equalsIgnoreCase(QUEUING_AADL_TYPE))
				{
					queueHandler(s.getName(),
							(FeatureInstance) HookAccessImpl.
							getTransformationTrace(s), pp);

					pp.hasQueue = true ;
				}
				else if(s.getClassifier().getQualifiedName()
						.equalsIgnoreCase(SAMPLING_AADL_TYPE))
				{
					sampleHandler(s.getName(), (FeatureInstance) HookAccessImpl.
							getTransformationTrace(s), pp);
					pp.hasSample = true ;
				}
				else if(s.getClassifier().getQualifiedName()
						.equalsIgnoreCase(EVENT_AADL_TYPE))
				{
					pp.eventNames.add(s.getName());
					pp.hasEvent = true ;
				}
				else if(s.getClassifier().getQualifiedName()
						.equalsIgnoreCase(BUFFER_AADL_TYPE))
				{
					bufferHandler(s.getName(),
							(FeatureInstance) HookAccessImpl.
							getTransformationTrace(s), pp);
					pp.hasBuffer = true ;
				}
				else if(s.getClassifier().getQualifiedName()
						.equalsIgnoreCase(SEMAPHORE_AADL_TYPE))
				{
					pp.semaphoreNames.add(s.getName());
					pp.hasSemaphore = true ;
				}
			}
		}
	}

	private PartitionProperties genMainSpecification(ProcessImplementation process,
			UnparseText mainHeaderCode,
			ProcessorProperties processorProp,
			PartitionProperties pp)
	{
		List<ThreadSubcomponent> bindedThreads =
                process.getOwnedThreadSubcomponents() ;

		// Included files.
		genFileIncludedMainImpl(mainHeaderCode) ;
		
		// conditioned files included:
		if(pp.hasBlackboard)
		{
			  mainHeaderCode.addOutputNewline("with APEX.Blackboards; use APEX.Blackboards;");
	    }
		if(pp.hasQueue)
		{
			mainHeaderCode.addOutputNewline("with APEX.Queuing_Ports; use APEX.Queuing_Ports;");
		}
		if (pp.hasBuffer)
		{
			mainHeaderCode.addOutputNewline("with APEX.Buffers; use APEX.Buffers;");
		}

		mainHeaderCode.addOutputNewline(GenerationUtilsADA.generateSectionMarkAda()) ;
		mainHeaderCode.addOutputNewline(GenerationUtilsADA
				.generateSectionTitleAda("MAIN SPECIFICATION")) ;

		String guard = GenerationUtilsADA.generateHeaderInclusionGuard("main_ada.ads") ;
		mainHeaderCode.addOutput(guard) ;


		    if(processorProp.stdioFound == true)
		    {
		      mainHeaderCode
		            .addOutputNewline("POK_NEEDS_LIBC_STDIO : constant := 1;") ;
		    }

		    if(processorProp.stdlibFound == true)
		    {
		      mainHeaderCode
		            .addOutputNewline("POK_NEEDS_LIBC_STDLIB : constant := 1;") ;
		    }
		    
		    mainHeaderCode
		          .addOutputNewline("POK_CONFIG_NB_THREADS : constant := " +
		                Integer.toString(bindedThreads.size() + 1)+";") ;
		    
		    if(pp.hasBlackboard)
		    {
		      mainHeaderCode
		            .addOutputNewline("POK_CONFIG_NB_BLACKBOARDS : constant := " +
		                  pp.blackboardInfo.size()+";") ;
		      mainHeaderCode
		            .addOutputNewline("POK_NEEDS_ARINC653_BLACKBOARD : constant := 1;") ;
		      
		      mainHeaderCode
		      .addOutputNewline("POK_NEEDS_BLACKBOARDS : constant := 1;") ;
		    }

		    if(pp.hasEvent)
		    {
		      mainHeaderCode
		            .addOutputNewline("POK_CONFIG_NB_EVENTS : constant := " +
		                  pp.eventNames.size()+";") ;
		      mainHeaderCode
		      .addOutputNewline("POK_CONFIG_ARINC653_NB_EVENTS : constant := " +
		            pp.eventNames.size()+";") ;
		      
		      mainHeaderCode
		            .addOutputNewline("POK_NEEDS_ARINC653_EVENT : constant := 1;") ;

		      mainHeaderCode
		      .addOutputNewline("POK_NEEDS_EVENTS : constant := 1;") ;
		      
		      mainHeaderCode
		      .addOutputNewline("POK_NEEDS_ARINC653_EVENTS : constant := 1;") ;
		    }
		    
		    if(pp.hasQueue)
		    {
		      deploymentHeaderCode.addOutputNewline("POK_NEEDS_ARINC653_QUEUEING : constant := 1;") ;
		      
		      // XXX ARBITRARY
		      mainHeaderCode.addOutputNewline("POK_NEEDS_LIBC_STRING : constant := 1;");
		      mainHeaderCode.addOutputNewline("POK_NEEDS_PARTITIONS : constant := 1;");
		      mainHeaderCode.addOutputNewline("POK_NEEDS_PROTOCOLS : constant := 1;") ;
		      
		    }
		    
		    if(pp.hasSample)
		    {
		      mainHeaderCode.addOutputNewline("POK_NEEDS_ARINC653_SAMPLING : constant := 1;");
		      // XXX ARBITRARY
		      mainHeaderCode.addOutputNewline("POK_NEEDS_LIBC_STRING : constant := 1;");
		      mainHeaderCode.addOutputNewline("POK_NEEDS_PARTITIONS : constant := 1;");
		      mainHeaderCode.addOutputNewline("POK_NEEDS_PROTOCOLS : constant := 1;") ;
		    }

		    if(pp.hasBuffer)
		    {
		      
		    mainHeaderCode
		      .addOutputNewline("POK_CONFIG_NB_BUFFERS : constant := " +
		           pp.bufferInfo.size()+";") ;
		      
		    mainHeaderCode
		      .addOutputNewline("POK_NEEDS_BUFFERS : constant := 1;") ;
		      
		    mainHeaderCode.addOutputNewline("POK_NEEDS_ARINC653_BUFFER : constant := 1;");
		    }
		    
		    mainHeaderCode
		    	.addOutputNewline("POK_CONFIG_STACKS_SIZE : constant := " +
		    			Long.toString(processorProp.requiredStackSizePerPartition.get(process))+";");
		    
		    // XXX Is there any condition for the generation of theses directives ?
		    // XXX ARBITRARY
		    mainHeaderCode
		          .addOutputNewline("POK_NEEDS_ARINC653_TIME : constant := 1;") ;
		    mainHeaderCode
		          .addOutputNewline("POK_NEEDS_ARINC653_PROCESS : constant := 1;") ;
		    mainHeaderCode
		          .addOutputNewline("POK_NEEDS_ARINC653_PARTITION : constant := 1;") ;
		    
		    // XXX Is there any condition for the generation of POK_NEEDS_MIDDLEWARE ?
		    // XXX ARBITRARY
		    mainHeaderCode
		          .addOutputNewline("POK_NEEDS_MIDDLEWARE : constant := 1;") ;
		    
//		    mainHeaderCode.addOutputNewline("procedure strcpy(Target : out char_array; Source : in char_array);");
//		    mainHeaderCode.addOutputNewline("pragma Import(C,strcpy, \"strcpy\");");
		    mainHeaderCode.addOutputNewline("procedure Main_Ada;");
		    mainHeaderCode.addOutputNewline("pragma Export(C, Main_Ada, \"main_ada\");") ;
		    mainHeaderCode.addOutputNewline("end Main_Ada;") ;

		return pp ;
	}

	private void genDeploymentImpl(ProcessorSubcomponent processor,
			UnparseText deploymentImplCode,
			ProcessorProperties pokProp)
	{
		deploymentImplCode.addOutputNewline("#include \"deployment.h\"") ;
		deploymentImplCode.addOutputNewline("#include <types.h> ") ;
	}

	private void genDeploymentHeader(ProcessorSubcomponent processor,
			                             UnparseText deploymentHeaderCode,
			                             RoutingProperties routing)
					                                           throws GenerationException
  {
    _processorProp = new ProcessorProperties() ;

    String guard =
          GenerationUtilsADA.generateHeaderInclusionGuard("deployment.h") ;

    deploymentHeaderCode.addOutputNewline(guard) ;

    deploymentHeaderCode.addOutputNewline("#include \"routing.h\"") ;

    // POK::Additional_Features => (libc_stdio,libc_stdlib,console);
    // this property is associated to virtual processors
    List<VirtualProcessorSubcomponent> bindedVPS =
          new ArrayList<VirtualProcessorSubcomponent>() ;

    for(Subcomponent sub : processor.getComponentImplementation()
          .getOwnedSubcomponents())
      if(sub instanceof VirtualProcessorSubcomponent)
      {
        bindedVPS.add((VirtualProcessorSubcomponent) sub) ;
      }

    List<String> additionalFeatures ;

    // Try to fetch POK properties: Additional_Features.
    for(VirtualProcessorSubcomponent vps : bindedVPS)
    {
      additionalFeatures =
          PropertyUtils.getStringListValue(vps, "Additional_Features") ;
      if(additionalFeatures != null)
      {
        for(String s : additionalFeatures)
        {
          if(s.equalsIgnoreCase("console"))
          {
            // POK_NEEDS_CONSOLE has to be in both kernel's deployment.h
            deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_CONSOLE 1") ;
            _processorProp.consoleFound = true ;
            break ;
          }
        }

        for(String s : additionalFeatures)
        {
          if(s.equalsIgnoreCase("libc_stdio"))
          {
            _processorProp.stdioFound = true ;
            break ;
          }
        }

        for(String s : additionalFeatures)
        {
          if(s.equalsIgnoreCase("libc_stdlib"))
          {
            _processorProp.stdlibFound = true ;
            break ;
          }
        }
      }
      else
      {
        String errMsg = "cannot fecth Additional_Features for \'" +
                                                vps.getName() + '\'' ;
        _LOGGER.error(errMsg) ;
        ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
      }
    }

    // TODO: the integer ID in this macro must be set carefully to respect the
    // routing table defined in deployment.c files in the generated code for a
    // partition.
    int id =
          ((SystemImplementation) processor.eContainer())
                .getOwnedProcessorSubcomponents().indexOf(processor) ;
    deploymentHeaderCode.addOutputNewline("#define POK_CONFIG_LOCAL_NODE " +
          Integer.toString(id)) ;
    // POK_GENERATED_CODE 1 always true in our usage context
    deploymentHeaderCode.addOutputNewline("#define POK_GENERATED_CODE 1") ;

    deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_GETTICK 1") ;
    // POK_NEEDS_THREADS 1 always true in our usage context.
    deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_THREADS 1") ;

    if(bindedVPS.size() > 0)
    {
      deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_PARTITIONS 1") ;
    }

    if(System.getProperty("DEBUG") != null)
    {
      deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_DEBUG 1") ;
    }

    deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_SCHED 1") ;
    // The maccro POK_CONFIG_NB_PARTITIONS indicates the amount of partitions in
    // the current system.It corresponds to the amount of process components in
    // the system.
    List<ProcessSubcomponent> bindedProcess =
          GeneratorUtils.getBindedProcesses(processor) ;
    deploymentHeaderCode.addOutputNewline("#define POK_CONFIG_NB_PARTITIONS " +
          Integer.toString(bindedVPS.size())) ;
    List<ThreadSubcomponent> bindedThreads =
          new ArrayList<ThreadSubcomponent>() ;
    List<Integer> threadNumberPerPartition = new ArrayList<Integer>() ;

    for(ProcessSubcomponent p : bindedProcess)
    {
      ProcessImplementation pi =
            (ProcessImplementation) p.getComponentImplementation() ;
      bindedThreads.addAll(pi.getOwnedThreadSubcomponents()) ;
      threadNumberPerPartition.add(Integer.valueOf(pi
            .getOwnedThreadSubcomponents().size())) ;
    }

    //  The maccro POK_CONFIG_NB_THREADS indicates the amount of threads used in 
    //  the kernel.It comprises the tasks for the partition and the main task of 
    //  each partition that initialize all ressources.
    deploymentHeaderCode.addOutputNewline("#define POK_CONFIG_NB_THREADS " +
          Integer.toString(2 + bindedProcess.size() + bindedThreads.size())) ;
    //  The maccro POK_CONFIG_NB_PARTITIONS_NTHREADS indicates the amount of 
    //  threads in each partition we also add an additional process that 
    //  initialize all partition's entities (communication, threads, ...)
    deploymentHeaderCode.addOutput("#define POK_CONFIG_PARTITIONS_NTHREADS {") ;

    int idx = 0 ;
    for(Integer i : threadNumberPerPartition)
    {

      deploymentHeaderCode.addOutput(Integer.toString(i + 1)) ;

      if(idx != (threadNumberPerPartition.size() - 1))
      {
        deploymentHeaderCode.addOutput(",") ;
      }
      idx++ ;
    }

    deploymentHeaderCode.addOutputNewline("}") ;

    for(VirtualProcessorSubcomponent vps : bindedVPS)
    {
      boolean foundRR = false ;

      String requiredScheduler = PropertyUtils.getEnumValue(vps, "Scheduler") ;
      if(requiredScheduler != null)
      {
        if(requiredScheduler.equalsIgnoreCase("RR") && foundRR == false)
        {
          foundRR = true ;
          deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_SCHED_RR 1") ;
        }
      }
      else
      {
        String errMsg = "scheduler is not provided for \'" + vps.getName() + '\'' ;
        _LOGGER.error(errMsg) ;
        ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
      }
    }

    // TODO: define POK_CONFIG_PARTITIONS_SCHEDULER: sched algo associated to 
    // each partition
    deploymentHeaderCode.addOutput("#define POK_CONFIG_PARTITIONS_SCHEDULER {") ;

    for(VirtualProcessorSubcomponent vps : bindedVPS)
    {
      String requiredScheduler = PropertyUtils.getEnumValue(vps, "Scheduler") ;
      if(requiredScheduler != null)
      {
        if(requiredScheduler.equalsIgnoreCase("RR"))
        {
          deploymentHeaderCode.addOutput("POK_SCHED_RR") ;
        }

        if(bindedVPS.indexOf(vps) != bindedVPS.size() - 1)
        {
          deploymentHeaderCode.addOutput(",") ;
        }
      }
      else
      {
        String errMsg = "cannot fetch the scheduler for \'" +
                                                vps.getName() + '\'' ;
        _LOGGER.error(errMsg) ;
        ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
      }
    }

    deploymentHeaderCode.addOutputNewline("}") ;

    boolean queueAlreadyAdded = false ;
    boolean sampleAlreadyAdded = false ;
    boolean bufferAlreadyAdded = false ;
    for(ProcessSubcomponent ps : bindedProcess)
    {
      ProcessImplementation process =
            (ProcessImplementation) ps.getComponentImplementation() ;
      if(!_processorProp.partitionProperties.containsKey(process))
      {
        PartitionProperties pp = new PartitionProperties() ;
        _processorProp.partitionProperties.put(process, pp) ;
        findCommunicationMechanism(process, pp) ;
      }
      PartitionProperties pp = _processorProp.partitionProperties.get(process) ;
      if(pp.hasBlackboard || pp.hasBuffer || pp.hasEvent || pp.hasSemaphore)
      {
        deploymentHeaderCode
              .addOutputNewline("#define POK_NEEDS_LOCKOBJECTS 1") ;
      }

      if(pp.hasQueue && false == queueAlreadyAdded)
      {
        deploymentHeaderCode
              .addOutputNewline("#define POK_NEEDS_PORTS_QUEUEING 1") ;

        queueAlreadyAdded = true ;
      }

      if(pp.hasSample && false == sampleAlreadyAdded)
      {
        deploymentHeaderCode
              .addOutputNewline("#define POK_NEEDS_PORTS_SAMPLING 1") ;

        sampleAlreadyAdded = true ;
      }

      if(pp.hasBuffer && false == bufferAlreadyAdded)
      {
        deploymentHeaderCode
              .addOutputNewline("#define POK_NEEDS_PORTS_BUFFER 1") ;

        bufferAlreadyAdded = true ;
      }
    }

    if(sampleAlreadyAdded || queueAlreadyAdded)
      deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_THREAD_ID 1") ;
    deploymentHeaderCode
          .addOutput("#define POK_CONFIG_PARTITIONS_NLOCKOBJECTS {") ;
    for(ProcessSubcomponent ps : bindedProcess)
    {
      PartitionProperties pp =
            _processorProp.partitionProperties.get((ProcessImplementation) ps
                  .getComponentImplementation()) ;
      deploymentHeaderCode.addOutput(Integer.toString(pp.blackboardInfo.size() +
            pp.bufferInfo.size() + pp.eventNames.size())) ;
      if(bindedProcess.indexOf(ps) < bindedProcess.size() - 1)
      {
        deploymentHeaderCode.addOutput(",") ;
      }
    }
    deploymentHeaderCode.addOutputNewline("}") ;
    //  The maccro POK_CONFIG_PARTTITIONS_SIZE indicates the required amount of 
    //  memory for each partition.This value was deduced from the property 
    //  POK::Needed_Memory_Size of each process
    // comes from property POK::Needed_Memory_Size => XXX Kbyte;
    List<Long> memorySizePerPartition = new ArrayList<Long>() ;

    for(ProcessSubcomponent p : bindedProcess)
    {
      Long mem = PropertyUtils.getIntValue(p, "Needed_Memory_Size") ;
      if(mem != null)
      {
        memorySizePerPartition.add(mem) ;
      }
      else
      {
        String warnMsg = "cannot fetch Needed_Memory_Size for \'" +
                                                p.getName() +
                                                "\'. try to fetch the partition memory" ;
        _LOGGER.warn(warnMsg) ;
        ServiceProvider.SYS_ERR_REP.warning(warnMsg, true) ;

        MemorySubcomponent bindedMemory =
              (MemorySubcomponent) GeneratorUtils
                    .getDeloymentMemorySubcomponent(p) ;
        
        
        mem = PropertyUtils.getIntValue(bindedMemory, "Memory_Size") ;
        if(mem != null)
        {
          memorySizePerPartition.add(mem) ;
        }
        else
        {
          String errMsg = "cannot fetch the partition memory (Memory_Size) for \'" +
                                                  bindedMemory.getName() + '\'' ;
          _LOGGER.error(errMsg) ;
          ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
        }
      }
    }

    deploymentHeaderCode.addOutput("#define POK_CONFIG_PARTITIONS_SIZE {") ;
    idx = 0 ;
    for(Long l : memorySizePerPartition)
    {
      deploymentHeaderCode.addOutput(Long.toString(l)) ;

      if(idx != memorySizePerPartition.size() - 1)
      {
        deploymentHeaderCode.addOutput(",") ;
      }
      idx++ ;
    }

    deploymentHeaderCode.addOutputNewline("}") ;
    
    List<Long> slotPerPartition =
        PropertyUtils.getIntListValue(processor, "Partition_Slots") ;
    if(slotPerPartition != null)
    {
      // POK_CONFIG_SCHEDULING_SLOTS extracted from POK::Paritions_Slots => (500 ms);
      deploymentHeaderCode.addOutput("#define POK_CONFIG_SCHEDULING_SLOTS {") ;
      idx = 0 ;
      for(Long l : slotPerPartition)
      {
        deploymentHeaderCode.addOutput(Long.toString(l)) ;

        if(idx != slotPerPartition.size() - 1)
        {
          deploymentHeaderCode.addOutput(",") ;
        }
        idx++ ;
      }

      deploymentHeaderCode.addOutputNewline("}") ;
      deploymentHeaderCode
            .addOutputNewline("#define POK_CONFIG_SCHEDULING_NBSLOTS " +
                  Integer.toString(slotPerPartition.size())) ;
    }
    else
    {
      String errMsg = "cannot fetch the partition slots for \'" +
                                              processor.getName() + '\'' ;
      _LOGGER.error(errMsg) ;
      ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
    }

    List<Subcomponent> slotsAllocation = PropertyUtils.getSubcomponentList(processor,
                                                           "Slots_Allocation") ;
    if(! slotsAllocation.isEmpty())
    {
      deploymentHeaderCode.addOutput("#define POK_CONFIG_SCHEDULING_SLOTS_ALLOCATION {") ;

      for(Subcomponent sAllocation : slotsAllocation)
      {
        int referencedComponentId = bindedVPS.indexOf(sAllocation) ;
        deploymentHeaderCode.addOutput(Integer.toString(referencedComponentId)) ;

        if(slotsAllocation.indexOf(sAllocation) != slotsAllocation.size() - 1)
        {
          deploymentHeaderCode.addOutput(",") ;
        }
      }

      deploymentHeaderCode.addOutputNewline("}") ;
    }
    else
    {
      String errMsg = "cannot fetch the Slots_Allocation for \'" +
                                              processor.getName() + '\'' ;
      _LOGGER.error(errMsg) ;
      ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
    }
    
    Long majorFrame =
        PropertyUtils.getIntValue(processor, "Module_Major_Frame") ;
    
    if(majorFrame != null)
    {
      deploymentHeaderCode
            .addOutputNewline("#define POK_CONFIG_SCHEDULING_MAJOR_FRAME " +
                  Long.toString(majorFrame)) ;
    }
    else
    {
      String errMsg = "cannot fetch the module major frame for \'" +
                                              processor.getName() + '\'' ;
      _LOGGER.error(errMsg) ;
      ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
    }

    String portsFlushTime = PropertyUtils.getEnumValue(processor,
                                                       "Ports_Flush_Time") ;
    if(portsFlushTime != null)
    {
      if(portsFlushTime.equalsIgnoreCase("Minor_Frame_Switch"))
      {
        Long minorFrame = PropertyUtils.getIntValue(processor,
                                                    "Module_Minor_Frame") ;
        if(minorFrame != null)
        {
          deploymentHeaderCode.addOutputNewline("#define POK_FLUSH_PERIOD " +
                                                Long.toString(minorFrame)) ;
        }
        else
        {
          String errMsg = "Ports_Flush_Time was set to Minor_Frame_Switch for \'" +
                             processor.getName() + "\', but property Module_Minor_Frame" ;
          _LOGGER.error(errMsg) ;
          ServiceProvider.SYS_ERR_REP.error(errMsg, true) ;
        }
      }
      else if(portsFlushTime.equalsIgnoreCase("Partition_Slot_Switch"))
        deploymentHeaderCode.addOutputNewline("#define POK_NEEDS_FLUSH_ON_WINDOWS") ;
    }
    else
    {
      String warnMsg =
            "Ports_Flush_Time was not set on \'" + processor.getName() +
                  "\', default flush policy will be used" ;
      _LOGGER.warn(warnMsg) ;
      ServiceProvider.SYS_ERR_REP.warning(warnMsg, true) ;
    }

    for(ProcessSubcomponent ps : bindedProcess)
    {
      ProcessImplementation pi =
            (ProcessImplementation) ps.getComponentImplementation() ;

      for(ThreadSubcomponent ts : pi.getOwnedThreadSubcomponents())
      {
        Long partitionStack =
            PropertyUtils.getIntValue(ts, "Stack_Size") ;
        if(partitionStack != null)
        {
          _processorProp.requiredStackSize += partitionStack ;
          _processorProp.requiredStackSizePerPartition.put(pi, partitionStack) ;
        }
        else
        {
          _processorProp.requiredStackSize += DEFAULT_REQUIRED_STACK_SIZE ;
          _processorProp.requiredStackSizePerPartition
                .put(pi, DEFAULT_REQUIRED_STACK_SIZE) ;
        }
      }
    }

    deploymentHeaderCode.addOutputNewline("#define POK_CONFIG_STACKS_SIZE " +
          Long.toString(_processorProp.requiredStackSize)) ;

    // XXX is that right ???
    if(routing.buses.isEmpty())
    {
      deploymentHeaderCode.addOutputNewline("#define POK_CONFIG_NB_BUSES 0") ;
    }
    else
    {
      deploymentHeaderCode.addOutputNewline("#define POK_CONFIG_NB_BUSES 1") ;
    }

    genDeploymentHeaderEnd(deploymentHeaderCode) ;

    deploymentHeaderCode.addOutputNewline("#endif") ;
  }                                           

	protected void genDeploymentHeaderEnd(UnparseText deploymentHeaderCode){}

	@Override
	  public void setParameters(Map<Enum<?>, Object> parameters)
    {
      String msg = "setParameters not supported" ;
      _LOGGER.fatal(msg) ;
      throw new UnsupportedOperationException(msg) ;
    }

	  public TargetProperties process(SystemImplementation si,
	                                  File runtimePath,
	                                  File outputDir,
	                                  IProgressMonitor monitor)
		     	                                             throws GenerationException
		{
		  SystemInstance system = (SystemInstance) 
	                                     HookAccessImpl.getTransformationTrace(si) ;
	    
	    RoutingProperties routing = new RoutingProperties();
		  routing.setRoutingProperties(system);
		  
		  return routing ;
		  /*
		  for(ComponentInstance subComponent: system.getComponentInstances())
		  {
			  processComponentInstance(subComponent, generatedFilePath, routing);
		  }
		  */
	  }
	  
	  private List<FeatureInstance> getLocalPorts(ComponentInstance processor,
			                                          RoutingProperties routeProp)
		                                                    throws GenerationException
	  {
		List<FeatureInstance> localPorts = new ArrayList<FeatureInstance>();
		if(routeProp.processPerProcessor.get(processor).isEmpty())
			return localPorts;
		for(ComponentInstance deployedProcess:routeProp.processPerProcessor.get(processor))
		{
		  localPorts.addAll(routeProp.portPerProcess.get(deployedProcess));
		}
		return localPorts;
	  }
	  
	  private void genRoutingHeader(ComponentInstance processor,
	                                UnparseText routingHeaderCode,
	                                RoutingProperties routeProp)
	                                                      throws GenerationException
  {
    String guard = GenerationUtilsADA.generateHeaderInclusionGuard("routing.h") ;
    routingHeaderCode.addOutput(guard) ;

    if(routeProp.processPerProcessor.get(processor) != null)
    {
      int globalPortNb = routeProp.globalPort.size() ;
      routingHeaderCode.addOutputNewline("#define POK_CONFIG_NB_GLOBAL_PORTS " +
            Integer.toString(globalPortNb)) ;

      List<FeatureInstance> localPorts = getLocalPorts(processor, routeProp) ;
      int localPortNb = localPorts.size() ;

      routingHeaderCode.addOutputNewline("#define POK_CONFIG_NB_PORTS " +
            Integer.toString(localPortNb)) ;

      routingHeaderCode.addOutputNewline("#define POK_CONFIG_NB_NODES " +
            Integer.toString(routeProp.processors.size())) ;

      List<VirtualProcessorSubcomponent> bindedVPS =
            new ArrayList<VirtualProcessorSubcomponent>() ;

      for(Subcomponent sub : processor.getSubcomponent()
            .getComponentImplementation().getOwnedSubcomponents())
      {
        if(sub instanceof VirtualProcessorSubcomponent)
        {
          bindedVPS.add((VirtualProcessorSubcomponent) sub) ;
        }
      }

      if(!localPorts.isEmpty())
      {
        routingHeaderCode.addOutput("#define POK_CONFIG_PARTITIONS_PORTS {") ;
        for(FeatureInstance fi : localPorts)
        {
          ComponentInstance processInstance =
                (ComponentInstance) fi.getComponentInstance().eContainer() ;
          if(processInstance.getCategory() == ComponentCategory.PROCESS)
          {
            List<ComponentInstance> bindedVP =
                  PropertyUtils
                        .getComponentInstanceList(processInstance,
                                                  "Actual_Processor_Binding") ;
            if(bindedVP != null)
            {
              int partitionIndex =
                  bindedVPS.indexOf(bindedVP.get(0).getSubcomponent()) ;
              routingHeaderCode.addOutput(Integer.toString(partitionIndex)) ;
            }
          }
          routingHeaderCode.addOutput(",") ;
        }
        routingHeaderCode.addOutputNewline("}") ;
      }

      routingHeaderCode.addOutputNewline("typedef enum") ;
      routingHeaderCode.addOutputNewline("{") ;
      routingHeaderCode.incrementIndent() ;

      int idx = 0 ;
      for(ComponentInstance node : routeProp.processors)
      {
        routingHeaderCode.addOutput(AadlToPokADAUtils
              .getComponentInstanceIdentifier(node)) ;
        routingHeaderCode.addOutput(" = " + Integer.toString(idx)) ;
        routingHeaderCode.addOutputNewline(",") ;
        idx++ ;
      }
      routingHeaderCode.decrementIndent() ;
      routingHeaderCode.addOutputNewline("} pok_node_identifier_t;") ;

      idx = 0 ;
      routingHeaderCode.addOutputNewline("typedef enum") ;
      routingHeaderCode.addOutputNewline("{") ;
      routingHeaderCode.incrementIndent() ;
      for(FeatureInstance fi : localPorts)
      {
        routingHeaderCode.addOutput(AadlToPokADAUtils
              .getFeatureLocalIdentifier(fi)) ;
        routingHeaderCode.addOutput(" = " + Integer.toString(idx)) ;
        routingHeaderCode.addOutputNewline(",") ;
        idx++ ;
      }
      routingHeaderCode.addOutput("invalid_local_port") ;
      routingHeaderCode.addOutputNewline(" = " + Integer.toString(idx)) ;
      routingHeaderCode.decrementIndent() ;
      routingHeaderCode.addOutputNewline("} pok_port_local_identifier_t;") ;

      idx = 0 ;
      routingHeaderCode.addOutputNewline("typedef enum") ;
      routingHeaderCode.addOutputNewline("{") ;
      routingHeaderCode.incrementIndent() ;
      for(FeatureInstance fi : routeProp.globalPort)
      {
        routingHeaderCode.addOutput(AadlToPokADAUtils
              .getFeatureGlobalIdentifier(fi)) ;
        routingHeaderCode.addOutput(" = " + Integer.toString(idx)) ;
        routingHeaderCode.addOutputNewline(",") ;
        idx++ ;
      }
      routingHeaderCode.decrementIndent() ;
      routingHeaderCode.addOutputNewline("} pok_port_identifier_t;") ;

      // TODO: define buses for distributed use-case
      routingHeaderCode.addOutputNewline("#define POK_CONFIG_NB_BUSES 0") ;
      idx = 0 ;
      routingHeaderCode.addOutputNewline("typedef enum") ;
      routingHeaderCode.addOutputNewline("{") ;
      routingHeaderCode.incrementIndent() ;
      for(ComponentInstance bus : routeProp.buses)
      {
        routingHeaderCode.addOutput(AadlToPokADAUtils
              .getComponentInstanceIdentifier(bus)) ;
        routingHeaderCode.addOutput(" = " + Integer.toString(idx)) ;
        routingHeaderCode.addOutputNewline(",") ;
        idx++ ;
      }
      routingHeaderCode.addOutputNewline("invalid_bus = " +
            Integer.toString(idx)) ;
      routingHeaderCode.decrementIndent() ;
      routingHeaderCode.addOutputNewline("} pok_bus_identifier_t;") ;
    }

    routingHeaderCode.addOutputNewline("#endif") ;
  }

  private void genRoutingImpl(ComponentInstance processor,
                              UnparseText routingImplCode,
                              RoutingProperties routeProp)
                                                      throws GenerationException
  {
    routingImplCode.addOutputNewline("#include \"routing.h\"") ;
    routingImplCode.addOutputNewline("#include \"middleware/port.h\"") ;
    routingImplCode.addOutputNewline("#include <types.h>") ;

    if(routeProp.processPerProcessor.get(processor) == null)
      return ;

    for(ComponentInstance deployedProcess : routeProp.processPerProcessor
          .get(processor))
    {
      // compute list of ports for each partition deployed on "processor"
      String processName = deployedProcess.getSubcomponent().getName() ;
      int nbPorts = routeProp.portPerProcess.get(deployedProcess).size() ;
      routingImplCode.addOutput("uint8_t ") ;
      routingImplCode.addOutput(processName + "_partport[" +
            Integer.toString(nbPorts) + "] = {") ;
      for(FeatureInstance fi : routeProp.portPerProcess.get(deployedProcess))
      {
        routingImplCode.addOutput(AadlToPokADAUtils
              .getFeatureLocalIdentifier(fi)) ;
        routingImplCode.addOutput(",") ;
      }
      routingImplCode.addOutputNewline("};") ;

      // compute list of destination ports for each port of partitions deployed on "processor"
      for(FeatureInstance fi : routeProp.portPerProcess.get(deployedProcess))
      {
        if(fi.getDirection().equals(DirectionType.OUT) ||
              fi.getDirection().equals(DirectionType.IN_OUT))
        {
          List<FeatureInstance> destinations =
                RoutingProperties.getFeatureDestinations(fi) ;
          routingImplCode.addOutput("uint8_t ") ;
          routingImplCode.addOutput(AadlToPokADAUtils
                .getFeatureLocalIdentifier(fi) +
                "_deployment_destinations[" +
                Integer.toString(destinations.size()) + "] = {") ;
          for(FeatureInstance dst : destinations)
          {
            routingImplCode.addOutput(AadlToPokADAUtils
                  .getFeatureGlobalIdentifier(dst)) ;
            routingImplCode.addOutput(",") ;
          }
          routingImplCode.addOutputNewline("};") ;
        }
      }

    }

    List<FeatureInstance> localPorts = getLocalPorts(processor, routeProp) ;

    routingImplCode.addOutput("uint8_t pok_global_ports_to_local_ports"
          + "[POK_CONFIG_NB_GLOBAL_PORTS] = {") ;
    for(FeatureInstance fi : routeProp.globalPort)
    {
      if(localPorts.contains(fi))
        routingImplCode.addOutput(AadlToPokADAUtils
              .getFeatureLocalIdentifier(fi)) ;
      else
        routingImplCode.addOutput("invalid_local_port") ;

      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t pok_local_ports_to_global_ports"
          + "[POK_CONFIG_NB_PORTS] = {") ;
    for(FeatureInstance fi : localPorts)
    {
      routingImplCode.addOutput(AadlToPokADAUtils
            .getFeatureGlobalIdentifier(fi)) ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t pok_ports_nodes"
          + "[POK_CONFIG_NB_GLOBAL_PORTS] = {") ;
    for(FeatureInstance fi : routeProp.globalPort)
    {
      ComponentInstance inst = routeProp.processorPort.get(fi) ;
      routingImplCode.addOutput(AadlToPokADAUtils
            .getComponentInstanceIdentifier(inst)) ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t pok_ports_nb_ports_by_partition"
          + "[POK_CONFIG_NB_PARTITIONS] = {") ;
    for(ComponentInstance deployedProcess : routeProp.processPerProcessor
          .get(processor))
    {
      int nbPort = routeProp.portPerProcess.get(deployedProcess).size() ;
      routingImplCode.addOutput(Integer.toString(nbPort)) ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t* pok_ports_by_partition"
          + "[POK_CONFIG_NB_PARTITIONS] = {") ;
    for(ComponentInstance deployedProcess : routeProp.processPerProcessor
          .get(processor))
    {
      routingImplCode.addOutput(deployedProcess.getSubcomponent().getName() +
            "_partport") ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("char* pok_ports_names"
          + "[POK_CONFIG_NB_PORTS] = {") ;
    for(FeatureInstance fi : localPorts)
    {
      routingImplCode.addOutput("\"" +
            AadlToPokADAUtils.getFeatureLocalIdentifier(fi) + "\"") ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t pok_ports_identifiers"
          + "[POK_CONFIG_NB_PORTS] = {") ;
    for(FeatureInstance fi : localPorts)
    {
      routingImplCode.addOutput("" +
            AadlToPokADAUtils.getFeatureLocalIdentifier(fi) + "") ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t pok_ports_nb_destinations"
          + "[POK_CONFIG_NB_PORTS] = {") ;
    for(FeatureInstance fi : localPorts)
    {
      int destNb = RoutingProperties.getFeatureDestinations(fi).size() ;
      routingImplCode.addOutput(Integer.toString(destNb)) ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t* pok_ports_destinations"
          + "[POK_CONFIG_NB_PORTS] = {") ;
    for(FeatureInstance fi : localPorts)
    {
      int destNb = RoutingProperties.getFeatureDestinations(fi).size() ;
      if(destNb == 0)
        routingImplCode.addOutput("NULL") ;
      else
        routingImplCode.addOutput(AadlToPokADAUtils
              .getFeatureLocalIdentifier(fi) +
              "_deployment_destinations") ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;

    routingImplCode.addOutput("uint8_t pok_ports_kind"
          + "[POK_CONFIG_NB_PORTS] = {") ;
    for(FeatureInstance fi : localPorts)
    {
      if(fi.getCategory().equals(FeatureCategory.DATA_PORT))
        routingImplCode.addOutput("POK_PORT_KIND_SAMPLING") ;
      if(fi.getCategory().equals(FeatureCategory.EVENT_DATA_PORT) ||
            fi.getCategory().equals(FeatureCategory.EVENT_PORT))
        routingImplCode.addOutput("POK_PORT_KIND_QUEUEING") ;
      routingImplCode.addOutput(",") ;
    }
    routingImplCode.addOutputNewline("};") ;
  }
	  
  public static class BlackBoardInfo
  {
    public String id = null ;

    public String dataType = null ;
  }

  public static class QueueInfo
  {
    public String id = null ;

    public String uniqueId = null ;

    public long size = -1 ;

    public String type = null ;

    public String dataType = null ;

    public DirectionType direction = null ;
  }

  public static class SampleInfo
  {
    public String id = null ;

    public String uniqueId = null ;

    public long refresh = -1 ;

    public String dataType = null ;

    public DirectionType direction = null ;
  }

  public static class BufferInfo
  {
    public String id = null ;

    public String uniqueId = null ;

    public long refresh = -1 ;

    public String dataType = null ;

    public DirectionType direction = null ;
  }
}