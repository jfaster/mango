package org.jfaster.mango.invoker;

import com.google.common.reflect.TypeToken;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class HierarchyGetterInvoker implements GetterInvoker  {

    private Type type;
    private Class<?> rawType;
    private String propertyPath;
    List<GetterInvoker> invokers;

    private HierarchyGetterInvoker(Type type, String propertyPath) {
        this.propertyPath = propertyPath;
        invokers = new ArrayList<GetterInvoker>();
        Class<?> rawType = TypeToken.of(type).getRawType();
        if (Strings.isNotEmpty(propertyPath)) {
            for (String propertyName : propertyPath.split("\\.")) {
                GetterInvoker invoker = InvokerCache.getGetterInvoker(rawType, propertyName);
                if (invoker == null) {
                    throw new RuntimeException(); // TODO
                }
                invokers.add(invoker);
                type = invoker.getType();
                rawType = TypeToken.of(type).getRawType();
            }
        }
        this.type = type;
        this.rawType = rawType;
    }

    public static HierarchyGetterInvoker create(Type type, String propertyPath) {
        return new HierarchyGetterInvoker(type, propertyPath);
    }

    @Override
    public Object invoke(Object obj) {
        Object r = obj;
        int size = invokers.size();
        for (int i = 0; i < size; i++) {
            r = invokers.get(i).invoke(r);
            if (r == null && i != size - 1) {
                throw new RuntimeException(); // TODO
            }
        }
        return r;
    }

    @Override
    public String getName() {
        return propertyPath;
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
