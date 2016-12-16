package org.jfaster.mango.mapper.tuple;

/**
 * @author ash
 */
public class Tuples {

  public static <T1, T2> Tuple2<T1, T2> tuple(
      Class<T1> cls1, Class<T2> cls2, Object val1, Object val2) {
    return new Tuple2<T1, T2>(cls1.cast(val1), cls2.cast(val2));
  }

  public static <T1, T2, T3> Tuple3<T1, T2, T3> tuple(
      Class<T1> cls1, Class<T2> cls2, Class<T3> cls3, Object val1, Object val2, Object val3) {
    return new Tuple3<T1, T2, T3>(cls1.cast(val1), cls2.cast(val2), cls3.cast(val3));
  }

}
