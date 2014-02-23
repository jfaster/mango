package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;
import com.google.common.base.Strings;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

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
        checkState(m.matches());
        beanName = m.group(1);
        propertyPath = parameter.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public void checkType(TypeContext context) {
        Type type = context.getPropertyType(beanName, propertyPath);
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null) {
            throw new RuntimeException(""); // TODO Exception
        }
        if (typeToken.isIterable()) {
            throw new RuntimeException(""); // TODO Exception
        }
        if (!JdbcUtils.isSingleColumnClass(mappedClass)) {
            // TODO 合适的Exception 包含 java.util.Date
            throw new IncorrectParameterTypeException("need single colum class but " + type);
        }
    }

    @Override
    public String toString() {
        return ":" + (Strings.isNullOrEmpty(propertyPath) ? beanName : beanName + propertyPath);
    }

}
