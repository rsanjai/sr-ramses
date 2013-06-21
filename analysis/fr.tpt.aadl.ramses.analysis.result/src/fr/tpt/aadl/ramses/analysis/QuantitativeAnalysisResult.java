/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package fr.tpt.aadl.ramses.analysis;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Quantitative Analysis Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link fr.tpt.aadl.ramses.analysis.QuantitativeAnalysisResult#getMargin <em>Margin</em>}</li>
 * </ul>
 * </p>
 *
 * @see fr.tpt.aadl.ramses.analysis.AnalysisResultPackage#getQuantitativeAnalysisResult()
 * @model
 * @generated
 */
public interface QuantitativeAnalysisResult extends AnalysisResult {
	/**
	 * Returns the value of the '<em><b>Margin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Margin</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Margin</em>' attribute.
	 * @see #setMargin(float)
	 * @see fr.tpt.aadl.ramses.analysis.AnalysisResultPackage#getQuantitativeAnalysisResult_Margin()
	 * @model
	 * @generated
	 */
	float getMargin();

	/**
	 * Sets the value of the '{@link fr.tpt.aadl.ramses.analysis.QuantitativeAnalysisResult#getMargin <em>Margin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Margin</em>' attribute.
	 * @see #getMargin()
	 * @generated
	 */
	void setMargin(float value);

} // QuantitativeAnalysisResult