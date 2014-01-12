package cc.concurrent.mango.util;

import java.lang.reflect.*;

/**
 * @author ash
 */
public class ToStringHelper {

    public static String toString(Method m) {
        StringBuffer sb = new StringBuffer();
        sb.append(m.getDeclaringClass().getSimpleName()).append(".").append(m.getName()).append("(");
        printTypes(sb, m.getGenericParameterTypes(), "", ", ", "");
        return sb.append(")").toString();
    }


    public static void printTypes(StringBuffer sb, Type[] types, String pre, String sep, String suf) {
        if (types.length > 0) {
            sb.append(pre);
        }
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(sep);
            }
            printType(sb, types[i]);
        }
        if (types.length > 0) {
            sb.append(suf);
        }
    }

    private static void printType(StringBuffer sb, Type type) {
        if (type instanceof Class) {
            Class t = (Class) type;
            sb.append(t.getSimpleName());
        } else if (type instanceof TypeVariable) {
            TypeVariable t = (TypeVariable) type;
            sb.append(t.getName());
            printTypes(sb, t.getBounds(), " extends ", " & ", "");
        } else if (type instanceof WildcardType) {
            WildcardType t = (WildcardType) type;
            sb.append("?");
            printTypes(sb, t.getLowerBounds(), " extends ", " & ", "");
            printTypes(sb, t.getUpperBounds(), " super ", " & ", "");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            Type owner = t.getOwnerType();
            if (owner != null) {
                printType(sb, owner);
                sb.append(".");
            }
            printType(sb, t.getRawType());
            printTypes(sb, t.getActualTypeArguments(), "<", ", ", ">");
        } else if (type instanceof GenericArrayType) {
            GenericArrayType t = (GenericArrayType) type;
            sb.append("");
            printType(sb, t.getGenericComponentType());
            sb.append("[]");
        }
    }


}
