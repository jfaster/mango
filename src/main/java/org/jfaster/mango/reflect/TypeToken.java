/*
 * Copyright (C) 2006 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfaster.mango.reflect;

import org.jfaster.mango.util.Primitives;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Type} with generics.
 * <p/>
 * <p>Operations that are otherwise only available in {@link Class} are implemented to support
 * {@code Type}, for example {@link #isAssignableFrom}, {@link #isArray} and {@link
 * #getComponentType}. It also provides additional utilities such as {@link #getTypes} and {@link
 * #resolveType} etc.
 * <p/>
 * <p>There are three ways to get a {@code TypeToken} instance: <ul>
 * <li>Wrap a {@code Type} obtained via reflection. For example: {@code
 * TypeToken.of(method.getGenericReturnType())}.
 * <li>Capture a generic type with a (usually anonymous) subclass. For example: <pre>   {@code
 *   new TypeToken<List<String>>() {}}</pre>
 * <p>Note that it's critical that the actual type argument is carried by a subclass.
 * The following code is wrong because it only captures the {@code <T>} type variable
 * of the {@code listType()} method signature; while {@code <String>} is lost in erasure:
 * <pre>   {@code
 *   class Util {
 *     static <T> TypeToken<List<T>> listType() {
 *       return new TypeToken<List<T>>() {};
 *     }
 *   }
 * <p/>
 *   TypeToken<List<String>> stringListType = Util.<String>listType();}</pre>
 * <li>Capture a generic type with a (usually anonymous) subclass and resolve it against
 * a context class that knows what the type parameters are. For example: <pre>   {@code
 *   abstract class IKnowMyType<T> {
 *     TypeToken<T> type = new TypeToken<T>(getClass()) {};
 *   }
 *   new IKnowMyType<String>() {}.type => String}</pre>
 * </ul>
 * <p/>
 * <p>{@code TypeToken} is serializable when no type variable is contained in the type.
 * <p/>
 * <p>Note to Guice users: {@code} TypeToken is similar to Guice's {@code TypeLiteral} class
 * except that it is serializable and offers numerous additional utility methods.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Ben Yu
 * @since 12.0
 */
@SuppressWarnings("serial") // SimpleTypeToken is the serialized form.
public abstract class TypeToken<T> extends TypeCapture<T> implements Serializable {

    private final Type runtimeType;

    /**
     * Resolver for resolving types with {@link #runtimeType} as context.
     */
    private transient TypeResolver typeResolver;

    /**
     * Constructs a new type token of {@code T}.
     * <p/>
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute
     * it at runtime despite erasure.
     * <p/>
     * <p>For example: <pre>   {@code
     *   TypeToken<List<String>> t = new TypeToken<List<String>>() {};}</pre>
     */
    protected TypeToken() {
        this.runtimeType = capture();
        if (runtimeType instanceof TypeVariable) {
            throw new IllegalStateException(String.format("Cannot construct a TypeToken for a type variable.\n" +
                    "You probably meant to call new TypeToken<%s>(getClass()) " +
                    "that can resolve the type variable for you.\n" +
                    "If you do need to create a TypeToken of a type variable, " +
                    "please use TypeToken.of() instead.", runtimeType));
        }
    }

    /**
     * Constructs a new type token of {@code T} while resolving free type variables in the context of
     * {@code declaringClass}.
     * <p/>
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute
     * it at runtime despite erasure.
     * <p/>
     * <p>For example: <pre>   {@code
     *   abstract class IKnowMyType<T> {
     *     TypeToken<T> getMyType() {
     *       return new TypeToken<T>(getClass()) {};
     *     }
     *   }
     * <p/>
     *   new IKnowMyType<String>() {}.getMyType() => String}</pre>
     */
    protected TypeToken(Class<?> declaringClass) {
        Type captured = super.capture();
        if (captured instanceof Class) {
            this.runtimeType = captured;
        } else {
            this.runtimeType = of(declaringClass).resolveType(captured).runtimeType;
        }
    }

    private TypeToken(Type type) {
        this.runtimeType = type;
    }

    /**
     * Returns an instance of type token that wraps {@code type}.
     */
    public static <T> TypeToken<T> of(Class<T> type) {
        return new SimpleTypeToken<T>(type);
    }

    /**
     * Returns an instance of type token that wraps {@code type}.
     */
    public static TypeToken<?> of(Type type) {
        return new SimpleTypeToken<Object>(type);
    }

