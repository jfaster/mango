package cc.concurrent.mango.util.reflect;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class MethodCache {

    public static Method getReadMethod(Class<?> clazz, String propertyName) {
        return cache.getUnchecked(clazz).getReadMethod(propertyName);
    }

    public static Method getWriteMethod(Class<?> clazz, String propertyName) {
        return cache.getUnchecked(clazz).getWriteMethod(propertyName);
    }

    private static LoadingCache<Class<?>, BeanMethodInfo> cache = CacheBuilder.newBuilder()
            .build(
                    new CacheLoader<Class<?>, BeanMethodInfo>() {
                        public BeanMethodInfo load(Class<?> clazz) throws Exception {
                            return new BeanMethodInfo(clazz);
                        }
                    });


    private static class BeanMethodInfo {

        ImmutableMap<String, Method> readMethodMap;
        ImmutableMap<String, Method> writeMethodMap;

        public BeanMethodInfo(Class<?> clazz) throws Exception {
            ImmutableMap.Builder<String, Method> readBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<String, Method> writeBuilder = ImmutableMap.builder();
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                String name = pd.getName();
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    readBuilder.put(name, readMethod);
                }
                Method writeMethod = pd.getWriteMethod();
                if (writeMethod != null) {
                    writeBuilder.put(name, writeMethod);
                }
            }
            readMethodMap = readBuilder.build();
            writeMethodMap = writeBuilder.build();
        }

        public Method getReadMethod(String propertyName) {
            return readMethodMap.get(propertyName);
        }

        public Method getWriteMethod(String propertyName) {
            return writeMethodMap.get(propertyName);
        }

    }

}
