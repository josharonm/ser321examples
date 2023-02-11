/**
 File: StringList.java
 Author: Student in Fall 2020B, Josh McManus
 Description: StringList class in package tasktwo.
 */

package tasktwo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class StringList {
    
    List<String> strings = new ArrayList<String>();

    public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }

    public int contains(String str) {
        return strings.indexOf(str);
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }

    public void prepend(int i, String str) {
        if (i < strings.size() && i >= 0) {
            //valid index
            String temp = str + strings.get(i);
            strings.set(i, temp);
        }
    }

    public void sort() {Collections.sort(strings);}
}