package cc.concurrent.mango.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class TypeToken {

    private Class<?> mappedClass = null;
    private boolean isList;
    private boolean isSet;
    private boolean isArray;

    public TypeToken(Type type) {
        if (type instanceof ParameterizedType) { // 参数化类型
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                if (List.class.equals(rawClass)) {
                    isList = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                } else if (Set.class.equals(rawClass)) {
                    isSet = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                }
            }
        } else if (type instanceof Class) { // 没有参数化
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) { // 数组
                isArray = true;
                mappedClass = clazz.getComponentType();
            } else { // 普通类
                mappedClass = clazz;
            }
        }
    }

    public boolean isIterable() {
        return isList || isSet || isArray;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isCollection() {
        return isList || isSet;
    }

    public Class<?> getMappedClass() {
        return mappedClass;
    }

}










