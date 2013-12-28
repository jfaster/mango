package cc.concurrent.mango.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.*;

/**
 * 将collection和数组的迭代结合在一起
 *
 * @author ash
 */
public class IterableHolder implements Iterable {

    private Collection<?> collection = null;
    private Object array = null;
    private Integer size = null;
    private Object object = null;

    public IterableHolder(Object object) {
        checkNotNull(object);
        if (Collection.class.isAssignableFrom(object.getClass())) { // 集合
            this.collection = (Collection<?>) object;
        } else if (object.getClass().isArray()) { // 数组
            this.array = object;
        }
        this.object = object;
    }

    public boolean isIterable() {
        return collection != null || array != null;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        if (size == null) {
            if (collection != null) {
                size = collection.size();
            } else if (array != null) {
                size = Array.getLength(array);
            } else {
                throw new IllegalArgumentException("class need collection or array but " + object.getClass());
            }
        }
        return size;
    }

    @Override
    public Iterator iterator() {
        if (collection != null) {
            return collection.iterator();
        } else if (array != null) {
            return new ArrayItr();
        } else {
            throw new IllegalArgumentException("class need collection or array but " + object.getClass());
        }
    }

    private class ArrayItr implements Iterator {

        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public Object next() {
            return Array.get(array, cursor++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
