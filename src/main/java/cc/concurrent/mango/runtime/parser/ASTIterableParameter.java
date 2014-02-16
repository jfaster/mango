package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.runtime.TypeContext;
import com.google.common.base.CharMatcher;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * 可迭代参数
 *
 * @author ash
 */
public class ASTIterableParameter extends ValuableParameter {

    private String field; // "a in (:1)"中的a

    public ASTIterableParameter(int i) {
        super(i);
    }

    public ASTIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void setField(String field) {
        this.field = field;
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
        Class<?> type = context.getPropertyType(beanName, propertyPath);
        if (!Collection.class.isAssignableFrom(type) && !type.isArray()) { // 不是集合或数组抛出异常
            throw new IncorrectParameterTypeException(getLocation() + " " + literal() +
                    " expected Collection or Array but " + type.getName());
        }
    }

}
