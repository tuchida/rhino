/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.javascript;

import java.util.Iterator;
import java.util.List;

public class NativeJavaList extends NativeJavaObject {

    private List<Object> list;

    static void init(ScriptableObject scope, boolean sealed) {
        NativeJavaListIterator.init(scope, sealed);
    }

    @SuppressWarnings("unchecked")
    public NativeJavaList(Scriptable scope, Object list) {
        super(scope, list, list.getClass());
        assert list instanceof List;
        this.list = (List<Object>) list;
    }

    @Override
    public String getClassName() {
        return "JavaList";
    }


    @Override
    public boolean has(String name, Scriptable start) {
        if (name.equals("length")) {
            return true;
        }
        return super.has(name, start);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        if (isWithValidIndex(index)) {
            return true;
        }
        return super.has(index, start);
    }

    @Override
    public boolean has(Symbol key, Scriptable start) {
        if (SymbolKey.IS_CONCAT_SPREADABLE.equals(key)) {
            return true;
        }
        if (SymbolKey.ITERATOR.equals(key)) {
            return true;
        }
        return super.has(key, start);
    }

    @Override
    public Object get(String name, Scriptable start) {
        if ("length".equals(name)) {
            return Integer.valueOf(list.size());
        }
        return super.get(name, start);
    }

    @Override
    public Object get(int index, Scriptable start) {
        if (isWithValidIndex(index)) {
            Context cx = Context.getContext();
            Object obj = list.get(index);
            return cx.getWrapFactory().wrap(cx, this, obj, obj.getClass());
        }
        return Undefined.instance;
    }

    @Override
    public Object get(Symbol key, Scriptable start) {
        if (SymbolKey.IS_CONCAT_SPREADABLE.equals(key)) {
            return Boolean.TRUE;
        }
        if (SymbolKey.ITERATOR.equals(key)) {
            return symbol_iterator;
        }
        return super.get(key, start);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        if (isWithValidIndex(index)) {
            list.set(index, Context.jsToJava(value, Object.class));
            return;
        }
        super.put(index, start, value);
    }

    @Override
    public Object[] getIds() {
        List<?> list = (List<?>) javaObject;
        Object[] result = new Object[list.size()];
        int i = list.size();
        while (--i >= 0) {
            result[i] = Integer.valueOf(i);
        }
        return result;
    }

    private boolean isWithValidIndex(int index) {
        return index >= 0  && index < list.size();
    }

    private static Callable symbol_iterator = (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) -> {
        if (!(thisObj instanceof NativeJavaList)) {
            throw ScriptRuntime.typeErrorById("msg.incompat.call", SymbolKey.ITERATOR);
        }
        return new NativeJavaListIterator(scope, ((NativeJavaList)thisObj).list);
    };

    private static final class NativeJavaListIterator extends ES6Iterator {
        private static final long serialVersionUID = 1L;
        private static final String ITERATOR_TAG = "JavaListIterator";

        static void init(ScriptableObject scope, boolean sealed) {
            ES6Iterator.init(scope, sealed, new NativeJavaListIterator(), ITERATOR_TAG);
        }

        /**
         * Only for constructing the prototype object.
         */
        private NativeJavaListIterator() {
            super();
        }

        NativeJavaListIterator(Scriptable scope, List<Object> list) {
            super(scope, ITERATOR_TAG);
            this.iterator = list.iterator();
        }

        @Override
        public String getClassName() {
            return "Java List Iterator";
        }

        @Override
        protected boolean isDone(Context cx, Scriptable scope) {
            return !iterator.hasNext();
        }

        @Override
        protected Object nextValue(Context cx, Scriptable scope) {
            if (!iterator.hasNext()) {
                return Undefined.instance;
            }
            return iterator.next();
        }

        @Override
        protected String getTag() {
            return ITERATOR_TAG;
        }

        private Iterator<Object> iterator;
    }
}
