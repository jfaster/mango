package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class ASTVariable extends PrimaryExpression {

    private String beanName;
    private String propertyPath; // 为""的时候表示没有属性

    public ASTVariable(int i) {
        super(i);
    }

    public ASTVariable(Parser p, int i) {
        super(p, i);
    }

    public void setParam(String param) {
        Pattern p = Pattern.compile(":(\\w+)(\\.\\w+)*");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        beanName = m.group(1);
        propertyPath = param.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public Object value(RuntimeContext context) {
        return context.getPropertyValue(beanName, propertyPath);
    }

    @Override
    public void checkType(TypeContext context) {
        Class<?> type = context.getPropertyType(beanName, propertyPath);
        if (Collection.class.isAssignableFrom(type)) {
            throw new IncorrectParameterTypeException("need singleColumnClass but " + type);
        }
    }

}