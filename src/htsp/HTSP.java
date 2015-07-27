/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp;

/**
 *
 * @author madlt
 */
public class HTSP {
    //public static int DEFAULTWIDTH = 19, DEFAULTHEIGHT = 19, DEFAULTNODES = 9;
    public static int DEFAULTWIDTH = 15, DEFAULTHEIGHT = 15, DEFAULTNODES = 9; //12 12
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Solver solver = new BruteForceSolver(DEFAULTWIDTH, DEFAULTHEIGHT, TSPGrid.randomGoalPoints(DEFAULTNODES));

        //Solver solver = new LIDASolver(DEFAULTWIDTH, DEFAULTHEIGHT, TSPGrid.randomGoals(DEFAULTNODES, DEFAULTWIDTH, DEFAULTHEIGHT));

        MainGui gui = new MainGui();
        
        gui.setTitle("HTSP");
        gui.setVisible(true);
    }

}
