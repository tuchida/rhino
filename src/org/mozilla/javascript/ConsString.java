/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * <p>This class represents a string composed of two components, each of which
 * may be a <code>java.lang.String</code> or another ConsString.</p>
 *
 * <p>This string representation is optimized for concatenation using the "+"
 * operator. Instead of immediately copying both components to a new character
 * array, ConsString keeps references to the original components and only
 * converts them to a String if either toString() is called or a certain depth
 * level is reached.</p>
 *
 * <p>Note that instances of this class are only immutable if both parts are
 * immutable, i.e. either Strings or ConsStrings that are ultimately composed
 * of Strings.</p>
 *
 * <p>Both the name and the concept are borrowed from V8.</p>
 */
public class ConsString implements CharSequence, Serializable {

    private static final long serialVersionUID = -8432806714471372570L;

    private CharSequence left, right;
    private final int length;

    /**
     * Number of ConsString (other than flattened) in left and right.
     */
    private int consCount;

    /**
     * Maximum of number of right edges in path traversed from root to leaf.
     */
    private int maxRightEdge;

    public ConsString(CharSequence left, CharSequence right) {
        this.left = left;
        this.right = right;
        length = left.length() + right.length();
        consCount = 1;
        int leftRightEdge = 0;
        int rightRightEdge = 1;
        if (left instanceof ConsString) {
            consCount += ((ConsString)left).consCount;
            leftRightEdge += ((ConsString)left).maxRightEdge;
        }
        if (right instanceof ConsString) {
            consCount += ((ConsString)right).consCount;
            rightRightEdge += ((ConsString)right).maxRightEdge;
        }
        maxRightEdge = leftRightEdge < rightRightEdge ? rightRightEdge : leftRightEdge;
        // Don't let it grow too deep, can cause out of memory
        if (consCount > 2000) {
            flatten();
        }
    }

    // Replace with string representation when serializing
    private Object writeReplace() {
        return this.toString();
    }
    
    public String toString() {
        return consCount == 0 ? (String)left : flatten();
    }

    private synchronized String flatten() {
        if (consCount != 0) {
            left = flattenInternal();
            right = "";
            consCount = 0;
            maxRightEdge = 0;
        }
        return (String)left;
    }

    private synchronized String flattenInternal() {
        char[] chars = new char[length];
        // necessary to add 1 when leaf is ConsString, 
        CharSequence[] stack = new CharSequence[maxRightEdge + 1];
        int stackPos = 0;
        stack[stackPos++] = left;
        CharSequence cs = right;
        int begin = length;

        while (true) {
            if (cs instanceof ConsString) {
                stack[stackPos++] = ((ConsString)cs).left;
                cs = ((ConsString)cs).right;
            } else {
                begin -= cs.length();
                ((String)cs).getChars(0, cs.length(), chars, begin);
                if (stackPos == 0) {
                    break;
                }
                cs = stack[--stackPos];
            }
        }
        return new String(chars);
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        String str = consCount == 0 ? (String)left : flatten();
        return str.charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        String str = consCount == 0 ? (String)left : flatten();
        return str.substring(start, end);
    }

}
