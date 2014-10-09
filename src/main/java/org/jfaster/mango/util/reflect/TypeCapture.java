package org.jfaster.mango.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
abstract class TypeCapture<T> {

    final Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(superclass + " isn't parameterized");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

}
