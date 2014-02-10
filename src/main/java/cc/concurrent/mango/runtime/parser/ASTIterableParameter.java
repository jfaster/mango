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


    public ASTIterableParameter(int i) {
        super(i);
    }

    public ASTIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void setParam(String param) {
        Pattern p = Pattern.compile("in\\s*\\(\\s*:(\\w+)(\\.\\w+)*\\s*\\)");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        beanName = m.group(1);
        CharMatcher toRemove = CharMatcher.is(' ').or(CharMatcher.is(')'));
        propertyPath = toRemove.removeFrom(param.substring(m.end(1))); // 去掉空格与)
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public void checkType(TypeContext context) {
        Class<?> type = context.getPropertyType(beanName, propertyPath);
        checkNotNull(type);
        boolean isIterable = false;
        if (Collection.class.isAssignableFrom(type)) { // 集合
            isIterable = true;
        } else if (type.isArray()) { // 数组
            isIterable = true;
        }
        if (!isIterable) {
            throw new IncorrectParameterTypeException("need iterable class but " + type);
        }
    }

}
