package cc.concurrent.mango.operator;

import cc.concurrent.mango.annotation.SQL;
import com.google.common.base.Strings;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

/**
 * @author ash
 */
public class OperatorFactory {

    private static Pattern QUERY_PATTERN = Pattern.compile("^\\s*select\\s+", Pattern.CASE_INSENSITIVE);

    public static Operator getOperator(Method method) {
        checkNotNull(method);
        SQL anno = method.getAnnotation(SQL.class);
        checkNotNull(anno);
        String sql = anno.value();
        checkArgument(!Strings.isNullOrEmpty(sql));

        TypeToken returnType = Invokable.from(method).getReturnType();
        if (QUERY_PATTERN.matcher(sql).find()) {
            return new QueryOperator(returnType);
        } else {
            Class<?>[] arameterTypes = method.getParameterTypes();
            boolean isBatchUpdate = false;
            if (arameterTypes.length == 1) {
                Class<?> arameterType = arameterTypes[0];
                if (Collection.class.isAssignableFrom(arameterType)) {
                    isBatchUpdate = true;
                }
            }
            return isBatchUpdate ? new BatchUpdateOperator(returnType) : new UpdateOperator(returnType);
        }
    }

}
