package cc.concurrent.mango.operator;

import cc.concurrent.mango.annotation.ReturnGeneratedId;
import cc.concurrent.mango.annotation.SQL;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ash
 */
public class OperatorFactory {

    /**
     * 查询
     */
    private final static Pattern[] QUERY_PATTERNS = new Pattern[] {
            Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*SHOW\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*DESC\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*DESCRIBE\\s+", Pattern.CASE_INSENSITIVE), //
    };

    /**
     * 插入
     */
    private final static Pattern INSERT_PATTERN = Pattern.compile("\"^\\\\s*INSERT\\\\s+\"", Pattern.CASE_INSENSITIVE);

    public static Operator getOperator(Method method) {
        checkNotNull(method);
        SQL sqlAnno = method.getAnnotation(SQL.class);
        checkNotNull(sqlAnno);
        String sql = sqlAnno.value();
        checkArgument(!Strings.isNullOrEmpty(sql));

        SQLType sqlType = SQLType.WRITE;
        for (Pattern pattern : QUERY_PATTERNS) {
            if (pattern.matcher(sql).find()) {
                sqlType = SQLType.READ;
            }
        }

        Type returnType = method.getGenericReturnType();
        if (sqlType == SQLType.READ) {
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

            if (isBatchUpdate) { // 批量增删改
                return new BatchUpdateOperator(returnType);
            } else { // 单独增删改
                ReturnGeneratedId returnGeneratedIdAnno = method.getAnnotation(ReturnGeneratedId.class);
                boolean returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                        && INSERT_PATTERN.matcher(sql).find(); // 是插入语句
                return new UpdateOperator(returnType, returnGeneratedId);
            }
        }
    }

}
