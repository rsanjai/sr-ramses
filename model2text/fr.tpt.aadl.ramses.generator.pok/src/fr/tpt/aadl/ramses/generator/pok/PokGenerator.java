package fr.tpt.aadl.ramses.generator.pok;

import java.io.File ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import org.eclipse.emf.common.util.TreeIterator ;
import org.eclipse.emf.ecore.EObject ;
import org.eclipse.emf.ecore.resource.Resource ;
import org.osate.aadl2.Element ;
import org.osate.aadl2.ProcessSubcomponent ;
import org.osate.aadl2.ProcessorSubcomponent ;
import org.osate.aadl2.SystemImplementation ;

import fr.tpt.aadl.ramses.control.support.generator.GenerationException ;
import fr.tpt.aadl.ramses.control.support.generator.TargetProperties ;
import fr.tpt.aadl.ramses.control.support.plugins.NamedPlugin ;
import fr.tpt.aadl.ramses.generator.c.AadlToCUnparser ;
import fr.tpt.aadl.ramses.generator.pok.c.AadlToPokCUnparser ;
import fr.tpt.aadl.ramses.generator.pok.makefile.AadlToPokMakefileUnparser ;
import fr.tpt.aadl.ramses.generator.target.specific.GeneratorUtils ;
import fr.tpt.aadl.ramses.instantiation.StandAloneInstantiator ;
import fr.tpt.aadl.ramses.transformation.atl.AtlTransfoLauncher ;

public class PokGenerator implements NamedPlugin
{
  public static final String GENERATED_DIR_NAME = "/generated-code/" ; 
  public static final String KERNEL_DIR_NAME = "/kernel" ;
  
  public final static String GENERATOR_NAME = "pok_generator" ;

  @Override
  public String getRegistryName()
  {
    return GENERATOR_NAME ;
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
  
  // It may be factorized in an abstract class.
  private Resource doAtlTransformation(
                                   Resource inputResource,
                                   Map<String, Resource> standardPropertySets,
                                   List<File> transformationFileName,
                                   String dataTargetfilepath,
                                   String transformationDir,
                                   List<File> postTransformationFiles,
                                   File target_directory) throws GenerationException
  {
    AtlTransfoLauncher atlTransfo = null ;
    
    try
    {
      atlTransfo = new AtlTransfoLauncher() ;
      
      if(transformationDir == null || transformationDir.isEmpty())
      {
        throw new GenerationException("Transformation directory is not set.") ;
      }
      else
      {
        File generatorDirectory = new File(transformationDir) ;
        atlTransfo.setGenerationPathDirectory(generatorDirectory) ;
      }

      Resource expandedResult =
            atlTransfo.doGeneration(inputResource, standardPropertySets,
                                    transformationFileName, dataTargetfilepath) ;

      for(File f : postTransformationFiles)
      {
        List<File> refinements = new ArrayList<File>() ;
        refinements.add(f) ;
        expandedResult =
              atlTransfo.doGeneration(expandedResult, standardPropertySets,
                                     refinements, dataTargetfilepath) ;
      }
      
      // XXX Why an other time ?
      dataTargetfilepath =
            target_directory +
                  inputResource.getURI().toFileString()
                        .substring(inputResource.getURI().toFileString()
                                         .lastIndexOf('/'),
                                         inputResource.getURI().toFileString()
                                         .lastIndexOf(".aaxl2")) + ".aadl2" ;
      
      // DEBUG 
      StandAloneInstantiator instantiator = StandAloneInstantiator.getInstantiator() ;
      instantiator.serialize(expandedResult, dataTargetfilepath) ;
      
      return expandedResult ;
    }
    catch(Exception e)
    {
      throw new GenerationException(e.getMessage()) ;
    }
  }
  
  // TODO parameters and interface design refactoring.
  public Resource doGeneration(Resource inputResource,
                               Map<String, Resource> standardPropertySets,
                               List<File> transformationFileName,
                               String dataTargetfilepath,
                               String transformationDir,
                               List<File> postTransformationFiles,
                               Resource baseTypes,
                               File target_directory) throws GenerationException
  {
    Resource expandedResult = doAtlTransformation(inputResource,
                                                  standardPropertySets,
                                                  transformationFileName,
                                                  dataTargetfilepath,
                                                  transformationDir,
                                                  postTransformationFiles,
                                                  target_directory) ;
    
    
    File generatedCodeDirectory =
            new File(target_directory + "/generated-code") ;
      generatedCodeDirectory.mkdir() ;
//    SystemInstance rootInstance =  (SystemInstance) inputResource.getContents().get(0);
    AadlToPokCUnparser pokCUnparser = new AadlToPokCUnparser();
    
    
//    pokCUnparser.process(rootInstance, generatedCodeDirectory);
    
    TreeIterator<EObject> iter = expandedResult.getAllContents() ;
      
    


/******************************************************************************/
/* TODO:
 * 
 * _ PokGenerator contains the iteration logic for pok code generation. So 
 *     _ PokGenerator have to create directories where files will be saved
 *       without knowing which implementation language is used.
 *     _ Iterate AADL-- tree so as to give to AadlToCUnparser and AadlToPokCUnparser
 *       the AADL objects to unparse, without PokGenerator knowing which
 *       implementation language is used.
 *     _ generate the makefile the same way as above.
 */
/******************************************************************************/
    
    while(iter.hasNext())
    {
      Element elt = (Element) iter.next() ;
      
      if(elt instanceof SystemImplementation)
      {
        SystemImplementation si = (SystemImplementation) elt ;
        AadlToPokMakefileUnparser makefileGenerator =
              new AadlToPokMakefileUnparser() ;
        File generatedFileDir = new File(target_directory + GENERATED_DIR_NAME);
        makefileGenerator.process(si, generatedFileDir) ;
        
        TargetProperties tarProp = pokCUnparser.process(si, generatedFileDir) ;
        
        for(ProcessorSubcomponent ps : si.getOwnedProcessorSubcomponents())
        {
          // create directory with the processor subcomponent name
          File processorFileDir =
                new File(generatedFileDir + "/" + ps.getName()) ;
          if(!processorFileDir.isDirectory())
        	processorFileDir.mkdir() ;
          
          makefileGenerator.process(ps, processorFileDir) ;
          
          File kernelFileDir = new File(processorFileDir + KERNEL_DIR_NAME) ;
          
          pokCUnparser.process(ps, kernelFileDir, tarProp);
          List<ProcessSubcomponent> ownedProcess = 
                                        GeneratorUtils.getBindedProcesses(ps) ;
          
          for(ProcessSubcomponent process : ownedProcess)
          {
            String generationTargetDirectory = processorFileDir +
                                               "/" + process.getName() ;
            File processDirectory = new File(generationTargetDirectory) ;
            processDirectory.mkdir() ;
            
            AadlToCUnparser processC_Gen = new AadlToCUnparser() ;
            processC_Gen.process(process, processDirectory) ;
            makefileGenerator.process(process, processDirectory) ;
            
//            ProcessImplementation processImpl = (ProcessImplementation)
//                                          process.getComponentImplementation() ;
            pokCUnparser.process(process, processDirectory, tarProp);
          }
        }
      }
    }  

    // DEBUG
    return null ;
  }
}