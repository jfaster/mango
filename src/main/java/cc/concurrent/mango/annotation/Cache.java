package cc.concurrent.mango.annotation;

import java.lang.annotation.*;

/**
 * @author ash
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
}
