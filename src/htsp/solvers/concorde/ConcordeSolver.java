/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers.concorde;

import htsp.solvers.*;
import htsp.TspDrawer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 *
 * @author madlt
 */
public class ConcordeSolver implements Solver {
    public static String tsppath = "E:\\cygwin\\bin\\asdf.tsp";

    @Override
    public int getWidth(){return 0;}
    @Override
    public int getHeight() {return 0;}
    
    @Override
    public double solve() {
        double cost = 0;
        ProcessBuilder pb = null;
        Process p = null;
        try {
             pb = new ProcessBuilder("E:\\cygwin\\bin\\bash", "-c", "\"/bin/con.sh\"");
             Map<String, String> env = pb.environment();
             pb.directory(new File("E:\\cygwin\\bin"));
             pb.redirectErrorStream(true);
             p = pb.start();
             //int res = p.waitFor();
             InputStream is = p.getInputStream();
             int c;
             String out = "";
             while ((c = is.read())!=-1) out+=(char)c;
             if (out.length() > 0)
                out=out;
             //if getting STATUS_ACCESS_VIOLATION from cygwin: in cmd execute ash, in ash: "/bin/rebaseall"
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null, "Concorde path not found - please update in ConcordeSolver.java");
            e.printStackTrace();
        }

        String result = "", line, coststr;
        try {
            FileReader fr = new FileReader("E:\\cygwin\\bin\\result.txt");
            BufferedReader br = new BufferedReader(fr);
            while ((line=br.readLine()) != null) {
                result += line;
                if (line.toLowerCase().contains("optimal")) {
                    coststr=(line.split(":")[1]).substring(1);
                    cost = Double.parseDouble(coststr);
                }
            }
            br.close();
            fr.close();
        }
        catch (Exception ex) {
        }

        if (cost == 0) {
            cost = cost;
        }
        System.out.println("Concorde solution cost: "+cost);
        return cost;
    }

    @Override
    public double getProgress() {return 0;}
    @Override
    public void init(int number) {}
    @Override
    public void init(GoalPoint[] nodes) {}
    @Override
    public void init(GoalPoint[] nodes, int width, int height) {}
    @Override
    public GoalPoint[] getGoalpoints()  {return null;}
    @Override
    public GoalPoint getGoalPoint(int x, int y)  {return null;}
    @Override
    public List<Integer> getSolutionPath()  {return null;}
    @Override
    public int getSolutionIterations() {return 0;}
    @Override
    public void setTspDrawer(TspDrawer d) {}
}
