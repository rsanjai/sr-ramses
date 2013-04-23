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

package fr.tpt.aadl.ramses.generation.c ;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.AccessCategory;
import org.osate.aadl2.AccessConnection;
import org.osate.aadl2.AccessType;
import org.osate.aadl2.AnnexLibrary;
import org.osate.aadl2.AnnexSubclause;
import org.osate.aadl2.BehavioredImplementation;
import org.osate.aadl2.CallSpecification;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ClassifierValue;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentPrototypeActual;
import org.osate.aadl2.ComponentPrototypeBinding;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.ConnectedElement;
import org.osate.aadl2.Connection;
import org.osate.aadl2.ConnectionEnd;
import org.osate.aadl2.DataAccess;
import org.osate.aadl2.DataImplementation;
import org.osate.aadl2.DataPrototype;
import org.osate.aadl2.DataSubcomponent;
import org.osate.aadl2.DataSubcomponentType;
import org.osate.aadl2.DataType;
import org.osate.aadl2.DefaultAnnexLibrary;
import org.osate.aadl2.Element;
import org.osate.aadl2.EnumerationLiteral;
import org.osate.aadl2.Feature;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.NamedValue;
import org.osate.aadl2.Parameter;
import org.osate.aadl2.ProcessImplementation;
import org.osate.aadl2.ProcessSubcomponent;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.PrototypeBinding;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.SubcomponentType;
import org.osate.aadl2.SubprogramAccess;
import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.aadl2.SubprogramClassifier;
import org.osate.aadl2.SubprogramGroupAccess;
import org.osate.aadl2.SubprogramImplementation;
import org.osate.aadl2.SubprogramSubcomponent;
import org.osate.aadl2.SubprogramSubcomponentType;
import org.osate.aadl2.SubprogramType;
import org.osate.aadl2.ThreadImplementation;
import org.osate.aadl2.ThreadSubcomponent;
import org.osate.aadl2.ThreadType;
import org.osate.aadl2.modelsupport.modeltraversal.AadlProcessingSwitch;
import org.osate.aadl2.util.Aadl2Switch;
import org.osate.annexsupport.AnnexUnparser;

import fr.tpt.aadl.annex.behavior.AadlBaParserAction;
import fr.tpt.aadl.annex.behavior.AadlBaUnParserAction;
import fr.tpt.aadl.annex.behavior.analyzers.TypeHolder;
import fr.tpt.aadl.annex.behavior.utils.AadlBaUtils;
import fr.tpt.aadl.annex.behavior.utils.DimensionException;
import fr.tpt.aadl.ramses.control.support.generator.AadlGenericUnparser;
import fr.tpt.aadl.ramses.control.support.generator.GenerationException;
import fr.tpt.aadl.ramses.control.support.services.ServiceRegistryProvider;
import fr.tpt.aadl.ramses.generation.c.annex.behavior.AadlBaToCUnparser;
import fr.tpt.aadl.ramses.generation.c.annex.behavior.AadlBaToCUnparserAction;
import fr.tpt.aadl.ramses.generation.target.specific.GeneratorUtils;
import fr.tpt.aadl.utils.Aadl2Utils;
import fr.tpt.aadl.utils.PropertyUtils;
import fr.tpt.aadl.utils.names.DataModelProperties;

