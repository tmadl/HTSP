/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida.excitation;

/**
 *
 * @author madlt
 */
public interface ExcitationStrategy {
    public double calculateNewActivation(double oldActivation, double incomingActivation);
}
