package fr.tpt.aadl.ramses.control.support.analysis;

import fr.tpt.aadl.ramses.control.support.AadlModelInstantiatior ;
import fr.tpt.aadl.ramses.control.support.PredefinedAadlModelManager ;

public interface AnalyzerFactory
{
  public Analyzer createAnalyzer(AadlModelInstantiatior modelInstantiatior,
                                 PredefinedAadlModelManager predefinedAadlModels) ;
}