package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.IncorrectParameterTypeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 不可迭代参数
 *
 * @author ash
 */
public class ASTNonIterableParameter extends ValuableParameter {


    public ASTNonIterableParameter(int i) {
        super(i);
    }

    public ASTNonIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void setParameter(String parameter) {
        Pattern p = Pattern.compile(":(\\w+)(\\.\\w+)*");
        Matcher m = p.matcher(parameter);
        if (!m.matches()) {
            throw new RuntimeException(""); // TODO
        }
        parameterName = m.group(1);
        propertyPath = parameter.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
        fullName = parameter;
    }

    @Override
    public void checkType(TypeContext context) {
        Type type = context.getPropertyType(parameterName, propertyPath);
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null || typeToken.isIterable() || !JdbcUtils.isSingleColumnClass(mappedClass)) {
            throw new IncorrectParameterTypeException("invalid type of " + fullName + ", " +
                    "need a single column class but " + type);
        }
    }

    @Override
    public String toString() {
        return fullName;
    }

}
