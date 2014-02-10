package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.EmptyParameterException;
import cc.concurrent.mango.exception.NullParameterException;
import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.Iterables;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class ASTIterableParameter extends ASTExpressionNode {

    private String beanName;
    private String propertyName; // 为""的时候表示没有属性
    //private String field; // "a in (:1)"中的a

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
        propertyName = toRemove.removeFrom(param.substring(m.end(1))); // 去掉空格与)
        if (!propertyName.isEmpty()) {
            propertyName = propertyName.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public Object value(RuntimeContext context) {
        Object v = context.getPropertyValue(beanName, propertyName);
        if (v == null) {
            throw new NullParameterException("parameter " + getParamName() + " can't be null");
        }
        Iterables iterables = new Iterables(v);
        if (!iterables.isIterable()) {
            throw new IncorrectParameterTypeException("expected collection or array but " + v.getClass());
        }
        if (iterables.isEmpty()) {
            throw new EmptyParameterException("batchUpdate's parameter can't be empty");
        }

        List<Object> values = Lists.newArrayList();
        for (Object obj : iterables) {
            values.add(obj);
        }
        return values;
    }

    @Override
    public void checkType(TypeContext context) {
        Class<?> type = context.getPropertyType(beanName, propertyName);
        checkNotNull(type);
        boolean isIterable = false;
        if (Collection.class.isAssignableFrom(type)) { // 集合
            isIterable = true;
        } else if (type.isArray()) { // 数组
            isIterable = true;
        }
        if (!isIterable) {
            throw new IncorrectParameterTypeException("need iterableClass but " + type);
        }
    }

    public Class<?> type(TypeContext context) {
        return context.getPropertyType(beanName, propertyName);
    }

    private String getParamName() {
        return ":" + beanName + (propertyName.isEmpty() ? "" : "." + propertyName);
    }

}
