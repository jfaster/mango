package cc.concurrent.mango.operator;

import com.google.common.reflect.TypeToken;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected final TypeToken returnType;

    protected JdbcTemplate jdbcTemplate;

    protected AbstractOperator(TypeToken returnType) {
        this.returnType = returnType;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
