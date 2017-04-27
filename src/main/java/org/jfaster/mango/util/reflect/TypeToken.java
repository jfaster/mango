/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.util.reflect;

import org.jfaster.mango.util.Primitives;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

public abstract class TypeToken<T> extends TypeCapture<T> implements Serializable {

  private final Type runtimeType;

  private transient TypeResolver typeResolver;

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

  private TypeToken(Type type) {
    this.runtimeType = type;
  }

  public static <T> TypeToken<T> of(Class<T> type) {
    return new SimpleTypeToken<T>(type);
  }

  public static TypeToken<?> of(Type type) {
    return new SimpleTypeToken<Object>(type);
  }

  public final Class<? super T> getRawType() {
    Class<?> rawType = getRawType(runtimeType);
    @SuppressWarnings("unchecked") // raw type is |T|
        Class<? super T> result = (Class<? super T>) rawType;
    return result;
  }

  public final Type getType() {
    return runtimeType;
  }

  public final <X> TypeToken<T> where(TypeParameter<X> typeParam, TypeToken<X> typeArg) {
    Map<TypeResolver.TypeVariableKey, Type> mappings = new HashMap<TypeResolver.TypeVariableKey, Type>();
    mappings.put(new TypeResolver.TypeVariableKey(typeParam.typeVariable), typeArg.runtimeType);
    TypeResolver resolver = new TypeResolver()
        .where(mappings);
    // If there's any type error, we'd report now rather than later.
    return new SimpleTypeToken<T>(resolver.resolveType(runtimeType));
  }

  public final TypeToken<?> resolveType(Type type) {
    TypeResolver resolver = typeResolver;
    if (resolver == null) {
      resolver = (typeResolver = TypeResolver.accordingTo(runtimeType));
    }
    return of(resolver.resolveType(type));
  }

  public final boolean isAssignableFrom(TypeToken<?> type) {
    return isAssignableFrom(type.runtimeType);
  }

  public final boolean isAssignableFrom(Type type) {
    return isAssignable(type, runtimeType);
  }

  public final boolean isArray() {
    return getComponentType() != null;
  }

  public final boolean isPrimitive() {
    return (runtimeType instanceof Class) && ((Class<?>) runtimeType).isPrimitive();
  }

  public final TypeToken<T> wrap() {
    if (isPrimitive()) {
      @SuppressWarnings("unchecked") // this is a primitive class
          Class<T> type = (Class<T>) runtimeType;
      return TypeToken.of(Primitives.wrap(type));
    }
    return this;
  }

  @Nullable
  public final TypeToken<?> getComponentType() {
    Type componentType = Types.getComponentType(runtimeType);
    if (componentType == null) {
      return null;
    }
    return of(componentType);
  }

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

  Set<TypeToken<?>> getTypes() {
    Set<TypeToken<?>> tokens = new HashSet<TypeToken<?>>();
    tokens.add(this);
    TypeToken<?> superclass = getGenericSuperclass();
    if (superclass != null) {
      tokens.add(superclass);
      tokens.addAll(superclass.getTypes());
    }
    List<TypeToken<?>> interfaces = getGenericInterfaces();
    for (TypeToken<?> anInterface : interfaces) {
      tokens.add(anInterface);
      tokens.addAll(anInterface.getTypes());
    }
    return tokens;
  }

  public TypeToken<?> resolveFatherClass(Class<?> clazz) {
    Set<TypeToken<?>> fathers = getTypes();
    TypeToken<?> token = null;
    for (TypeToken<?> father : fathers) {
      if (clazz.equals(father.getRawType())) {
        token = father.resolveType(clazz.getTypeParameters()[0]);
        if (Object.class.equals(token.getRawType())) { // 处理范型T
          token = TypeToken.of(Object.class);
        }
      }
    }
    if (token == null) {
      throw new IllegalStateException();
    }
    return token;
  }

  public TokenTuple resolveFatherClassTuple(Class<?> clazz) {
    Set<TypeToken<?>> fathers = getTypes();
    TypeToken firstToken = null;
    TypeToken secondToken = null;
    for (TypeToken<?> father : fathers) {
      if (clazz.equals(father.getRawType())) {
        firstToken = father.resolveType(clazz.getTypeParameters()[0]);
        if (Object.class.equals(firstToken.getRawType())) { // 处理范型T
          firstToken = TypeToken.of(Object.class);
        }
        secondToken = father.resolveType(clazz.getTypeParameters()[1]);
        if (Object.class.equals(secondToken.getRawType())) { // 处理范型T
          secondToken = TypeToken.of(Object.class);
        }
      }
    }
    if (firstToken == null || secondToken == null) {
      throw new IllegalStateException();
    }
    return new TokenTuple(firstToken, secondToken);
  }

  final List<TypeToken<?>> getGenericInterfaces() {
    if (runtimeType instanceof TypeVariable) {
      throw new IllegalStateException();
    }
    if (runtimeType instanceof WildcardType) {
      throw new IllegalStateException();
    }
    List<TypeToken<?>> tokens = new ArrayList<TypeToken<?>>();
    for (Type interfaceType : getRawType().getGenericInterfaces()) {
      TypeToken<?> resolvedInterface = resolveSupertype(interfaceType);
      tokens.add(resolvedInterface);
    }
    return tokens;
  }

  @Nullable
  final TypeToken<?> getGenericSuperclass() {
    if (runtimeType instanceof TypeVariable) {
      throw new IllegalStateException();
    }
    if (runtimeType instanceof WildcardType) {
      throw new IllegalStateException();
    }
    Type superclass = getRawType().getGenericSuperclass();
    if (superclass == null) {
      return null;
    }
    TypeToken<?> superToken = resolveSupertype(superclass);
    return superToken;
  }

  private TypeToken<?> resolveSupertype(Type type) {
    TypeToken<?> supertype = resolveType(type);
    // super types' type mapping is a subset of type mapping of this type.
    supertype.typeResolver = typeResolver;
    return supertype;
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
