/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lightningbug;

import java.util.Comparator;

/**
 *
 * @author Administrator
 */
public class BugComparator implements Comparator<MyBug> {

    @Override
    public int compare(MyBug b1, MyBug b2) {
        return (b1.getId().compareTo(b2.getId()));
    }
}