    /**
     * Returns the raw type of {@code T}. Formally speaking, if {@code T} is returned by
     * {@link java.lang.reflect.Method#getGenericReturnType}, the raw type is what's returned by
     * {@link java.lang.reflect.Method#getReturnType} of the same method object. Specifically:
     * <ul>
     * <li>If {@code T} is a {@code Class} itself, {@code T} itself is returned.
     * <li>If {@code T} is a {@link ParameterizedType}, the raw type of the parameterized type is
     * returned.
     * <li>If {@code T} is a {@link GenericArrayType}, the returned type is the corresponding array
     * class. For example: {@code List<Integer>[] => List[]}.
     * <li>If {@code T} is a type variable or a wildcard type, the raw type of the first upper bound
     * is returned. For example: {@code <X extends Foo> => Foo}.
     * </ul>
     */
    public final Class<? super T> getRawType() {
        Class<?> rawType = getRawType(runtimeType);
        @SuppressWarnings("unchecked") // raw type is |T|
                Class<? super T> result = (Class<? super T>) rawType;
        return result;
    }

    /**
     * Returns the represented type.
     */
    public final Type getType() {
        return runtimeType;
    }

    /**
     * <p>Resolves the given {@code type} against the type context represented by this type.
     * For example: <pre>   {@code
     *   new TypeToken<List<String>>() {}.resolveType(
     *       List.class.getMethod("get", int.class).getGenericReturnType())
     *   => String.class}</pre>
     */
    public final TypeToken<?> resolveType(Type type) {
        TypeResolver resolver = typeResolver;
        if (resolver == null) {
            resolver = (typeResolver = TypeResolver.accordingTo(runtimeType));
        }
        return of(resolver.resolveType(type));
    }

    private Type[] resolveInPlace(Type[] types) {
        for (int i = 0; i < types.length; i++) {
            types[i] = resolveType(types[i]).getType();
        }
        return types;
    }

    private TypeToken<?> resolveSupertype(Type type) {
        TypeToken<?> supertype = resolveType(type);
        // super types' type mapping is a subset of type mapping of this type.
        supertype.typeResolver = typeResolver;
        return supertype;
    }

    /**
     * Returns the generic superclass of this type or {@code null} if the type represents
     * {@link Object} or an interface. This method is similar but different from {@link
     * Class#getGenericSuperclass}. For example, {@code
     * new TypeToken<StringArrayList>() {}.getGenericSuperclass()} will return {@code
     * new TypeToken<ArrayList<String>>() {}}; while {@code
     * StringArrayList.class.getGenericSuperclass()} will return {@code ArrayList<E>}, where {@code E}
     * is the type variable declared by class {@code ArrayList}.
     * <p/>
     * <p>If this type is a type variable or wildcard, its first upper bound is examined and returned
     * if the bound is a class or extends from a class. This means that the returned type could be a
     * type variable too.
     */
    @Nullable
    final TypeToken<? super T> getGenericSuperclass() {
        if (runtimeType instanceof TypeVariable) {
            // First bound is always the super class, if one exists.
            return boundAsSuperclass(((TypeVariable<?>) runtimeType).getBounds()[0]);
        }
        if (runtimeType instanceof WildcardType) {
            // wildcard has one and only one upper bound.
            return boundAsSuperclass(((WildcardType) runtimeType).getUpperBounds()[0]);
        }
        Type superclass = getRawType().getGenericSuperclass();
        if (superclass == null) {
            return null;
        }
        @SuppressWarnings("unchecked") // super class of T
                TypeToken<? super T> superToken = (TypeToken<? super T>) resolveSupertype(superclass);
        return superToken;
    }

    @Nullable
    private TypeToken<? super T> boundAsSuperclass(Type bound) {
        TypeToken<?> token = of(bound);
        if (token.getRawType().isInterface()) {
            return null;
        }
        @SuppressWarnings("unchecked") // only upper bound of T is passed in.
                TypeToken<? super T> superclass = (TypeToken<? super T>) token;
        return superclass;
    }

    /**
     * Returns true if this type is assignable from the given {@code type}.
     */
    public final boolean isAssignableFrom(TypeToken<?> type) {
        return isAssignableFrom(type.runtimeType);
    }

    /**
     * Check if this type is assignable from the given {@code type}.
     */
    public final boolean isAssignableFrom(Type type) {
        return isAssignable(type, runtimeType);
    }

    /**
     * Returns true if this type is known to be an array type, such as {@code int[]}, {@code T[]},
     * {@code <? extends Map<String, Integer>[]>} etc.
     */
    public final boolean isArray() {
        return getComponentType() != null;
    }

    /**
     * Returns true if this type is one of the nine primitive types (including {@code void}).
     *
     * @since 15.0
     */
    public final boolean isPrimitive() {
        return (runtimeType instanceof Class) && ((Class<?>) runtimeType).isPrimitive();
    }

    /**
     * Returns the corresponding wrapper type if this is a primitive type; otherwise returns
     * {@code this} itself. Idempotent.
     *
     * @since 15.0
     */
    public final TypeToken<T> wrap() {
        if (isPrimitive()) {
            @SuppressWarnings("unchecked") // this is a primitive class
                    Class<T> type = (Class<T>) runtimeType;
            return TypeToken.of(Primitives.wrap(type));
        }
        return this;
    }

