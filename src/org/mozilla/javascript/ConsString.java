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

    private CharSequence s1, s2;
    private final int length;
    private int depth;

    public ConsString(CharSequence str1, CharSequence str2) {
        s1 = str1;
        s2 = str2;
        length = str1.length() + str2.length();
        depth = 1;
        if (str1 instanceof ConsString) {
            depth += ((ConsString)str1).depth;
        }
        if (str2 instanceof ConsString) {
            depth += ((ConsString)str2).depth;
        }
        // Don't let it grow too deep, can cause out of memory
        if (depth > 2000) {
            flatten();
        }
    }

    // Replace with string representation when serializing
    private Object writeReplace() {
        return this.toString();
    }
    
    public String toString() {
        return depth == 0 ? (String)s1 : flatten();
    }

    private synchronized String flatten() {
        if (depth != 0) {
            s1 = flattenInternal();
            s2 = "";
            depth = 0;
        }
        return (String)s1;
    }

    private synchronized String flattenInternal() {
        char[] chars = new char[length];
        ArrayDeque<CharSequence> stack = new ArrayDeque<CharSequence>();
        stack.addLast(s1);
        CharSequence cs = s2;
        int begin = length;

        while (true) {
            if (cs instanceof ConsString) {
                stack.addLast(((ConsString)cs).s1);
                cs = ((ConsString)cs).s2;
            } else {
                begin -= cs.length();
                ((String)cs).getChars(0, cs.length(), chars, begin);
                if (stack.isEmpty()) {
                    break;
                }
                cs = stack.pollLast();
            }
        }
        return new String(chars);
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        String str = depth == 0 ? (String)s1 : flatten();
        return str.charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        String str = depth == 0 ? (String)s1 : flatten();
        return str.substring(start, end);
    }

}
