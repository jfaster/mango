package org.jfaster.mango.invoker;

import org.jfaster.mango.exception.NotReadablePropertyException;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.util.Strings;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class HierarchyGetterInvoker implements GetterInvoker  {

    private Type type;
    private Class<?> rawType;
    private String parameterName;
    private String propertyPath;
    private List<GetterInvoker> invokers;

    private HierarchyGetterInvoker(Type type, String parameterName, String propertyPath) {
        this.parameterName = parameterName;
        this.propertyPath = propertyPath;
        invokers = new ArrayList<GetterInvoker>();
        Class<?> rawType = TypeToken.of(type).getRawType();
        if (Strings.isNotEmpty(propertyPath)) {
            NestedProperty np = new NestedProperty();
            NestedProperty pnp = new NestedProperty();
            for (String propertyName : propertyPath.split("\\.")) {
                np.append(propertyName);
                GetterInvoker invoker = InvokerCache.getGetterInvoker(rawType, propertyName);
                if (invoker == null) {
                    String fullName = Strings.getFullName(parameterName, np.getNestedProperty());
                    String pFullName = Strings.getFullName(parameterName, pnp.getNestedProperty());
                    throw new NotReadablePropertyException("property " + fullName + " is not readable, " +
                            "the type of " + pFullName + " is " + type + ", please check it's get method");
                }
                invokers.add(invoker);
                type = invoker.getType();
                rawType = TypeToken.of(type).getRawType();
                pnp.append(propertyName);
            }
        }
        this.type = type;
        this.rawType = rawType;
    }

    public static HierarchyGetterInvoker create(Type type, String parameterName, String propertyPath) {
        return new HierarchyGetterInvoker(type, parameterName, propertyPath);
    }

    @Override
    public Object invoke(@Nullable Object obj) {
        Object r = obj;
        int size = invokers.size();
        for (int i = 0; i < size; i++) {
            if (r == null) {
                NestedProperty np = new NestedProperty();
                for (int j = 0; j < i; j++) {
                    np.append(invokers.get(i).getName());
                }
                String key = i == 0 ? "parameter" : "property";
                String fullName = Strings.getFullName(parameterName, np.getNestedProperty());
                throw new NullPointerException(key + " " + fullName + " is null");
            }
            r = invokers.get(i).invoke(r);
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

    private static class NestedProperty {

        private StringBuilder nestedProperty = new StringBuilder();
        private int num = 0;

        public void append(String property) {
            if (num++ == 0) {
                nestedProperty.append(property);
            } else {
                nestedProperty.append("." + property);
            }
        }

        public String getNestedProperty() {
            return nestedProperty.toString();
        }

    }


}
