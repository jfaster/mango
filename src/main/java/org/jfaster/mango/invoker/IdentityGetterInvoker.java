package org.jfaster.mango.invoker;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public class IdentityGetterInvoker implements GetterInvoker {

    private Type type;
    private Class<?> rawType;

    public IdentityGetterInvoker(Type type) {
        this.type = type;
        this.rawType = TypeToken.of(type).getRawType();
    }

    @Override
    public Object invoke(Object object) {
        return object;
    }

    @Override
    public boolean isIdentity() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getRawType() {
        return rawType;
    }

}
