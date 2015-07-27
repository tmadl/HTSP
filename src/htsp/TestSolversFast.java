/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp;

import htsp.solvers.GoalPoint;
import htsp.solvers.RandomSolver;
import htsp.solvers.Solver;
import htsp.solvers.TSPGrid;
import htsp.solvers.bruteforce.BruteForceSolver;
import htsp.solvers.concorde.ConcordeSolver;
import htsp.solvers.lida.GoalPath;
import htsp.solvers.lida.LIDASolverHierarchicalSIExhaustive;
import java.io.FileWriter;

/**
 *
 * @author Tom
 */
public class TestSolversFast {
    static Solver solver, optimalSolver;

    public static void main(String[] args) {
        int number = 100; //10  3
        int maxnumber = 100; //48 70 120
        int step = 10;
        int step10threshold = Integer.MAX_VALUE; //10
        int runs = 30; //50
        boolean constgrid = false;
        boolean calcerr = true;
        boolean round = true; //true
        int maxlevels = Integer.MAX_VALUE;

        HTSPLogger.disable();
        GoalPath.debugging = false;

        if (LIDASolverHierarchicalSIExhaustive.CONNECTIONK < 10)
            System.out.println("warning: low connection k "+LIDASolverHierarchicalSIExhaustive.CONNECTIONK);

        String differences = "", alldifferences="";
        double sumerrors = 0, preverror = 0;
        String costsmatrix = "", errmatrix = "", itmatrix = "", optpmatrix = "";
        String header = ("N=["+number+" , "+maxnumber+"] nodes; n = "+runs+" runs\n(costs /n/n errors /n/n iterations /n/n % optimal solutions)");
        for (; number <= maxnumber; number+=step) {
            if (number>=step10threshold) step = 10;
            //    if (number == 20) step = 28;

            sumerrors = 0;
            differences="";
            preverror = 0;
            for (int i = 0; i < runs; i++) {
                System.out.println(i+"/"+runs + " (N="+number+", avgerr="+Math.round((sumerrors/i)*100*100)/100.0+")");
                GoalPoint[] goals;
                if (constgrid) goals = TSPGrid.randomGoals(number, HTSP.DEFAULTWIDTH, HTSP.DEFAULTHEIGHT);
                else goals = TSPGrid.randomGoalPoints(number);
                if (number > 9) {
                    TSPGrid.exportGoals(goals, ConcordeSolver.tsppath);
                    optimalSolver = new ConcordeSolver();
                    round = true;
                }
                else {
                    if (constgrid) optimalSolver = new BruteForceSolver(HTSP.DEFAULTWIDTH, HTSP.DEFAULTHEIGHT, goals);
                    else optimalSolver = new BruteForceSolver(goals);
                }
                if (!calcerr) optimalSolver = new RandomSolver(goals);
                    //optimalSolver.init(number);
                double optimalCost = optimalSolver.solve();
                
                if (constgrid) solver = new LIDASolverHierarchicalSIExhaustive(HTSP.DEFAULTWIDTH, HTSP.DEFAULTHEIGHT, goals, maxlevels);
                else solver = new LIDASolverHierarchicalSIExhaustive(goals, maxlevels); //LIDASolverInhibitingPE

                double solverCost = solver.solve();
                double error;
                if (optimalCost == 0 || solverCost == 0) {
                    if (preverror != 0)
                        error = preverror;
                    else {
                        System.out.println("SOLVER FAILED, NO PREVERROR");
                        i--;
                        continue;
                    }
                }
                else {
                    if (round) error = ((double)(Math.round(solverCost))/(double)(Math.round(optimalCost))-1.0);
                    else error=(double)solverCost/optimalCost - 1.0;
                    preverror = error;
                }
                sumerrors += error;
                differences += Math.round(error*100*1000)/1000.0 + "\t";

                costsmatrix += (int)solverCost + " ";
                errmatrix += (int)(error*1000)/1000.0+" ";
                itmatrix += solver.getSolutionIterations()+" ";
                optpmatrix += error==0?"1 ":"0 ";
            }
            alldifferences+=differences;
            
            costsmatrix += ";\n";
            errmatrix += ";\n";
            itmatrix += ";\n";
            optpmatrix += ";\n";

            /*finalstr+=(optimalCosts+"\n");
            finalstr+=(solverCosts+"\n");
            finalstr+=(differences+"\n");
            finalstr+=(Math.round((sumerrors/runs)*100*1000)/1000.0+"\n\n");*/

            //avgerrors += Math.round((sumerrors/runs)*100*1000)/1000.0+"\t";
            //avgiterations += Math.round(((double)sumiterations/runs)*1000)/1000.0+"\t";
            System.out.println("Avg error:\t"+Math.round((sumerrors/runs)*100*100)/100.0+"\n");
            System.out.println(alldifferences);

            try {
                FileWriter fw = new FileWriter("SolverTest.log");
                fw.write(header+"\n\n"+costsmatrix+"\n\n"+errmatrix+"\n\n"+itmatrix+"\n\n"+optpmatrix+"\n\n"+number+"/"+maxnumber+" done.");
                fw.close();
            }
            catch (Exception ex) {
            }
        }
    }
}
