package org.jfaster.mango.invoker;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public abstract class LiteSetterFunction<I, O> extends SetterFunction<I, O> {

    @Nullable
    @Override
    public O apply(@Nullable I input, Type realOutputType) {
        return apply(input);
    }

    @Nullable
    public abstract O apply(@Nullable I input);

    @Override
    public boolean outputTypeIsGeneric() {
        return false;
    }

}