public class AadlToCUnparser extends AadlProcessingSwitch
                             implements AadlGenericUnparser
{
  private static AadlToCUnparser singleton;
  
  // gtype.c and .h
  protected AadlToCSwitchProcess _gtypesImplCode ;
  protected AadlToCSwitchProcess _gtypesHeaderCode ;

  // subprogram.c and .h
  protected AadlToCSwitchProcess _subprogramImplCode ;
  protected AadlToCSwitchProcess _subprogramHeaderCode ;
  
  // activity.c and .h
  protected AadlToCSwitchProcess _activityImplCode ;
  protected AadlToCSwitchProcess _activityHeaderCode ;

  // partition's deployment.c and .h
  protected AadlToCSwitchProcess _deploymentImplCode ;
  protected AadlToCSwitchProcess _deploymentHeaderCode ;
  
  // Temporary .c and .h files.
  private AadlToCSwitchProcess _currentImplUnparser ;
  private AadlToCSwitchProcess _currentHeaderUnparser ;

  private Map<AadlToCSwitchProcess, Set<String>> _additionalHeaders ;

  private List<String> _processedTypes  ;

  public List<NamedElement> delayedUnparsing = new ArrayList<NamedElement>();
  
  private static final String MAIN_HEADER_INCLUSION = "#include \"main.h\"\n" ;
  // Map Data Access with their relative Data Subcomponent. Relations 
  // are defined in the process implementation via connections.
  private Map<DataAccess, String> _dataAccessMapping = new HashMap<DataAccess, String>();
  
  /**
   * Stack of subprogram classifiers currently uunparsed.
   * Used to resolve prototype dependencies between subprogram calls.
   */
  private List<SubprogramClassifier> subprogramsUnparsingStack = new ArrayList<SubprogramClassifier>();
  
  public static AadlToCUnparser getAadlToCUnparser()
  {
    if(singleton==null)
      singleton = new AadlToCUnparser();
    return singleton;
  }
  
  private AadlToCUnparser()
  {
    super() ;
    init() ;
  }
  
  private void init()
  {
    _gtypesImplCode = new AadlToCSwitchProcess(this) ;
    _gtypesImplCode.addOutputNewline("#include \"gtypes.h\"") ;
    
    _gtypesHeaderCode = new AadlToCSwitchProcess(this) ;
    
    _subprogramImplCode = new AadlToCSwitchProcess(this) ;
    _subprogramImplCode.addOutputNewline("#include \"subprograms.h\"") ;
    
    _subprogramHeaderCode = new AadlToCSwitchProcess(this) ;
    
    _subprogramHeaderCode.addOutputNewline("#include \"gtypes.h\"") ;    
    
    _activityImplCode = new AadlToCSwitchProcess(this) ;
    _activityImplCode.addOutputNewline("#include \"activity.h\"") ;
    _activityImplCode.addOutputNewline(MAIN_HEADER_INCLUSION);
    
    _activityHeaderCode = new AadlToCSwitchProcess(this) ;
    _activityHeaderCode.addOutputNewline("#include \"subprograms.h\"") ;
        
    _deploymentImplCode = new AadlToCSwitchProcess(this) ;
    _deploymentImplCode.addOutputNewline("#include \"deployment.h\"") ;
    
    _deploymentHeaderCode = new AadlToCSwitchProcess(this) ;
    _deploymentHeaderCode.addOutputNewline("#include \"gtypes.h\"") ;
    
    _processedTypes = new ArrayList<String>() ;
    
    _additionalHeaders = new HashMap<AadlToCSwitchProcess, Set<String>>() ;
    
  }
  
  public List<PrototypeBinding> getCurrentPrototypeBindings(String ctxt)
  {
	  System.out.println("Inherited prototype bindings for " + ctxt);
	  
	  List<PrototypeBinding> bindings = new ArrayList<PrototypeBinding>();
	  for(SubprogramClassifier c: subprogramsUnparsingStack)
	  {
		  System.out.println("  prototype bindings from " + c.getName());
		  List<PrototypeBinding> cBindings = c.getOwnedPrototypeBindings();
		  for(PrototypeBinding b : cBindings)
		  {
			  ComponentPrototypeBinding cpb = (ComponentPrototypeBinding) b;
			  SubcomponentType st = cpb.getActuals().get(0).getSubcomponentType();
			  System.out.println("    prototype binding " + b.getFormal().getName() + " => " + st.getName());
		  }
		  
		  bindings.addAll(cBindings);
	  }
	  return bindings;
  }
  
  public void saveGeneratedFilesContent(File targetDirectory)
  {

    _gtypesHeaderCode.addOutputNewline("\n#endif\n") ;
    _subprogramHeaderCode.addOutputNewline("\n#endif\n") ;
    _activityHeaderCode.addOutputNewline("\n#endif\n") ;
    _deploymentHeaderCode.addOutputNewline("\n#endif\n") ;

    try
    {
      String headerGuard = null ;
      
      // gtypes.c
      FileWriter typesFile_C =
            new FileWriter(targetDirectory.getAbsolutePath() + "/gtypes.c") ;
      String addTypeHeader_C = getAdditionalHeader(_gtypesImplCode) ;
      saveFile(typesFile_C, addTypeHeader_C, _gtypesImplCode.getOutput()) ;
      
      // gtypes.h
      FileWriter typesFile_H =
            new FileWriter(targetDirectory.getAbsolutePath() + "/gtypes.h") ;
      headerGuard = GenerationUtilsC.generateHeaderInclusionGuard("gtypes.h") ;
      String addTypeHeader_H = getAdditionalHeader(_gtypesHeaderCode) ;
      saveFile(typesFile_H, headerGuard, addTypeHeader_H,
               _gtypesHeaderCode.getOutput()) ;
      
      // subprogram.c
      FileWriter subprogramsFile_C =
            new FileWriter(targetDirectory.getAbsolutePath() + "/subprograms.c") ;
      String addSubprogramHeader_C = getAdditionalHeader(_subprogramImplCode) ;
      saveFile(subprogramsFile_C, addSubprogramHeader_C,
                     _subprogramImplCode.getOutput()) ;
      
      // subprogram.h
      FileWriter subprogramsFile_H =
            new FileWriter(targetDirectory.getAbsolutePath() + "/subprograms.h") ;
      headerGuard = GenerationUtilsC.generateHeaderInclusionGuard("subprograms.h");
      String addSubprogramsHeader_H = getAdditionalHeader(_subprogramHeaderCode) ;
      saveFile(subprogramsFile_H, headerGuard, addSubprogramsHeader_H,
                     _subprogramHeaderCode.getOutput()) ;
      
      // activity.c     
      FileWriter activityFile_C =
            new FileWriter(targetDirectory.getAbsolutePath() + "/activity.c") ;
      String addActivityHeader_C = getAdditionalHeader(_activityImplCode) ;
      saveFile(activityFile_C, addActivityHeader_C, _activityImplCode.getOutput()) ;
      
      // activity.h
      FileWriter activityFile_H =
            new FileWriter(targetDirectory.getAbsolutePath() + "/activity.h") ;
      headerGuard = GenerationUtilsC.generateHeaderInclusionGuard("activity.h");
      String addActivityHeader_H = getAdditionalHeader(_activityHeaderCode) ;
      saveFile(activityFile_H, headerGuard,
               addActivityHeader_H, _activityHeaderCode.getOutput()) ;
      
      // partition's deployment.c
      FileWriter deploymentFile_C =
            new FileWriter(targetDirectory.getAbsolutePath() + "/deployment.c") ;
      String addDeploymentHeader_C = getAdditionalHeader(_deploymentImplCode) ;
      saveFile(deploymentFile_C, addDeploymentHeader_C,
                     _deploymentImplCode.getOutput()) ;
      
      
      // partition's deployment.h
      FileWriter deploymentFile_H =
            new FileWriter(targetDirectory.getAbsolutePath() + "/deployment.h") ;
      headerGuard = GenerationUtilsC.generateHeaderInclusionGuard("deployment.h");
      String addDeploymentHeader_H = getAdditionalHeader(_deploymentHeaderCode) ;
      saveFile(deploymentFile_H, headerGuard, MAIN_HEADER_INCLUSION,
               addDeploymentHeader_H, _deploymentHeaderCode.getOutput()) ;
      
      clean();

    }
    catch(IOException e)
    {
      // TODO: handle error message.
      e.printStackTrace() ;
    }
  }

  private void clean() {
	this._additionalHeaders.clear();
	this._dataAccessMapping.clear();
  }

  private String getAdditionalHeader(AadlToCSwitchProcess fileUnparser)
  {
    StringBuffer res = new StringBuffer("") ;

    if(_additionalHeaders.containsKey(fileUnparser))
    {
      Set<String> additionalTypeHeaders = _additionalHeaders.get(fileUnparser) ;

      for(String s : additionalTypeHeaders)
      {
        res.append("#include \"" + s + "\"\n") ;
      }
      additionalTypeHeaders.clear();
    }

    return res.toString() ;
  }

  private void saveFile(FileWriter file,
                        String ... content)
  {
    BufferedWriter output ;
    StringBuilder sb = new StringBuilder() ;
    
    for(String s : content)
    {
      sb.append(s) ;
    }
    
    try
    {
      output = new BufferedWriter(file) ;
      
      output.write(sb.toString()) ;
      
      output.close() ;
    }
    catch(IOException e)
    {
      // TODO: handle error message.
      e.printStackTrace() ;
    }
  }

  public boolean resolveExistingCodeDependencies(NamedElement object,
          AadlToCSwitchProcess sourceNameDest,
          AadlToCSwitchProcess sourceTextDest) throws Exception
  {
	Set<String> l;
	if(_additionalHeaders.containsKey(sourceTextDest) == false)
	{
	  l = new HashSet<String>() ;
	  _additionalHeaders.put(sourceTextDest, l) ;
	}
	else
	{
	  l = _additionalHeaders.get(sourceTextDest) ;
	}
	String sourceName = GenerationUtilsC.resolveExistingCodeDependencies(object, l);
	if(sourceName!=null)
	{
	  if(sourceNameDest!=null)
		sourceNameDest.addOutput(sourceName);
	  return true;
	}
	else
		throw new Exception("In component "+object.getName()+": Source_Text " +
	      		"property should also reference a header (.h extension) file");
  }
  
  protected void processDataSubcomponentType(DataSubcomponentType dst,
		  AadlToCSwitchProcess sourceNameDest, 
		  AadlToCSwitchProcess sourceTextDest)
  {
	  processDataSubcomponentType(null, dst, sourceNameDest, sourceTextDest);
  }
  
  protected void processDataSubcomponentType(Classifier owner,
		  DataSubcomponentType dst,
		  AadlToCSwitchProcess sourceNameDest, 
		  AadlToCSwitchProcess sourceTextDest)
  {

    try
    {
      resolveExistingCodeDependencies(dst, sourceNameDest, sourceTextDest) ;
    }
    catch(Exception e)
    {
      if(dst instanceof DataPrototype && owner!=null)
      {
    	for(PrototypeBinding pb: owner.getOwnedPrototypeBindings())
    	{
    	  if(pb instanceof ComponentPrototypeBinding
    			&& pb.getFormal().getName().equalsIgnoreCase(dst.getName()))
    	  {
  		    for(ComponentPrototypeActual pa: ((ComponentPrototypeBinding) pb).getActuals())
    		{
    		  if(pa.getSubcomponentType() instanceof DataSubcomponentType)
    	      {
    		    DataSubcomponentType dstActual = (DataSubcomponentType) pa.getSubcomponentType();
    		    processDataSubcomponentType(owner, dstActual, sourceNameDest, sourceTextDest);
    	        break;
    	      }
    	    }
    	  }
    	}
      } else
    	sourceNameDest.addOutput(GenerationUtilsC.getGenerationCIdentifier(dst
    	  .getQualifiedName())) ;
    }
  }
  
  boolean processBehavioredType(ComponentType type)
  {
	  return processBehavioredType(type, type);
  }
  
  boolean processBehavioredType(ComponentType type, ComponentType owner)
  {
	  boolean foundRestrictedAnnex = false;
	  for(AnnexSubclause annex : type.getOwnedAnnexSubclauses())
      {
        if(annex.getName().equalsIgnoreCase(AadlBaParserAction.ANNEX_NAME))
        {
        	foundRestrictedAnnex = true;
        	processAnnexSubclause(annex, owner) ;
        	break;
        }
      }
	  if(!foundRestrictedAnnex && type.getExtended()!=null)
		  foundRestrictedAnnex = processBehavioredType(type.getExtended(), owner);
	  return foundRestrictedAnnex;
  }
  
  /**
   * unparses annex subclause
   *
   * @param as
   *            AnnexSubclause object
   */
  public String processAnnexSubclause(AnnexSubclause as, NamedElement owner)
  {
	AadlToCSwitchProcess codeUnparser;
	AadlToCSwitchProcess headerUnparser;
	if(owner instanceof SubprogramClassifier)
	{
	  codeUnparser = _subprogramImplCode;
	  headerUnparser = _subprogramHeaderCode;
	}
	else
	{
	  codeUnparser = _activityImplCode;
	  headerUnparser = _subprogramHeaderCode;
	}
    String annexName = as.getName() ;
    	if(annexName.equalsIgnoreCase(AadlBaUnParserAction.ANNEX_NAME))
    		annexName = "restricted_"+annexName;
    AnnexUnparser unparser =
          ServiceRegistryProvider.getServiceRegistry()
                .getUnparser(annexName) ;
    
    // XXX May AadlBaToCUnparser have its own interface ???
    if(unparser != null && unparser instanceof AadlBaToCUnparserAction )
    {
      AadlBaToCUnparserAction baToCUnparserAction =
            (AadlBaToCUnparserAction) unparser ;
      
      AadlBaToCUnparser baToCUnparser =
            (AadlBaToCUnparser) baToCUnparserAction.getUnparser() ;
      
      baToCUnparser.setDataAccessMapping(_dataAccessMapping) ;
      baToCUnparser.setOwner(owner);
      
      baToCUnparserAction.unparseAnnexSubclause(as,
    		  codeUnparser.getIndent()) ;
      
      
      
      baToCUnparser.addIndent_C(codeUnparser.getIndent()) ;
      baToCUnparser.addIndent_H(headerUnparser.getIndent()) ;
      codeUnparser.addOutput(baToCUnparser.getCContent()) ;
      headerUnparser.addOutput(baToCUnparser.getHContent()) ;

      if(_additionalHeaders.get(headerUnparser) == null)
      {
        Set<String> t = new HashSet<String>() ;
        _additionalHeaders.put(headerUnparser, t) ;
      }

      _additionalHeaders.get(headerUnparser)
            .addAll(baToCUnparser.getAdditionalHeaders()) ;
      baToCUnparser.getAdditionalHeaders().clear();
    }
    
    if (owner instanceof SubprogramType)
    {
    	subprogramsUnparsingStack.remove(subprogramsUnparsingStack.size()-1);
    }

    return DONE ;
  }
  
  boolean processBehavioredImplementation(BehavioredImplementation object)
  {
	  return processBehavioredImplementation(object, object);
  }
  
  public boolean processBehavioredImplementation(BehavioredImplementation object, BehavioredImplementation owner)
  {
	  boolean foundRestrictedAnnex = false;
	  for(AnnexSubclause annex : object.getOwnedAnnexSubclauses())
      {
        if(annex.getName().equalsIgnoreCase(AadlBaUnParserAction.ANNEX_NAME))
        {
        	foundRestrictedAnnex = true;
        	processAnnexSubclause(annex, owner) ;
        	break;
        }
      }
	  if(!foundRestrictedAnnex && !object.getOwnedSubprogramCallSequences().isEmpty())
	  {
	  	for(SubprogramCallSequence scs: object.getOwnedSubprogramCallSequences())
	  	{
	  		process(scs);
	  	}
	  	foundRestrictedAnnex=true;
	  }
	  if(!foundRestrictedAnnex && object.getExtended()!=null)
		  foundRestrictedAnnex = processBehavioredImplementation((BehavioredImplementation)object.getExtended(), object);
	  return foundRestrictedAnnex;
  }
  
  
  protected void getCTypeDeclarator(NamedElement object)
  {
    
    String id =
          GenerationUtilsC.getGenerationCIdentifier(object.getQualifiedName()) ;
    TypeHolder dataTypeHolder = null ;

    try
    {
      dataTypeHolder = AadlBaUtils.getTypeHolder(object) ;
    }
    catch(DimensionException e)
    {
      // TODO: handle error message.
      e.printStackTrace() ;
    }

    EList<PropertyExpression> numberRepresentation =
          PropertyUtils
                .getPropertyExpression(dataTypeHolder.klass,
                                       DataModelProperties.NUMBER_REPRESENTATION) ;
    String numberRepresentationValue = "" ;

    for(PropertyExpression n : numberRepresentation)
    {
      if(n instanceof NamedValue)
      {
        NamedValue el = (NamedValue) n ;
        numberRepresentationValue =
              ((EnumerationLiteral) el.getNamedValue()).getName().toLowerCase() ;
        break;
      }
    }

    // define types the current data type depends on
    EList<PropertyExpression> referencedBaseType =
          PropertyUtils
                .getPropertyExpression(dataTypeHolder.klass,
                                       DataModelProperties.BASE_TYPE) ;

    for(PropertyExpression baseTypeProperty : referencedBaseType)
    {
      if(baseTypeProperty instanceof ListValue)
      {
        ListValue lv = (ListValue) baseTypeProperty ;

        for(PropertyExpression v : lv.getOwnedListElements())
        {
          if(v instanceof ClassifierValue)
          {
            ClassifierValue cv = (ClassifierValue) v ;
            if(_processedTypes.contains(cv.getClassifier().getQualifiedName())==false && cv.getClassifier() instanceof DataSubcomponentType)
            {
              getCTypeDeclarator(cv.getClassifier());
              _processedTypes.add(cv.getClassifier().getQualifiedName());
            }
          }
        }
      }
    }
    
    switch ( dataTypeHolder.dataRep )
    {
    // Simple types
      case BOOLEAN :
        _gtypesHeaderCode.addOutputNewline("typedef char " + id + ";") ;
        break ;
      case CHARACTER :
      {
    	  _gtypesHeaderCode.addOutput("typedef ") ;
    	  _gtypesHeaderCode.addOutputNewline(numberRepresentationValue + " char " +
              id + ";") ;
        break ;
      }
      case FIXED :
        break ;
      case FLOAT :
    	  _gtypesHeaderCode.addOutputNewline("typedef float " + id + ";") ;
        break ;
      case INTEGER :
      {
    	_gtypesHeaderCode.addOutput("typedef ") ;
    	_gtypesHeaderCode.addOutputNewline(numberRepresentationValue + " int " +
              id + ";") ;
        break ;
      }
      case STRING :
        _gtypesHeaderCode.addOutputNewline("typedef char * " + id + ";") ;
        break ;
      // Complex types
      case ENUM :
      {
    	StringBuilder enumDeclaration = new StringBuilder("typedef enum e_" + id + " {\n");
        List<String> stringifiedRepresentation = new ArrayList<String>() ;
        EList<PropertyExpression> dataRepresentation =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.REPRESENTATION) ;

        for(PropertyExpression representationProperty : dataRepresentation)
        {
          if(representationProperty instanceof ListValue)
          {
            ListValue lv = (ListValue) representationProperty ;

            for(PropertyExpression v : lv.getOwnedListElements())
            {
              if(v instanceof StringLiteral)
              {
                StringLiteral enumString = (StringLiteral) v ;
                stringifiedRepresentation.add(enumString.getValue()) ;
              }
            }
          }
        }

        EList<PropertyExpression> dataEnumerators =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.ENUMERATORS) ;

        for(PropertyExpression enumeratorProperty : dataEnumerators)
        {
          if(enumeratorProperty instanceof ListValue)
          {
            ListValue lv = (ListValue) enumeratorProperty ;
            Iterator<PropertyExpression> it =
                  lv.getOwnedListElements().iterator() ;

            while(it.hasNext())
            {
              PropertyExpression v = it.next() ;

              if(v instanceof StringLiteral)
              {
                StringLiteral enumString = (StringLiteral) v ;
                String rep = "" ;

                if(stringifiedRepresentation.isEmpty() == false)
                {
                  rep =
                        " = " +
                              stringifiedRepresentation.get(lv
                                    .getOwnedListElements().indexOf(v)) ;
                }

                if(it.hasNext())
                {
                  rep += "," ;
                }
                enumDeclaration.append("\t"+id + "_" +
                        enumString.getValue() + rep+"\n");
              }
            }
          }
        }

        _gtypesHeaderCode.addOutput(enumDeclaration.toString()) ;
        _gtypesHeaderCode.addOutputNewline("} " + id + ";") ;
        break ;
      }
      case STRUCT :
      {
    	  
        StringBuilder structDefinition = new StringBuilder("typedef struct " + id + " {\n");
        EList<PropertyExpression> elementNames =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.ELEMENT_NAMES) ;
        if(elementNames.isEmpty()==false)
        {
          List<String> stringifiedElementNames = new ArrayList<String>() ;

          for(PropertyExpression elementNameProperty : elementNames)
          {
            if(elementNameProperty instanceof ListValue)
            {
              ListValue lv = (ListValue) elementNameProperty ;

              for(PropertyExpression v : lv.getOwnedListElements())
              {
                if(v instanceof StringLiteral)
                {
                  StringLiteral eltName = (StringLiteral) v ;
                  stringifiedElementNames.add(eltName.getValue()) ;
                }
              }
            }
          }

          EList<PropertyExpression> elementTypes =
                PropertyUtils
                      .getPropertyExpression(dataTypeHolder.klass,
                                             DataModelProperties.BASE_TYPE) ;

          for(PropertyExpression elementTypeProperty : elementTypes)
          {
        	if(elementTypeProperty instanceof ListValue)
        	{
              ListValue lv = (ListValue) elementTypeProperty ;

              for(PropertyExpression v : lv.getOwnedListElements())
              {
                if(v instanceof ClassifierValue)
                {
                  ClassifierValue cv = (ClassifierValue) v ;
                  String type =
                        GenerationUtilsC.getGenerationCIdentifier(cv
                              .getClassifier().getQualifiedName()) ;
                  structDefinition.append("\t"+type +
                        " " +
                        stringifiedElementNames.get(lv.getOwnedListElements()
                              .indexOf(v)) + ";\n") ;
                }
              }
            }
          }
        }
        else
        {
        	if(object instanceof DataImplementation)
        	{
        		for(DataSubcomponent ds:((DataImplementation)object).getOwnedDataSubcomponents())
        		{
        			DataSubcomponentType dst = ds.getDataSubcomponentType();
        			Set<String> l;
        			if(_additionalHeaders.containsKey(_gtypesHeaderCode) == false)
        			{
        				l = new HashSet<String>() ;
        				_additionalHeaders.put(_gtypesHeaderCode, l) ;
        			}
        			else
        			{
        				l = _additionalHeaders.get(_gtypesHeaderCode) ;
        			}
        			String sourceName = GenerationUtilsC.resolveExistingCodeDependencies(dst, l);
        			if(sourceName!=null)
        				structDefinition.append("\t"+sourceName);
        			else
        			{
        				process(dst);
        				sourceName = GenerationUtilsC.getGenerationCIdentifier(dst.getQualifiedName());
        				structDefinition.append("\t"+sourceName);
        			}
        			structDefinition.append(" "+ds.getName()+";\n");
        		}
        	}
        }
        _gtypesHeaderCode.addOutput(structDefinition.toString());
        _gtypesHeaderCode.addOutputNewline("} " + id + ";") ;
        break ;
      }
      case UNION :
      {
    	StringBuilder unionDeclaration = new StringBuilder("typedef union " + id + " {\n");
        EList<PropertyExpression> elementNames =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.ELEMENT_NAMES) ;
        List<String> stringifiedElementNames = new ArrayList<String>() ;

        for(PropertyExpression elementNameProperty : elementNames)
        {
          if(elementNameProperty instanceof ListValue)
          {
            ListValue lv = (ListValue) elementNameProperty ;

            for(PropertyExpression v : lv.getOwnedListElements())
            {
              if(v instanceof StringLiteral)
              {
                StringLiteral eltName = (StringLiteral) v ;
                stringifiedElementNames.add(eltName.getValue()) ;
              }
            }
          }
        }

        EList<PropertyExpression> elementTypes =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.BASE_TYPE) ;

        for(PropertyExpression elementTypeProperty : elementTypes)
        {
          if(elementTypeProperty instanceof ListValue)
          {
            ListValue lv = (ListValue) elementTypeProperty ;

            for(PropertyExpression v : lv.getOwnedListElements())
            {
              if(v instanceof ClassifierValue)
              {
                ClassifierValue cv = (ClassifierValue) v ;
                String type =
                      GenerationUtilsC.getGenerationCIdentifier(cv
                            .getClassifier().getQualifiedName()) ;
                unionDeclaration.append("\t"+type +
                      " " +
                      stringifiedElementNames.get(lv.getOwnedListElements()
                            .indexOf(v)) + ";\n") ;
              }
            }
          }
        }
        _gtypesHeaderCode.addOutput(unionDeclaration.toString());
        _gtypesHeaderCode.addOutputNewline("}" + id + ";") ;
        break ;
      }
      case ARRAY :
      {
    	StringBuilder arrayDef = new StringBuilder ("typedef ");
        EList<PropertyExpression> baseType =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.BASE_TYPE) ;
        boolean found = false;
        for(PropertyExpression baseTypeProperty : baseType)
        {
          if(baseTypeProperty instanceof ListValue)
          {
            ListValue lv = (ListValue) baseTypeProperty ;

            for(PropertyExpression v : lv.getOwnedListElements())
            {
              if(v instanceof ClassifierValue)
              {
                ClassifierValue cv = (ClassifierValue) v ;
               	if(false == _processedTypes.contains(cv.getClassifier().getQualifiedName()))
               	{
                  getCTypeDeclarator(cv.getClassifier());
               	  _processedTypes.add(cv.getClassifier().getQualifiedName());
               	}
               	arrayDef.append(GenerationUtilsC
                      .getGenerationCIdentifier(cv.getClassifier()
                            .getQualifiedName())) ;
                found=true;
                break;
              }
            }
            if(found)
            	break;
          }
        }
        _gtypesHeaderCode.addOutput(arrayDef.toString());
        _gtypesHeaderCode.addOutput(" ") ;
        _gtypesHeaderCode.addOutput(id) ;
        EList<PropertyExpression> arrayDimensions =
              PropertyUtils
                    .getPropertyExpression(dataTypeHolder.klass,
                                           DataModelProperties.DIMENSION) ;

        for(PropertyExpression dimensionProperty : arrayDimensions)
        {
          if(dimensionProperty instanceof ListValue)
          {
            ListValue lv = (ListValue) dimensionProperty ;

            for(PropertyExpression v : lv.getOwnedListElements())
            {
              if(v instanceof IntegerLiteral)
              {
            	_gtypesHeaderCode.addOutput("[") ;
                IntegerLiteral dimension = (IntegerLiteral) v ;
                _gtypesHeaderCode.addOutput(Long.toString(dimension.getValue())) ;
                _gtypesHeaderCode.addOutput("]") ;
              }
            }
          }
        }

        _gtypesHeaderCode.addOutputNewline(";") ;
        break ;
      }
      case UNKNOWN :
      {
        try
        {
          _gtypesHeaderCode.addOutput("typedef ") ;
          resolveExistingCodeDependencies(object, _gtypesHeaderCode,
                                          _gtypesHeaderCode) ;
          _gtypesHeaderCode.addOutput(" ") ;
          _gtypesHeaderCode.addOutput(id) ;
          _gtypesHeaderCode.addOutputNewline(";") ;
        }
        catch(Exception e)
        {
          return ;
        }

        break ;
      }
    }
  }

  @Override
  protected void initSwitches()
  {
    aadl2Switch = new Aadl2Switch<String>()
    {
      @Override
      public String caseDataType(DataType object)
      {
        if(_processedTypes.contains(object.getQualifiedName()))
        {
          return DONE ;
        }
        _processedTypes.add(object.getQualifiedName());
        _gtypesHeaderCode.processComments(object) ;
        getCTypeDeclarator((NamedElement) object) ;
        return DONE ;
      }

      public String caseAadlPackage(AadlPackage object)
      {
        process(object.getOwnedPublicSection()) ;
        process(object.getOwnedPrivateSection()) ;
        return DONE ;
      }

      /**
       * unparses annex library
       *
       * @param al
       *            AnnexLibrary object
       */
      public String caseAnnexLibrary(AnnexLibrary al)
      {
        String annexName = al.getName() ;
        AnnexUnparser unparser =
              ServiceRegistryProvider.getServiceRegistry()
                    .getUnparser(annexName) ;

        if(unparser != null)
        {
          unparser.unparseAnnexLibrary(al, _currentImplUnparser.getIndent()) ;
        }

        return DONE ;
      }

      /**
       * unparses default annex library
       *
       * @param dal
       *            DefaultAnnexLibrary object
       */
      public String caseDefaultAnnexLibrary(DefaultAnnexLibrary dal)
      {
        AnnexUnparser unparser =
              ServiceRegistryProvider.getServiceRegistry().getUnparser("*") ;

        if(unparser != null)
        {
        }

        return DONE ;
      }

      public String caseDataImplementation(DataImplementation object)
      {
    	if(_processedTypes.contains(object.getQualifiedName()))
        {
          return DONE ;
        }
        _processedTypes.add(object.getQualifiedName());
        _currentHeaderUnparser = _gtypesHeaderCode ;
        _gtypesHeaderCode.processComments(object) ;
        getCTypeDeclarator((NamedElement) object) ;
        return null ;
      }

      public String caseDataSubcomponent(DataSubcomponent object)
      {
        AadlToCSwitchProcess unparser ;

        if(object.getContainingComponentImpl() instanceof DataImplementation)
        {
          unparser = _currentHeaderUnparser ;
        }
        else
        {
          unparser = _currentImplUnparser ;
        }

        unparser.processComments(object) ;
        DataSubcomponentType dst = object.getDataSubcomponentType() ;

        processDataSubcomponentType(dst, unparser, _currentHeaderUnparser);

        unparser.addOutput(" ") ;
        unparser.addOutput(GenerationUtilsC.getGenerationCIdentifier(object
              .getQualifiedName())) ;
        unparser.addOutput(GeneratorUtils.getInitialValue(object)) ;
        unparser.addOutputNewline(";") ;

        if(_processedTypes.contains(object.getDataSubcomponentType().getQualifiedName()) == false)
        {
          _processedTypes.add(object.getQualifiedName());
          process(object.getDataSubcomponentType());
        }

        return DONE ;
      }
            
      public String caseProcessImplementation(ProcessImplementation object)
      {
        buildDataAccessMapping(object) ;
        
        processEList(object.getOwnedThreadSubcomponents()) ;
        
        _currentImplUnparser = _deploymentImplCode;
        List<String> dataSubcomponentNames = new ArrayList<String>();
        Map<String, DataSubcomponent> dataSubcomponentMapping = new HashMap<String, DataSubcomponent>();
        for(DataSubcomponent ds: object.getOwnedDataSubcomponents())
        {
          dataSubcomponentNames.add(ds.getName());
          if(false == _dataAccessMapping.containsValue(ds.getName()))
          {
        	  processDataSubcomponentType(ds.getDataSubcomponentType(), _deploymentImplCode, _deploymentHeaderCode);
        	  _deploymentImplCode.addOutput(" "+ds.getName()) ;
        	  _deploymentImplCode.addOutput(GeneratorUtils.getInitialValue(ds)) ;
        	  _deploymentImplCode.addOutputNewline(";") ;
          }
          else
          {
        	  dataSubcomponentMapping.put(ds.getName(), ds);
          }
          process(ds.getDataSubcomponentType());
        }
        // *** Generate deployment.c ***
        if(false == _dataAccessMapping.isEmpty())
        {
          List<String> treatedDeclarations = new ArrayList<String>();
          for(DataAccess d : _dataAccessMapping.keySet())
          {
        	if(treatedDeclarations.contains(_dataAccessMapping.get(d)))
        	{
        		continue;
        	}
        	else if(dataSubcomponentNames.contains(_dataAccessMapping.get(d)))
        	{
        	  DataSubcomponentType dst = d.getDataFeatureClassifier();
        	  String declarationID = _dataAccessMapping.get(d);
              processDataSubcomponentType(dst, _deploymentImplCode, _deploymentHeaderCode);
              _deploymentImplCode.addOutput(" "+declarationID);
              DataSubcomponent ds = dataSubcomponentMapping.get(_dataAccessMapping.get(d));
              if(ds!=null)
            	  _deploymentImplCode.addOutput(GeneratorUtils.getInitialValue(ds)) ;
        	  _deploymentImplCode.addOutputNewline(";") ;
              treatedDeclarations.add(declarationID);
        	}
          }
        }
        return DONE ;
      }
      
      // Builds the data access mapping via the connections described in the
      // process implementation.
      private void buildDataAccessMapping(ComponentImplementation cptImpl)
      {
        
        EList<Subcomponent> subcmpts = cptImpl.getAllSubcomponents() ;
        
        List<String> dataSubcomponentNames = new ArrayList<String>() ;
        
        // Fetches data subcomponent names.
        for(Subcomponent s : subcmpts)
        {
          if(s instanceof DataSubcomponent)
          {
            dataSubcomponentNames.add(s.getName()) ;
          }
        }
        
        // Binds data subcomponent names with DataAcess objects
        // of threads.
        // See process implementation's connections.
        for(Connection connect : cptImpl.getAllConnections())
        {
          if (connect instanceof AccessConnection &&
             ((AccessConnection) connect).getAccessCategory() == AccessCategory.DATA)
          {

        	if(connect.getAllDestination() instanceof DataSubcomponent)
        	{
        	  DataSubcomponent destination =  (DataSubcomponent) connect.
                                                           getAllDestination() ;
            
              if(Aadl2Utils.contains(destination.getName(), dataSubcomponentNames))
              {
                ConnectedElement source = (ConnectedElement) connect.getSource() ;
                DataAccess da = (DataAccess) source.getConnectionEnd() ;
                _dataAccessMapping.put(da, destination.getName()) ; 
              }
        	}
            else if(connect.getAllSource() instanceof DataSubcomponent)
            {
              DataSubcomponent source =  (DataSubcomponent) connect.
              		getAllSource() ;
              if(Aadl2Utils.contains(source.getName(), dataSubcomponentNames))
              {
                ConnectedElement dest = (ConnectedElement) connect.getDestination() ;
                 
                DataAccess da = (DataAccess) dest.getConnectionEnd() ;
                _dataAccessMapping.put(da, source.getName()) ;
              }
            }
            else if(connect.getAllDestination() instanceof DataAccess
            		&& connect.getAllSource() instanceof DataAccess)
            {
            	if(!(connect.getAllDestination().eContainer() instanceof Thread)
            		&& !(connect.getAllSource().eContainer() instanceof Thread))
            		continue;
            	DataAccess destination = (DataAccess) connect.getAllDestination();
            	DataAccess source = (DataAccess) connect.getAllSource();
            	if(_dataAccessMapping.containsKey(destination) &&
            			!_dataAccessMapping.containsKey(source))
            		_dataAccessMapping.put(source, _dataAccessMapping.get(destination)) ;
            	if(_dataAccessMapping.containsKey(source) &&
            			!_dataAccessMapping.containsKey(destination))
            		_dataAccessMapping.put(destination, _dataAccessMapping.get(source)) ;
            	
            }
          }
        }
      }
      
      public String caseProcessSubcomponent(ProcessSubcomponent object)
      {
        process(object.getComponentImplementation()) ;
        for(NamedElement ne: delayedUnparsing)
        	process(ne);
        delayedUnparsing.clear();
        return DONE ;
      }
      
      public String caseSubprogramSubcomponent(SubprogramSubcomponent object)
      {
    	  process(object.getSubprogramSubcomponentType());
    	  return DONE;
      }
      
      public String caseThreadImplementation(ThreadImplementation object)
      {
    	for(SubprogramSubcomponent sc:object.getOwnedSubprogramSubcomponents())
    	{
    		process(sc);
    	}
    	if(_processedTypes.contains(object.getQualifiedName()))
        {
          return DONE ;
        }
        _processedTypes.add(object.getQualifiedName());
    	_currentImplUnparser = _activityImplCode ;
        _currentHeaderUnparser = _activityHeaderCode ;
        buildDataAccessMapping(object) ;
        process(object.getType()) ;
        _currentImplUnparser.addOutput("void* ") ;
        _currentImplUnparser.addOutput(GenerationUtilsC
              .getGenerationCIdentifier(object.getQualifiedName())) ;
        _currentImplUnparser.addOutputNewline(GenerationUtilsC.THREAD_SUFFIX + "()") ;
        _currentImplUnparser.addOutputNewline("{") ;
        _currentImplUnparser.incrementIndent() ;

        for(DataSubcomponent d : object.getOwnedDataSubcomponents())
        {
          process(d) ;
        }
        
        _currentImplUnparser.addOutputNewline("while (1) {") ;
        _currentImplUnparser.incrementIndent() ;
        
        processBehavioredImplementation(object);
        
        _activityImplCode.decrementIndent() ;
        _activityImplCode.addOutputNewline("}") ;
        
        _activityImplCode.addOutputNewline("return 0;") ;
        _activityImplCode.decrementIndent() ;
        _activityImplCode.addOutputNewline("}") ;
        
        _activityHeaderCode.addOutput("void*  ") ;
        _activityHeaderCode.addOutput(GenerationUtilsC
              .getGenerationCIdentifier(object.getQualifiedName())) ;
        _activityHeaderCode.addOutputNewline(GenerationUtilsC.THREAD_SUFFIX + "();\n") ;
        
        return null ;
      }

      public String caseSubprogramCallSequence(SubprogramCallSequence object)
      {
    	for(CallSpecification cs : object.getOwnedCallSpecifications())
    	{
    		if(cs instanceof SubprogramCall)
    		{
    			SubprogramCall sc = (SubprogramCall) cs;
    			process(sc);
    			if(cs.eContainer().eContainer() instanceof ThreadImplementation)
    	  		{
    	  			_currentImplUnparser = _activityImplCode ;
    	  	        _currentHeaderUnparser = _activityHeaderCode ;
    	  		}
    		}
    	}
    	return null ;
      }
      
      public String caseSubprogramType(SubprogramType object)
      {
    	_currentImplUnparser = _subprogramImplCode;
      	_currentHeaderUnparser = _subprogramHeaderCode; 
      	
    	if(_processedTypes.contains(object.getQualifiedName()))
        {
          return DONE ;
        }
        _processedTypes.add(object.getQualifiedName());
    	
        try {
          resolveExistingCodeDependencies(object, null, _subprogramHeaderCode);
        } catch (Exception e1) {
          caseSubprogramClassifier((SubprogramClassifier) object);
                
          processBehavioredType(object) ;
          	    
          _subprogramImplCode.decrementIndent();
          _subprogramImplCode.addOutputNewline("}");	
        }
  	    
  	    return DONE;
      }
      
      public String caseSubprogramClassifier(SubprogramClassifier object)
      {
    	subprogramsUnparsingStack.add(object);
    	  
        Parameter returnParameter = null;
        List<Feature> orderedFeatureList = null;
        if(object instanceof SubprogramImplementation)
        {
          SubprogramImplementation si = (SubprogramImplementation) object;
          orderedFeatureList = Aadl2Utils.orderFeatures(si.getType()) ;
        }
        else if (object instanceof SubprogramType)
        {
          SubprogramType st = (SubprogramType) object;
          orderedFeatureList = Aadl2Utils.orderFeatures(st) ;
        }
        boolean isReturnParam=false;
        for(Feature param: orderedFeatureList)
        {
          if(param instanceof Parameter)
          {
        	Parameter p = (Parameter) param ;
        	isReturnParam = GenerationUtilsC.isReturnParameter(p);
          }
        }
        if(!isReturnParam)
        {
          _subprogramImplCode.addOutput("void ");
          _subprogramHeaderCode.addOutput("void ");
        }

        _subprogramImplCode.addOutput(GenerationUtilsC.getGenerationCIdentifier(object.getQualifiedName()));
        _subprogramHeaderCode.addOutput(GenerationUtilsC.getGenerationCIdentifier(object.getQualifiedName()));

        _subprogramImplCode.addOutput("(");
        _subprogramHeaderCode.addOutput("(");
      	
    	boolean first = true;
    	for(Feature f: orderedFeatureList)
    	{
    	  if(f instanceof Parameter)
  		  {
  		    Parameter p = (Parameter) f ;
  		    String paramUsage = Aadl2Utils.getParameter_Usage(p);
  			if(p==returnParameter)
  			  continue;
  			if(first==false)
  			{
  			  _subprogramImplCode.addOutput(", ") ;
  			  _subprogramHeaderCode.addOutput(", ") ;
  			}
  			processDataSubcomponentType(object, p.getDataFeatureClassifier(), _subprogramImplCode, _subprogramImplCode);
  			processDataSubcomponentType(object, p.getDataFeatureClassifier(), _subprogramHeaderCode, _subprogramHeaderCode);
  			if(Aadl2Utils.isInOutParameter(p) ||
  					Aadl2Utils.isOutParameter(p)
  					|| paramUsage.equalsIgnoreCase("by_reference"))
  			{
  			  _subprogramImplCode.addOutput(" * ") ;
  			  _subprogramHeaderCode.addOutput(" * ") ;
  			}
  			_subprogramImplCode.addOutput(" "+p.getName());
  			_subprogramHeaderCode.addOutput(" "+p.getName());
  			first=false;
  			
  			process(p.getDataFeatureClassifier());
  		  }
  		  else if(f instanceof DataAccess)
  		  {
  			DataAccess da = (DataAccess) f ;
  			if(first==false)
  			{
  			  _subprogramImplCode.addOutput(", ") ;
  			  _subprogramHeaderCode.addOutput(", ") ;
  			}
  			processDataSubcomponentType(object, da.getDataFeatureClassifier(), _subprogramImplCode, _currentImplUnparser);
  			processDataSubcomponentType(object, da.getDataFeatureClassifier(), _subprogramHeaderCode, _subprogramHeaderCode);
  			if(da.getKind().equals(AccessType.REQUIRES))
  			{
  			  if(Aadl2Utils.isReadWriteDataAccess(da) ||
  					  Aadl2Utils.isWriteOnlyDataAccess(da))
  			  {
  				_subprogramImplCode.addOutput(" * ") ;
  			    _subprogramHeaderCode.addOutput(" * ") ;
  			  }
  			  _subprogramImplCode.addOutput(" "+da.getName());
  			  _subprogramHeaderCode.addOutput(" "+da.getName());
  			}
  			first=false;
  			
  			process(da.getDataFeatureClassifier());
  		  }
    	}

    	_subprogramImplCode.addOutputNewline(")");
    	_subprogramHeaderCode.addOutputNewline(");");
    	_subprogramHeaderCode.addOutputNewline("");
  	  
  	    _subprogramImplCode.addOutputNewline("{");
  	    _subprogramImplCode.incrementIndent();
      	
    	return null;
      }
      
      public String caseSubprogramImplementation(SubprogramImplementation object)
      {
    	_currentImplUnparser = _subprogramImplCode;
    	_currentHeaderUnparser = _subprogramHeaderCode; 
    	if(_processedTypes.contains(object.getQualifiedName()))
        {
          return DONE ;
        }
        _processedTypes.add(object.getQualifiedName());
        try {
          resolveExistingCodeDependencies(object, null, _subprogramHeaderCode);
        } catch (Exception e1) {
      	  caseSubprogramClassifier((SubprogramClassifier) object);
        
    	  for(DataSubcomponent d : object.getOwnedDataSubcomponents())
          {
            process(d) ;
          }

          processBehavioredImplementation(object) ;

          _subprogramImplCode.decrementIndent();
          _subprogramImplCode.addOutputNewline("}");
        }
    	  
    	return DONE;
      }
      
      public String caseSubprogramCall(SubprogramCall object)
      {
    	  Parameter returnParameter = null;

    	  if(object.getCalledSubprogram() != null)
    	  {
    		  SubprogramType st = null ;
    		  
    		  SubprogramSubcomponentType sct;
    		  if(object.getCalledSubprogram() instanceof SubprogramAccess)
    			  sct = ((SubprogramAccess)object.getCalledSubprogram()).getSubprogramFeatureClassifier();
    		  else
    			  sct = (SubprogramSubcomponentType) object.getCalledSubprogram() ;


    		  if(sct instanceof SubprogramType)
    		  {
    			  st = (SubprogramType) sct ;
    		  }
    		  else
    		  {
    			  SubprogramImplementation si = (SubprogramImplementation) sct ;
    			  st = si.getType() ;
    		  }

    		  List<Feature> orderedFeatureList = Aadl2Utils.orderFeatures(st) ;
    		  List<ConnectionEnd> orderedParamValue = new ArrayList<ConnectionEnd>();
    		  for(Feature f: orderedFeatureList)
    		  {
    			  ConnectionEnd ce = Aadl2Utils.getConnectedEnd(object, f);
    			  orderedParamValue.add(ce);
    		  }

    		  for(Feature param: orderedFeatureList)
    		  {
    			  if(param instanceof Parameter)
    			  {
    				  Parameter p = (Parameter) param ;
    				  boolean isReturnParam = GenerationUtilsC.isReturnParameter(p);;
    				  
    				  if(isReturnParam)
    				  {
    					  returnParameter = p;
    					  ConnectionEnd ce = orderedParamValue.get(orderedFeatureList.indexOf(p));
						  processConnectionEnd(ce);
						  _currentImplUnparser.addOutput(" = ");
    					  break;
    				  }
    			  }
    		  }

    		  try {
    			resolveExistingCodeDependencies(sct, _currentImplUnparser, _currentHeaderUnparser);
    		  } catch (Exception e1) {
    			_currentImplUnparser.addOutput(GenerationUtilsC.getGenerationCIdentifier(sct.getQualifiedName()));
    		  }
    		  
    		  if(st != null)
    		  {  
    			  _currentImplUnparser.addOutput(" (") ;
    			  boolean first = true;
    			  for(Feature feature: orderedFeatureList)
    			  {
    				  if(feature instanceof Parameter)
    				  {
    					  Parameter p = (Parameter) feature ;
    					  if(p==returnParameter)
    						  continue;
    					  if(first==false)
    						  _currentImplUnparser.addOutput(", ") ;
    					  String paramUsage = Aadl2Utils.getParameter_Usage(p);
    					  if(Aadl2Utils.isInOutParameter(p) ||
    							  Aadl2Utils.isOutParameter(p)
    							  || paramUsage.equalsIgnoreCase("by_reference"))
    					  {
    						  _currentImplUnparser.addOutput("&") ;
    					  }
    					  ConnectionEnd ce = orderedParamValue.get(orderedFeatureList.indexOf(p));
    					  if(ce != null)
						    processConnectionEnd(ce);
    					  first=false;
    				  }
    				  else if(feature instanceof DataAccess)
    				  {
    					  DataAccess da = (DataAccess) feature ;
    					  if(first==false)
    						  _currentImplUnparser.addOutput(", ") ;
    					  if(da.getKind().equals(AccessType.REQUIRES))
    					  {
    						  if(Aadl2Utils.isReadWriteDataAccess(da)
    								  || Aadl2Utils.isWriteOnlyDataAccess(da))
    						  {
    							  _currentImplUnparser.addOutput("&") ;
    						  }
    					  }

    					  String name = null ;

    					  // If a data access mapping is provided:
    					  // Transforms any data access into the right data subcomponent
    					  // 's name thanks to the given data access mapping.
    					  ConnectionEnd parentAccess = orderedParamValue.get(orderedFeatureList.indexOf(da));
    					  if(_dataAccessMapping != null && parentAccess instanceof DataAccess)
    					  {
    						  name = _dataAccessMapping.get((DataAccess)parentAccess);
    					  }

    					  if (name != null)
    					  {
    						  _currentImplUnparser.addOutput(name);
    					  }
    					  else
    					  {
    						  processConnectionEnd(parentAccess);
    					  }
    					  first=false;
    				  }

    			  }

    			  _currentImplUnparser.addOutputNewline(");") ;
    		  }
    		  process(sct);
    	  }
    	  return DONE ;
      }
      
      private void processConnectionEnd(ConnectionEnd ce)
      {
		  if(ce instanceof DataSubcomponent)
			  _currentImplUnparser.addOutput(
					  GenerationUtilsC.getGenerationCIdentifier(ce.getQualifiedName()));
		  else
		  {
			_currentImplUnparser.addOutput(ce.getName());
		  }
      }
      
      public String caseThreadSubcomponent(ThreadSubcomponent object)
      {
        process(object.getComponentImplementation()) ;
        return null ;
      }

      public String caseThreadType(ThreadType object)
      {
        if(_processedTypes.contains(object.getQualifiedName()))
        {
          return null ;
        }

        _processedTypes.add(object.getQualifiedName()) ;
        _currentHeaderUnparser = _activityHeaderCode ;
        _currentImplUnparser = _activityImplCode ;

        for(DataAccess d : object.getOwnedDataAccesses())
        {
          if(d.getKind().equals(AccessType.REQUIRES))
          {
            process(d) ;
          }
        }

        return null ;
      }

      /**
       *  subprogram group access
       */
      public String caseSubprogramGroupAccess(SubprogramGroupAccess object)
      {
        return DONE ;
      }

      /**
       * data access
       */
      public String caseDataAccess(DataAccess object)
      {
        _currentImplUnparser.addOutput("extern ") ;
        
        String dataSubprogramName = null ;
        
        if(_dataAccessMapping != null)
        {
          dataSubprogramName = _dataAccessMapping.get(object) ;
        }
        
        
        try
        {
          resolveExistingCodeDependencies(object.getDataFeatureClassifier(),
                               _currentImplUnparser, _currentHeaderUnparser);
        }
        catch(Exception e)
        {
          _currentImplUnparser.addOutput(GenerationUtilsC
                .getGenerationCIdentifier(object.getDataFeatureClassifier()
                      .getQualifiedName())) ;
        }
        _currentImplUnparser.addOutput(" ") ;
        if(dataSubprogramName != null)
        {
          _currentImplUnparser.addOutput(dataSubprogramName);
        }
        else
        {
          _currentImplUnparser.addOutput(GenerationUtilsC
                .getGenerationCIdentifier(object.getQualifiedName())) ;
        }

        _currentImplUnparser.addOutputNewline(";") ;
        
        
        return DONE ;
      }
    } ;
  }

  @Override
  public void process(Element element, File generatedFilePath) 
                                                    throws GenerationException
  {
    AadlToCSwitchProcess.process(element) ;
    saveGeneratedFilesContent(generatedFilePath) ;
    // Reset all AadlToCSwitchProcess private attributes !
    init() ;
  }

  @Override
  public void setParameters(Map<Enum<?>, Object> parameters)
  {
    throw new UnsupportedOperationException() ;
  }
}