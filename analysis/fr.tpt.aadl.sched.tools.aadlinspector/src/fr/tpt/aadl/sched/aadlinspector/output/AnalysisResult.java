package fr.tpt.aadl.sched.aadlinspector.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;

import fr.tpt.aadl.launch.AADLInspectorSchedulingAnalysis;
import fr.tpt.aadl.ramses.analysis.AnalysisArtifact;
import fr.tpt.aadl.ramses.analysis.AnalysisResultFactory;
import fr.tpt.aadl.ramses.analysis.AnalysisSource;
import fr.tpt.aadl.ramses.analysis.QualitativeAnalysisResult;
import fr.tpt.aadl.ramses.analysis.QuantitativeAnalysisResult;
import fr.tpt.aadl.ramses.util.properties.AadlUtil;
import fr.tpt.aadl.sched.aadlinspector.output.ResponseTimeResult.TaskResponseTimeResult;

public class AnalysisResult 
{
	private Map<String, UtilizationFactorResult> utilizationFactorResults
		= new HashMap<String,UtilizationFactorResult>();
	
	private Map<String, ResponseTimeResult> responseTimeResults
		= new HashMap<String,ResponseTimeResult>();

	private SystemInstance model = null;
	
	void setModel(SystemInstance model) 
	{
		this.model = model;
	}

	public boolean isSchedulable()
	{
		Set<String> cpuNames = responseTimeResults.keySet();
		for(String cpuName : cpuNames)
		{
			if (!getResponseTimeResults(cpuName).isResult())
			{
				return false;
			}
		}
		return true;
	}
	
	public UtilizationFactorResult getUtilizationFactor(String cpuName)
	{
		return utilizationFactorResults.get(cpuName);
	}
	
	public ResponseTimeResult getResponseTimeResults(String cpuName)
	{
		return responseTimeResults.get(cpuName);
	}
	
	void setUtilizationFactorResult(String cpuName, UtilizationFactorResult r)
	{
		utilizationFactorResults.put(cpuName, r);
	}
	
	void setResponseTimeResult (String cpuName, ResponseTimeResult r)
	{
		responseTimeResults.put(cpuName, r);
	}
	
	public void normalize(AnalysisArtifact aaResults)
	{
		AnalysisResultFactory f = AnalysisResultFactory.eINSTANCE;
		
		QualitativeAnalysisResult schedulable = f.createQualitativeAnalysisResult();
		AnalysisSource schedulable_s = f.createAnalysisSource();
		schedulable_s.setMethodName(AADLInspectorSchedulingAnalysis.PLUGIN_NAME);
		
		schedulable.setValidated(isSchedulable());
		schedulable.setSource(schedulable_s);
		
		aaResults.getResults().add(schedulable);
		
		for(String cpuName : responseTimeResults.keySet())
		{
			ResponseTimeResult rtrs = getResponseTimeResults(cpuName);
			Map<String,TaskResponseTimeResult> rtr = rtrs.getResponseTimes();
			for(String taskName : rtr.keySet())
			{
				double deadline = getDeadline(cpuName, taskName);
				double wcrt = rtr.get(taskName).worst;
				double margin = (deadline - wcrt)/deadline;
				
				AnalysisSource responseMargin_s = f.createAnalysisSource();
				responseMargin_s.setMethodName(AADLInspectorSchedulingAnalysis.PLUGIN_NAME);
				responseMargin_s.setScope(taskName);
				
				QuantitativeAnalysisResult responseMargin = f.createQuantitativeAnalysisResult();
				responseMargin.setMargin((float) margin);
				responseMargin.setSource(responseMargin_s);
				
				aaResults.getResults().add(responseMargin);
			}
		}
		
	}
	
	private double getDeadline(String cpuName, String taskName)
	{
		ComponentInstance cpu = null;
		ComponentInstance task = null;
		
		for(ComponentInstance c : model.getComponentInstances())
		{
			if ((c.getCategory()==ComponentCategory.PROCESSOR)
					&& (c.getName().equals(cpuName)))
			{
				cpu = c;
				break;
			}
		}
		
		for(ComponentInstance c : model.getAllComponentInstances())
		{
			if ((c.getCategory()==ComponentCategory.THREAD)
					&& (c.getName().equals(taskName)))
			{
				if (AadlUtil.getInfoTaskCPU(c) == cpu)
				{
					task = c;
					break;
				}
			}
		}
		
		if (task!=null)
		{
			return AadlUtil.getInfoTaskDeadline(task);
		}
		else
		{
			return 0d;
		}
	}
}
