package fr.tpt.aadl.sched.wcetanalysis.extractors.ba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osate.aadl2.Classifier;
import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramClassifier;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.ba.aadlba.BehaviorAction;
import org.osate.ba.aadlba.BehaviorActionBlock;
import org.osate.ba.aadlba.BehaviorActionSequence;
import org.osate.ba.aadlba.BehaviorActions;
import org.osate.ba.aadlba.BehaviorAnnex;
import org.osate.ba.aadlba.BehaviorTransition;

import fr.tpt.aadl.sched.wcetanalysis.ExtractionContext;
import fr.tpt.aadl.sched.wcetanalysis.WcetAnalysisDebug;
import fr.tpt.aadl.sched.wcetanalysis.model.ASTNode;
import fr.tpt.aadl.sched.wcetanalysis.util.BehaviorAnnexUtil;

/**
 * Extract execution flow from subprogram behavior annex.
 * 
 * @author Fabien Cadoret
 * 
 */
public class SubprogramBehaviorAnnexExtractor extends BehaviorAnnexExtractor
{

	public SubprogramBehaviorAnnexExtractor(ExtractionContext ctxt)
	{
		super(ctxt);
	}

	public ASTNode extractFrom(SubprogramCall call, ASTNode lastAction)
	{
		SubprogramClassifier spg = (SubprogramClassifier) call.getCalledSubprogram();
		return extractFrom(spg,lastAction);
	}
	
	public ASTNode extractFrom(Classifier spg, ASTNode lastAction)
	{
		BehaviorAnnex ba = BehaviorAnnexUtil.getBehaviorAnnex(spg) ;
		
		if (!checkIsCompatibleSpecification(ba))
		{
			return null;
		}
		else
		{
			ComponentInstance task = lastAction.getElement();
			return extractFrom(ba, task, lastAction);
		}
	}

	private ASTNode extractFrom(BehaviorAnnex ba, ComponentInstance task,
			ASTNode lastAction)
	{
		final Map<String, ASTNode> stateToStart = new HashMap<String, ASTNode>();
		final Map<String, ASTNode> stateToEnd = new HashMap<String, ASTNode>();
		
		/** For each state, register start and end actions. */
		registerStatesIntoGraph(ba, task, stateToStart, stateToEnd);
		
		String initStateID = BehaviorAnnexUtil.getInitialState(ba);
		String finalStateID = BehaviorAnnexUtil.getFinalState(ba);
		
		ASTNode initialAction = stateToStart.get(initStateID);
		ASTNode endAction = stateToEnd.get(finalStateID);
		//initialAction.addNext(stateToEnd.get(initStateID));
		List<BehaviorTransition> transitions = ba.getTransitions();

		for (int indexTr = 0; indexTr < transitions.size(); indexTr++)
		{
			BehaviorTransition tran = transitions.get(indexTr);
			BehaviorActionBlock block = tran.getActionBlock();
			List<BehaviorAction> actions = null;
			
			if (block == null)
			{
				actions = Collections.emptyList();
			}
			else
			{
				BehaviorActions bActions = block.getContent();

				if (bActions instanceof BehaviorAction)
				{
					actions = new ArrayList<BehaviorAction>();
					actions.add((BehaviorAction) bActions);
				}
				else if (bActions instanceof BehaviorActionSequence)
				{
					actions = ((BehaviorActionSequence) bActions).getActions();
				}
			}

			String srcState = tran.getSourceState().getName();
			String dstState = tran.getDestinationState().getName();
			
			/**
			 * There is a (direct) loop in the subprogram.
			 * Check if this is the main loop of thread.
			 *   - The subprogram is called by the thread directly.
			 */
			if (srcState.equals(dstState) && isCalledByThread())
			{
				dstState = getFinalState(ba).getName();
				WcetAnalysisDebug.println(
						"Loop on behavior transition of subprogram " +
						ctxt.getCurrentVisitedElement().getName() +
						" is the main loop of the thread -> IGNORED");
			}
			
			ASTNode srcStart = stateToStart.get(srcState);
			ASTNode srcEnd   = stateToEnd.get(srcState);
			ASTNode dstStart = stateToStart.get(dstState);

			ASTNode last = ctxt.behaviorActionBlockExtractor.extractFrom(task,
					actions, srcStart);
			
			last.addNext(srcEnd);
			
			if (! isInitialFinalTran(tran))
			{
				srcEnd.addNext(dstStart);
			}
		}

		lastAction.addNext(initialAction);
		return endAction;
	}

	private boolean isCalledByThread()
	{
		return ctxt.getCurrentVisitedThread()
				== ctxt.getParentVisitedElement();
	}
}
