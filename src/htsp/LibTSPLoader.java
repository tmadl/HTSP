/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp;

import htsp.solvers.GoalPoint;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tom
 * http://www2.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/tsp/
 */
public class LibTSPLoader {
    public static GoalPoint[] loadLibtspData(String path) {
        try {
            List<GoalPoint> goalpointlist = new ArrayList<GoalPoint>();
            double cx, cy;
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            boolean reacheddata = false;

            double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE, maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
            while ((line = br.readLine()) != null) {
                if (line.contains("SECTION") && (line.contains("DATA") || line.contains("COORD"))) {
                    reacheddata = true;
                }
                else if (line.contains("EOF")) {
                    reacheddata = false;
                }
                else if (reacheddata) {
                    line = line.replaceAll("\t", " ");
                    while (line.contains("  ")) line = line.replaceAll("  ", " ");
                    while (line.charAt(0)==' ') line = line.substring(1);
                    String[] parts = line.split(" ");
                    try {
                        cx = Double.parseDouble(parts[1]);
                        int i = 0;
                        while ((int)(cx) * 10 != (int)(cx * 10) && i++<2) cx *= 10;
                        cy = Double.parseDouble(parts[2]);
                        i = 0;
                        while ((int)(cy) * 10 != (int)(cy * 10) && i++<2) cy *= 10;
                        goalpointlist.add(new GoalPoint((int)cx, (int)cy));

                        if (cx < minx) minx = cx;
                        if (cx > maxx) maxx = cx;
                        if (cy < miny) miny = cy;
                        if (cy > maxy) maxy = cy;
                    }
                    catch (NumberFormatException ex) {
                        ex = ex;
                    }
                }
            }
            fr.close();
            if (minx >= 0 && miny >= 0 && maxx < 40 && maxy < 40 && (maxx-minx)>4 && (maxy-miny)>4) { //no normalization needed
                GoalPoint[] result;
                result = new GoalPoint[goalpointlist.size()];
                goalpointlist.toArray(result);
                return result;
            }
            else {
                return normalize(goalpointlist);
            }
        }
        catch (Exception ex) {}
        return null;
    }

    public static GoalPoint[] normalize(List<GoalPoint> goalpoints) {
        int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = 0, maxy = 0;
        for (GoalPoint n : goalpoints) {
            if (n.getX() < minx) minx = n.getX();
            if (n.getX() > maxx) maxx = n.getX();
            if (n.getY() < miny) miny = n.getY();
            if (n.getY() > maxy) maxy = n.getY();
        }
        for (GoalPoint n : goalpoints) {
            n.setX(n.getX()-minx);
            n.setY(n.getY()-miny);

            int xscale = (maxx - minx) / goalpoints.size();
            if (xscale < 1) xscale = 1;
            int yscale = (maxy - miny) / goalpoints.size();
            if (yscale < 1) yscale = 1;
            n.setX(n.getX()/xscale);
            n.setY(n.getY()/yscale);
        }

        GoalPoint[] result = new GoalPoint[goalpoints.size()];
        goalpoints.toArray(result);
        return result;
    }

    public static List<Integer> loadLibtspSolution(String path) {
        try {
            List<Integer> solution = new ArrayList<Integer>();
            double cx, cy;
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String solutionstring = "";
            boolean reacheddata = false;
            while ((line = br.readLine()) != null) {
                if (line.contains("SECTION")) {
                    reacheddata = true;
                }
                else if (line.contains("EOF")) {
                    reacheddata = false;
                }
                else if (reacheddata) {
                    line = line.replaceAll("\t", " ");
                    while (line.contains("  ")) line = line.replaceAll("  ", " ");
                    solutionstring += line + " ";
                }
            }
            while (solutionstring.contains("  ")) solutionstring = solutionstring.replaceAll("  ", " ");
            while (solutionstring.charAt(0)==' ') solutionstring = solutionstring.substring(1);
            String[] goalpointIds = solutionstring.split(" ");
            for (String i : goalpointIds) {
                try {
                    int gid = Integer.parseInt(i);
                    if (gid > 0) solution.add(gid - 1);
                }
                catch (NumberFormatException ex) {}
            }
            if (solution.size() > 1 && solution.get(0).intValue() != solution.get(solution.size() - 1).intValue()) {
                solution.add(solution.get(0));
            }
            fr.close();
            return solution;
        }
        catch (Exception ex) {}
        return null;
    }
}
