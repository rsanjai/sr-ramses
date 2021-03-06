package fr.tpt.aadl.sched.wcetanalysis.model ;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.utils.PropertyUtils;

import fr.tpt.aadl.sched.wcetanalysis.extractors.AST2AccessDates;
import fr.tpt.aadl.sched.wcetanalysis.util.LongestAndShortestPath;

public class TaskBody
{

  private ASTNode initAST = null ;
  private ASTNode mainLoopAST = null ;
  private ASTNode reducedMainLoopAST = null ;

  private int priority = -1 ;

  private double startTime = -1 ;
  private double BCET = -1;
  private double WCET = -1 ;

  public final ComponentInstance getTask()
  {
    return mainLoopAST.getElement() ;
  }

  public final int getPriority()
  {
    return priority ;
  }

  public final ASTNode getInitAST()
  {
    return initAST ;
  }

  public final ASTNode getMainLoopAST()
  {
    return mainLoopAST ;
  }

  public ASTNode getReducedMainLoopAST()
  {
    if(reducedMainLoopAST == null)
    {
      recomputeMainLoop() ;
    }

    return reducedMainLoopAST ;
  }

  public double getStartTime()
  {
    if(startTime == -1)
    {
    	startTime = LongestAndShortestPath.getLongestPath(
    			getInitAST(), getInitAST().getEnd()) ;
    }

    return startTime ;
  }

  public double getBCET()
  {
	if (BCET == -1)
	{
		BCET = LongestAndShortestPath.getShortestPath(
    			getMainLoopAST(), getMainLoopAST().getEnd()) ;
	}
	return BCET;
  }
  
  public double getWCET()
  {
    if(WCET == -1)
    {
    	WCET = LongestAndShortestPath.getLongestPath(
    			getMainLoopAST(), getMainLoopAST().getEnd()) ;
    }

    return WCET ;
  }

  public AST2AccessDates getSynchronizationInstants()
  {
    return new AST2AccessDates(mainLoopAST.reduceFromMe()) ;
  }

  public void recomputeMainLoop()
  {
    reducedMainLoopAST = mainLoopAST.reduceFromMe() ;
  }

  public boolean isUsingResource(String resourceID)
  {
    return getSynchronizationInstants().getAccessedResources()
          .contains(resourceID) ;
  }

  public void setInitAST(ASTNode initAST)
  {
    this.initAST = initAST ;
    priority = computePriority() ;
  }

  public void setMainLoopAST(ASTNode mainLoopAST)
  {
    this.mainLoopAST = mainLoopAST ;
    priority = computePriority() ;
  }

  private int computePriority()
  {
    try
    {
      return (int) PropertyUtils.getIntValue(getTask(), "Priority") ;
    }
    catch(Exception e)
    {
      return 0 ;
    }
  }

  public String toString()
  {
    return "Initialization graph : \n" + initAST + "\n\n" +
          "Iteration graph : \n" + mainLoopAST + "\n\n" +
          "Steady state sequence : \n" + getReducedMainLoopAST() + "\n\n" ;
  }
}