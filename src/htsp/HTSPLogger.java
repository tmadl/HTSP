/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp;

/**
 *
 * @author Tom
 */
public class HTSPLogger {
    private static boolean enabled = true;

    public static void disable() {enabled = false;}
    public static void enable() {enabled = true;}

    public static void logline(String str) {
        if (enabled) System.out.println(str);
    }
    public static void log(String str) {
        if (enabled) System.out.print(str);
    }
}
