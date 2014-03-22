package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.DB;
import cc.concurrent.mango.DataSourceFactory;
import cc.concurrent.mango.Rename;
import cc.concurrent.mango.jdbc.JdbcTemplate;
import cc.concurrent.mango.runtime.*;
import cc.concurrent.mango.util.Strings;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected JdbcTemplate jdbcTemplate;

    private DbDescriptor dbDescriptor;
    private DataSourceFactory dataSourceFactory;
    private SQLType sqlType;
    private String[] aliases;

    private final static String TABLE = "table";

    protected AbstractOperator(Method method, SQLType sqlType) {
        this.jdbcTemplate = new JdbcTemplate();
        this.sqlType = sqlType;
        buildAliases(method);
        buildDbDescriptor(method);
    }

    @Override
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    protected TypeContext buildTypeContext(Type[] methodArgTypes) {
        Map<String, Type> parameterTypeMap = new HashMap<String, Type>();
        String table = dbDescriptor.getTable();
        if (!Strings.isNullOrEmpty(table)) { // 在@DB中设置过全局表名
            parameterTypeMap.put(TABLE, String.class);
        }
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(getParameterNameByIndex(i), methodArgTypes[i]);
        }
        return new TypeContextImpl(parameterTypeMap);
    }

    protected RuntimeContext buildRuntimeContext(Object[] methodArgs) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        String table = dbDescriptor.getTable();
        if (!Strings.isNullOrEmpty(table)) { // 在@DB中设置过全局表名
            parameters.put(TABLE, table);
        }
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(getParameterNameByIndex(i), methodArgs[i]);
        }
        return new RuntimeContextImpl(parameters);
    }

    protected DataSource getDataSource() {
        return dataSourceFactory.getDataSource(dbDescriptor.getDataSourceName(), sqlType);
    }

    protected String getParameterNameByIndex(int index) {
        String alias = aliases[index];
        return alias != null ? alias : String.valueOf(index + 1);
    }

    private void buildAliases(Method method) {
        Annotation[][] pass = method.getParameterAnnotations();
        aliases = new String[pass.length];
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (Rename.class.equals(pa.annotationType())) {
                    aliases[i] = ((Rename) pa).value();
                }
            }
        }
    }

    private void buildDbDescriptor(Method method) {
        String dataSourceName = "";
        String table = "";
        DB dbAnno = method.getDeclaringClass().getAnnotation(DB.class);
        if (dbAnno != null) {
            dataSourceName = dbAnno.dataSource();
            table = dbAnno.table();
        }
        dbDescriptor = new DbDescriptor(dataSourceName, table);
    }

}
