package org.jfaster.mango.invoker;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public abstract class SetterFunction<I, O> {

    @Nullable
    public abstract O apply(@Nullable I input, Type runtimeOutputType);

    public abstract boolean outputTypeIsGeneric();

}
