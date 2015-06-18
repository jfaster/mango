package org.jfaster.mango.invoker;

import javax.annotation.Nullable;

/**
 * @author ash
 */
public abstract class GetterFunction<I, O> {

    @Nullable
    public abstract O apply(@Nullable I input);

}
