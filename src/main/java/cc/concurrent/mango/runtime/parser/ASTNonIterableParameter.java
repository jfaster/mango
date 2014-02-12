package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.TypeContext;

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
    public void checkType(TypeContext context) {
        Class<?> type = context.getPropertyType(beanName, propertyPath);
        if (!JdbcUtils.isSingleColumnClass(type)) {
            throw new IncorrectParameterTypeException("need single colum class but " + type);
        }
    }

}