    private boolean isWrapper() {
        return Primitives.allWrapperTypes().contains(runtimeType);
    }

    /**
     * Returns the corresponding primitive type if this is a wrapper type; otherwise returns
     * {@code this} itself. Idempotent.
     *
     * @since 15.0
     */
    public final TypeToken<T> unwrap() {
        if (isWrapper()) {
            @SuppressWarnings("unchecked") // this is a wrapper class
                    Class<T> type = (Class<T>) runtimeType;
            return TypeToken.of(Primitives.unwrap(type));
        }
        return this;
    }

    /**
     * Returns the array component type if this type represents an array ({@code int[]}, {@code T[]},
     * {@code <? extends Map<String, Integer>[]>} etc.), or else {@code null} is returned.
     */
    @Nullable
    public final TypeToken<?> getComponentType() {
        Type componentType = Types.getComponentType(runtimeType);
        if (componentType == null) {
            return null;
        }
        return of(componentType);
    }

    /**
     * Returns true if {@code o} is another {@code TypeToken} that represents the same {@link Type}.
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof TypeToken) {
            TypeToken<?> that = (TypeToken<?>) o;
            return runtimeType.equals(that.runtimeType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return runtimeType.hashCode();
    }

    @Override
    public String toString() {
        return Types.toString(runtimeType);
    }

    /**
     * Implemented to support serialization of subclasses.
     */
    protected Object writeReplace() {
        // TypeResolver just transforms the type to our own impls that are Serializable
        // except TypeVariable.
        return of(new TypeResolver().resolveType(runtimeType));
    }

    /**
     * Ensures that this type token doesn't contain type variables, which can cause unchecked type
     * errors for callers like {@link TypeToInstanceMap}.
     */
    final TypeToken<T> rejectTypeVariables() {
        new TypeVisitor() {
            @Override
            void visitTypeVariable(TypeVariable<?> type) {
                throw new IllegalArgumentException(
                        runtimeType + "contains a type variable and is not safe for the operation");
            }

            @Override
            void visitWildcardType(WildcardType type) {
                visit(type.getLowerBounds());
                visit(type.getUpperBounds());
            }

            @Override
            void visitParameterizedType(ParameterizedType type) {
                visit(type.getActualTypeArguments());
                visit(type.getOwnerType());
            }

            @Override
            void visitGenericArrayType(GenericArrayType type) {
                visit(type.getGenericComponentType());
            }
        }.visit(runtimeType);
        return this;
    }

    private static boolean isAssignable(Type from, Type to) {
        if (to.equals(from)) {
            return true;
        }
        if (to instanceof WildcardType) {
            return isAssignableToWildcardType(from, (WildcardType) to);
        }
        // if "from" is type variable, it's assignable if any of its "extends"
        // bounds is assignable to "to".
        if (from instanceof TypeVariable) {
            return isAssignableFromAny(((TypeVariable<?>) from).getBounds(), to);
        }
        // if "from" is wildcard, it'a assignable to "to" if any of its "extends"
        // bounds is assignable to "to".
        if (from instanceof WildcardType) {
            return isAssignableFromAny(((WildcardType) from).getUpperBounds(), to);
        }
        if (from instanceof GenericArrayType) {
            return isAssignableFromGenericArrayType((GenericArrayType) from, to);
        }
        // Proceed to regular Type assignability check
        if (to instanceof Class) {
            return isAssignableToClass(from, (Class<?>) to);
        } else if (to instanceof ParameterizedType) {
            return isAssignableToParameterizedType(from, (ParameterizedType) to);
        } else if (to instanceof GenericArrayType) {
            return isAssignableToGenericArrayType(from, (GenericArrayType) to);
        } else { // to instanceof TypeVariable
            return false;
        }
    }

