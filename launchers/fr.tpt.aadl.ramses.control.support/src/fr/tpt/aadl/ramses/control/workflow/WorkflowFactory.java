/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package fr.tpt.aadl.ramses.control.workflow;

import org.eclipse.emf.ecore.EFactory;

import fr.tpt.aadl.ramses.control.workflow.impl.WorkflowFactoryImpl;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see fr.tpt.aadl.ramses.control.workflow.WorkflowPackage
 * @generated
 */
public interface WorkflowFactory extends EFactory {
	/**
   * The singleton instance of the factory.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @generated
   */
	WorkflowFactory eINSTANCE = fr.tpt.aadl.ramses.control.workflow.impl.WorkflowFactoryImpl.init();

	/**
   * Returns a new object of class '<em>Workflow</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Workflow</em>'.
   * @generated
   */
	Workflow createWorkflow();

	/**
   * Returns a new object of class '<em>Transformation</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Transformation</em>'.
   * @generated
   */
	Transformation createTransformation();

	/**
   * Returns a new object of class '<em>Generation</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Generation</em>'.
   * @generated
   */
	Generation createGeneration();

	/**
   * Returns a new object of class '<em>Conjunction</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Conjunction</em>'.
   * @generated
   */
  Conjunction createConjunction();

  /**
   * Returns a new object of class '<em>Disjunction</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Disjunction</em>'.
   * @generated
   */
  Disjunction createDisjunction();

  /**
   * Returns a new object of class '<em>Analysis</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Analysis</em>'.
   * @generated
   */
	Analysis createAnalysis();

	/**
   * Returns a new object of class '<em>Error State</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Error State</em>'.
   * @generated
   */
	ErrorState createErrorState();

	/**
   * Returns a new object of class '<em>List</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>List</em>'.
   * @generated
   */
	List createList();

	/**
   * Returns a new object of class '<em>File</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>File</em>'.
   * @generated
   */
	File createFile();

	/**
   * Returns a new object of class '<em>Analysis Option</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Analysis Option</em>'.
   * @generated
   */
	AnalysisOption createAnalysisOption();

	/**
   * Returns a new object of class '<em>Model Identifier</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Model Identifier</em>'.
   * @generated
   */
	ModelIdentifier createModelIdentifier();

	/**
   * Returns a new object of class '<em>Unparse</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Unparse</em>'.
   * @generated
   */
	Unparse createUnparse();

	/**
   * Returns a new object of class '<em>Loop</em>'.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return a new object of class '<em>Loop</em>'.
   * @generated
   */
	Loop createLoop();

	/**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
	WorkflowPackage getWorkflowPackage();

} //WorkflowFactory
