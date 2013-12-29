package cc.concurrent.mango;

import java.lang.annotation.*;

/**
 * @author ash
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReturnGeneratedId {
}
