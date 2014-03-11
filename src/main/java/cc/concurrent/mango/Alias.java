package cc.concurrent.mango;

import java.lang.annotation.*;

/**
 * @author ash
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Alias {

    String value();

}
