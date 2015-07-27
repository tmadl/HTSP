/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida.excitation;

/**
 *
 * @author madlt
 */
public class LinearExcitation implements ExcitationStrategy {
    public double K = 1;

    public LinearExcitation() {}

    public LinearExcitation(double k) {
        K = k;
    }

    @Override
    public double calculateNewActivation(double oldActivation, double incomingActivation) {
        double newActivation = oldActivation + K*incomingActivation;
        //if (newActivation > 1) newActivation = 1;
        return newActivation;
   }
}
