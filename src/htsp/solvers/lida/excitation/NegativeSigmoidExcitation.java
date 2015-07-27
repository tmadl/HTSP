/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida.excitation;

/**
 *
 * @author madlt
 */
public class NegativeSigmoidExcitation implements ExcitationStrategy {
    public static double defaultA=6, defaultB=0.5*defaultA;
    public double A = defaultA;
    public double B = defaultB;
    // 1 / (1+exp(-Ax+B))
    public double epsilon = 1e-10;

    public NegativeSigmoidExcitation() {}

    public NegativeSigmoidExcitation(double a, double b) {
        A=a; B=b;
    }

    @Override
    public double calculateNewActivation(double oldActivation, double incomingActivation) {
        //double curExcitation = -(Math.log((1.0 + epsilon - oldActivation)/(oldActivation + epsilon)) + B) / A + incomingActivation;
        double curExcitation = -(Math.log(1/(-oldActivation+epsilon) - 1 + 2*epsilon) - B)/A;
        if (oldActivation == 0) curExcitation = 0;
        return -1/(1 + Math.exp(-A * (curExcitation+incomingActivation) + B));
    }
}
