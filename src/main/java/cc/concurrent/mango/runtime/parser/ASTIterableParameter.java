package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.IncorrectParameterTypeException;
import cc.concurrent.mango.exception.UnreachableCodeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 可迭代参数
 *
 * @author ash
 */
public class ASTIterableParameter extends ValuableParameter {

    private String interableProperty; // "a in (:1)"中的a

    public ASTIterableParameter(int i) {
        super(i);
    }

    public ASTIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void setInterableProperty(String interableProperty) {
        this.interableProperty = interableProperty;
    }

    public void setParameter(String parameter) {
        Pattern p = Pattern.compile(":(\\w+)(\\.\\w+)*");
        Matcher m = p.matcher(parameter);
        if (!m.matches()) {
            throw new UnreachableCodeException();
        }
        parameterName = m.group(1);
        propertyPath = parameter.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
        fullName = parameter;
    }

    public String getInterableProperty() {
        return interableProperty;
    }

    @Override
    public void checkType(TypeContext context) {
        Type type = context.getPropertyType(parameterName, propertyPath);
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (!typeToken.isIterable()) { // 不是集合或数组抛出异常
            throw new IncorrectParameterTypeException("invalid type of " + fullName + ", " +
                    "need array or subclass of java.util.Collection but " + type);
        }
        if (mappedClass == null || !JdbcUtils.isSingleColumnClass(mappedClass)) {
            String s = typeToken.isArray() ? "component" : "actual";
            throw new IncorrectParameterTypeException("invalid " + s + " type of " + fullName + ", " +
                    "need a single column class but " + type);
        }
    }

    @Override
    public String toString() {
        return interableProperty + " in (" + fullName + ")";
    }

}
