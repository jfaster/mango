package cc.concurrent.mango;

import java.lang.annotation.*;

/**
 * @author ash
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    String prefix();

}
