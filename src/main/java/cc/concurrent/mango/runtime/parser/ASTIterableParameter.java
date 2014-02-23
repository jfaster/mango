package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;
import com.google.common.base.Strings;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * 可迭代参数
 *
 * @author ash
 */
public class ASTIterableParameter extends ValuableParameter {

    private String propertyName; // "a in (:1)"中的a

    public ASTIterableParameter(int i) {
        super(i);
    }

    public ASTIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
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

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void checkType(TypeContext context) {
        Type type = context.getPropertyType(beanName, propertyPath);
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null) {
            throw new RuntimeException(""); // TODO Exception
        }
        if (!typeToken.isIterable()) { // 不是集合或数组抛出异常
            throw new IncorrectParameterTypeException(literal() + " expected Collection or Array but " + type);
        }
        if (!JdbcUtils.isSingleColumnClass(mappedClass)) {
            throw new RuntimeException(""); // TODO Exception
        }
    }

    @Override
    public String toString() {
        return propertyName + " in (:" + (Strings.isNullOrEmpty(propertyPath) ? beanName : beanName + propertyPath) + ")";
    }

}
