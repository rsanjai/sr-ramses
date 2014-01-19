package fr.tpt.aadl.ramses.control.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Element;
import org.osate.aadl2.SystemImplementation;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instantiation.InstantiateModel;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.xtext.aadl2.properties.linking.PropertiesLinkingService;

import fr.tpt.aadl.ramses.control.support.services.ServiceRegistry;


public class AadlModelsManagerImpl implements AadlModelsManager {

	private InstantiateModel instantiateModel ;
	private ResourceSet aadlResourceSet = new ResourceSetImpl();
	
	public AadlModelsManagerImpl() {
	  instantiateModel =
		        new InstantiateModel(new NullProgressMonitor(),
		              ServiceRegistry.ANALYSIS_ERR_REPORTER_MANAGER) ;
	}
	
	/**
	 * @see AadlModelsManagerImpl#instantiate(SystemImplementation)
	 */
	@Override
	public SystemInstance instantiate(SystemImplementation si) {
	  URI instanceURI =
	       OsateResourceUtil.getInstanceModelURI(si) ;
	  
	  Resource aadlResource = aadlResourceSet.getResource(instanceURI,
	                                                      false) ;

	  if(aadlResource == null)
	  {
	    aadlResource = aadlResourceSet
	              .createResource(instanceURI) ;
	  }

	  SystemInstance instance = instantiateModel.createSystemInstanceInt(si,
	                                                    aadlResource) ;
		    
	  return instance;
    }

	/**
	 * @see AadlModelsManager#instantiate(List, String)
	 */
	@Override
	public	SystemInstance instantiate(List<Resource> aadlModels,
			String systemToInstantiate) {
	    for(Resource r : aadlModels)
	    {
		  AadlModelsManager instantiator = RamsesConfiguration.getInstantiationManager();
	      PropertiesLinkingService pls = new PropertiesLinkingService ();
	      AadlPackage pkg = (AadlPackage) r.getContents().get(0);
	      SystemImplementation si = (SystemImplementation) pls.
	      		findNamedElementInsideAadlPackage(systemToInstantiate, 
	      				pkg.getOwnedPublicSection());
	      if(si==null)
	    	  continue;
	      return instantiator.instantiate(si);
	    }
	    // TODO: Manage with error reporter
	    System.err.println("ERROR: "+ 
	    			systemToInstantiate +
	    			" could not be found for instantiation.");
		return null;
	  }

	/**
	 * @see AadlModelsManagerImpl#serialize(Resource, String)
	 */
	@Override
	  public void serialize(Resource transfoResult,
	                        String targetFileName)
	  {
	    // serialization code
//	    XtextResource resource = (XtextResource) resourceSet.createResource(URI.createURI(targetFileName));
//	    resource.getContents().add(transfoResult.getContents().get(0));
//	    SaveOptions.Builder sb = SaveOptions.newBuilder();
//	    Map<Object,Object> options = new HashMap<Object,Object>();
//	    sb.getOptions().addTo(options);
//	    try {
//	    	resource.save(options);
//	    } catch (IOException e) {
//	    	e.printStackTrace();
//	    }
		  
//		  Serializer serializer = injector.getInstance(Serializer.class);
//		  String resultFileContent = serializer.serialize(transfoResult.getContents().get(0));
		try {
			transfoResult.load(null);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Aadl2StandaloneUnparser unparser = Aadl2StandaloneUnparser.getAadlUnparser();
		String resultFileContent = unparser.doUnparse((Element) transfoResult.getContents().get(0)) ;
	    try
	    {
	      File clean = new File(targetFileName);
	      clean.delete();
	      BufferedWriter out = new BufferedWriter(new FileWriter(targetFileName)) ;
	      out.write(resultFileContent) ;
	      out.close() ;
	    }
	    catch(IOException e)
	    {
	      // TODO Manage with error reporter
	      e.printStackTrace() ;
	    }

	    java.lang.System.out.println("Serialized result in: " + targetFileName) ;
	  }
}