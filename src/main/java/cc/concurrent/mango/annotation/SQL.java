package cc.concurrent.mango.annotation;

import java.lang.annotation.*;

/**
 * @author ash
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SQL {

    String value();

}
