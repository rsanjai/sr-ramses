package fr.tpt.aadl.sched.aadlinspector.output;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.tpt.aadl.sched.aadlinspector.output.ResponseTimeResult.TaskResponseTimeResult;

public class XMLResultsProvider {

	private final static String ANALYSIS_CPU_UTILIZATION_FACTOR 
		= "processor utilization factor";
	
	private final static String ANALYSIS_SIMULATION
		= "Task response time computed from simulation";
	
	
	private final static String COMPUTATION_SIMULATION_RESPONSE_TIME
		= "Task response time computed from simulation";
	
	
	
	private final AnalysisResult result = new AnalysisResult();
	private final Element root;
	
	public XMLResultsProvider(File outputFile) 
			throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;
	    DocumentBuilder builder = factory.newDocumentBuilder() ;
		Document doc = builder.parse(outputFile);
		this.root = doc.getDocumentElement();
		
		extractData();
	}
	
	public AnalysisResult getResult()
	{
		return result;
	}
	
	private void extractData()
	{
		NodeList children = root.getChildNodes();
		for(int iNode=0;iNode<children.getLength();iNode++)
		{
			Node n = children.item(iNode);
			String nodeName = n.getNodeName();
			if (nodeName.equals("feasibilityTest"))
			{
				NamedNodeMap attrs = n.getAttributes();
				String nName = attrs.getNamedItem("name").getNodeName();
				if (nName.equals(ANALYSIS_CPU_UTILIZATION_FACTOR))
				{
					extractUtilizationFactorResults(n);
				}
				else if (nName.equals(ANALYSIS_SIMULATION))
				{
					extractSimulationResults(n);
				}
			}
		}
	}
	
	private void extractUtilizationFactorResults(Node n)
	{
		//TODO: extract utilization factor results
	}
	
	private void extractSimulationResults(Node n)
	{
		String cpu = n.getAttributes().getNamedItem("reference")
				.getNodeName();
		
		ResponseTimeResult r = new ResponseTimeResult();
		result.setResponseTimeResult(cpu, r);
		
		NodeList children = n.getChildNodes();
		for(int iChild=0;iChild<children.getLength();iChild++)
		{
			Node nChild = children.item(iChild);
			String nChildName = nChild.getNodeName();
			
			if (nChildName.equals("computation"))
			{
				String compName = nChild.getAttributes()
						.getNamedItem("name").getNodeName();
				if (compName.equals(COMPUTATION_SIMULATION_RESPONSE_TIME))
				{
					NamedNodeMap attrs = nChild.getAttributes();
					Node nReference = attrs.getNamedItem("reference");
					Node nWorst = attrs.getNamedItem("worst");
					Node nBest = attrs.getNamedItem("best");
					Node nAvg = attrs.getNamedItem("average");
					
					String taskName = nReference.getNodeName();
					int worst = Integer.parseInt(nWorst.getNodeName());
					int best  = Integer.parseInt(nBest.getNodeName());
					double average = Double.parseDouble(nAvg.getNodeName());
					
					r.setResponseTime(taskName, new TaskResponseTimeResult(
							taskName, best,worst,average));
				}
			}
			else if (nChildName.equals("schedulability"))
			{
				NamedNodeMap attrs = nChild.getAttributes();
				Node nScheduler = attrs.getNamedItem("scheduler");
				Node nPreemptive = attrs.getNamedItem("preemptive");
				Node nResult = attrs.getNamedItem("result");
				Node nExplanation = attrs.getNamedItem("explanation");
				
				String scheduler = nScheduler.getNodeName();
				boolean preemptive = nPreemptive.getNodeName().equals("true");
				boolean result = nResult.getNodeName().equals("true");
				String explanation = nExplanation.getNodeName();
				
				r.setScheduler(scheduler);
				r.setPreemptive(preemptive);
				r.setResult(result);
				r.setExplanation(explanation);
			}
		}
	}
}