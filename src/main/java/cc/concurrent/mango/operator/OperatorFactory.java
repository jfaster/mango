package cc.concurrent.mango.operator;

import cc.concurrent.mango.SQL;
import cc.concurrent.mango.exception.structure.IncorrectReturnTypeException;
import cc.concurrent.mango.exception.structure.IncorrectSqlException;
import cc.concurrent.mango.exception.structure.NoSqlAnnotationException;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.Parser;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Operator工厂
 *
 * @author ash
 */
public class OperatorFactory {

    private final static Pattern INSERT_PATTERN = Pattern.compile("^\\s*INSERT\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern DELETE_PATTERN = Pattern.compile("^\\s*DELETE\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern UPDATE_PATTERN = Pattern.compile("^\\s*UPDATE\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);


    /**
     * 获取Operator
     *
     * @param method
     * @return
     * @throws Exception
     */
    public static Operator getOperator(Method method) throws Exception {
        SQL sqlAnno = method.getAnnotation(SQL.class);
        if (sqlAnno == null) {
            throw new NoSqlAnnotationException("expected cc.concurrent.mango.SQL annotation on method");
        }
        String sql = sqlAnno.value();
        if (Strings.isNullOrEmpty(sql)) {
            throw new IncorrectSqlException("sql is null or empty");
        }
        ASTRootNode rootNode = new Parser(sql).parse();
        SQLType sqlType = getSQLType(sql);

        if (sqlType == SQLType.SELECT) {
            return QueryOperator.create(rootNode, method, sqlType);
        } else if (int.class.equals(method.getReturnType())) {
            return UpdateOperator.create(rootNode, method, sqlType);
        } else if (int[].class.equals(method.getReturnType())) {
            return BatchUpdateOperator.create(rootNode, method, sqlType);
        } else {
            throw new IncorrectReturnTypeException("return type expected int or int[] but " + method.getReturnType());
        }
    }

    private static SQLType getSQLType(String sql) {
        if (INSERT_PATTERN.matcher(sql).find()) {
            return SQLType.INSERT;
        } else if (DELETE_PATTERN.matcher(sql).find()) {
            return SQLType.DELETE;
        } else if (UPDATE_PATTERN.matcher(sql).find()) {
            return SQLType.UPDATE;
        } else if (SELECT_PATTERN.matcher(sql).find()) {
            return SQLType.SELECT;
        } else {
            throw new IncorrectSqlException("sql must start with INSERT or DELETE or UPDATE or SELECT");
        }
    }

}