    private static boolean isAssignableFromAny(Type[] fromTypes, Type to) {
        for (Type from : fromTypes) {
            if (isAssignable(from, to)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAssignableToClass(Type from, Class<?> to) {
        return to.isAssignableFrom(getRawType(from));
    }

    private static boolean isAssignableToWildcardType(
            Type from, WildcardType to) {
        // if "to" is <? extends Foo>, "from" can be:
        // Foo, SubFoo, <? extends Foo>, <? extends SubFoo>, <T extends Foo> or
        // <T extends SubFoo>.
        // if "to" is <? super Foo>, "from" can be:
        // Foo, SuperFoo, <? super Foo> or <? super SuperFoo>.
        return isAssignable(from, supertypeBound(to)) && isAssignableBySubtypeBound(from, to);
    }

    private static boolean isAssignableBySubtypeBound(Type from, WildcardType to) {
        Type toSubtypeBound = subtypeBound(to);
        if (toSubtypeBound == null) {
            return true;
        }
        Type fromSubtypeBound = subtypeBound(from);
        if (fromSubtypeBound == null) {
            return false;
        }
        return isAssignable(toSubtypeBound, fromSubtypeBound);
    }

    private static boolean isAssignableToParameterizedType(Type from, ParameterizedType to) {
        Class<?> matchedClass = getRawType(to);
        if (!matchedClass.isAssignableFrom(getRawType(from))) {
            return false;
        }
        Type[] typeParams = matchedClass.getTypeParameters();
        Type[] toTypeArgs = to.getActualTypeArguments();
        TypeToken<?> fromTypeToken = of(from);
        for (int i = 0; i < typeParams.length; i++) {
            // If "to" is "List<? extends CharSequence>"
            // and "from" is StringArrayList,
            // First step is to figure out StringArrayList "is-a" List<E> and <E> is
            // String.
            // typeParams[0] is E and fromTypeToken.get(typeParams[0]) will resolve to
            // String.
            // String is then matched against <? extends CharSequence>.
            Type fromTypeArg = fromTypeToken.resolveType(typeParams[i]).runtimeType;
            if (!matchTypeArgument(fromTypeArg, toTypeArgs[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignableToGenericArrayType(Type from, GenericArrayType to) {
        if (from instanceof Class) {
            Class<?> fromClass = (Class<?>) from;
            if (!fromClass.isArray()) {
                return false;
            }
            return isAssignable(fromClass.getComponentType(), to.getGenericComponentType());
        } else if (from instanceof GenericArrayType) {
            GenericArrayType fromArrayType = (GenericArrayType) from;
            return isAssignable(fromArrayType.getGenericComponentType(), to.getGenericComponentType());
        } else {
            return false;
        }
    }

    private static boolean isAssignableFromGenericArrayType(GenericArrayType from, Type to) {
        if (to instanceof Class) {
            Class<?> toClass = (Class<?>) to;
            if (!toClass.isArray()) {
                return toClass == Object.class; // any T[] is assignable to Object
            }
            return isAssignable(from.getGenericComponentType(), toClass.getComponentType());
        } else if (to instanceof GenericArrayType) {
            GenericArrayType toArrayType = (GenericArrayType) to;
            return isAssignable(from.getGenericComponentType(), toArrayType.getGenericComponentType());
        } else {
            return false;
        }
    }

    private static boolean matchTypeArgument(Type from, Type to) {
        if (from.equals(to)) {
            return true;
        }
        if (to instanceof WildcardType) {
            return isAssignableToWildcardType(from, (WildcardType) to);
        }
        return false;
    }

    private static Type supertypeBound(Type type) {
        if (type instanceof WildcardType) {
            return supertypeBound((WildcardType) type);
        }
        return type;
    }

    private static Type supertypeBound(WildcardType type) {
        Type[] upperBounds = type.getUpperBounds();
        if (upperBounds.length == 1) {
            return supertypeBound(upperBounds[0]);
        } else if (upperBounds.length == 0) {
            return Object.class;
        } else {
            throw new AssertionError(
                    "There should be at most one upper bound for wildcard type: " + type);
        }
    }

    @Nullable
    private static Type subtypeBound(Type type) {
        if (type instanceof WildcardType) {
            return subtypeBound((WildcardType) type);
        } else {
            return type;
        }
    }

    @Nullable
    private static Type subtypeBound(WildcardType type) {
        Type[] lowerBounds = type.getLowerBounds();
        if (lowerBounds.length == 1) {
            return subtypeBound(lowerBounds[0]);
        } else if (lowerBounds.length == 0) {
            return null;
        } else {
            throw new AssertionError(
                    "Wildcard should have at most one lower bound: " + type);
        }
    }

    static Class<?> getRawType(Type type) {
        // For wildcard or type variable, the first bound determines the runtime type.
        return getRawTypes(type).iterator().next();
    }

    static Set<Class<?>> getRawTypes(Type type) {
        final Set<Class<?>> set = new HashSet<Class<?>>();
        new TypeVisitor() {
            @Override
            void visitTypeVariable(TypeVariable<?> t) {
                visit(t.getBounds());
            }

            @Override
            void visitWildcardType(WildcardType t) {
                visit(t.getUpperBounds());
            }

            @Override
            void visitParameterizedType(ParameterizedType t) {
                set.add((Class<?>) t.getRawType());
            }

            @Override
            void visitClass(Class<?> t) {
                set.add(t);
            }

            @Override
            void visitGenericArrayType(GenericArrayType t) {
                set.add(Types.getArrayClass(getRawType(t.getGenericComponentType())));
            }

        }.visit(type);
        return set;
    }

    private static final class SimpleTypeToken<T> extends TypeToken<T> {

        SimpleTypeToken(Type type) {
            super(type);
        }

        private static final long serialVersionUID = 0;
    }

}
