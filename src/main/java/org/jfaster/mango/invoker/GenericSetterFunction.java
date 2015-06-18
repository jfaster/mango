package org.jfaster.mango.invoker;

/**
 * @author ash
 */
public abstract class GenericSetterFunction<I, O> extends SetterFunction<I, O> {

    @Override
    public boolean outputTypeIsGeneric() {
        return true;
    }

}
