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

package org.jfaster.mango.invoker;

import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author ash
 */
public class BytecodeGen {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BytecodeGen.class);


    static final net.sf.cglib.core.NamingPolicy FASTCLASS_NAMING_POLICY
            = new net.sf.cglib.core.DefaultNamingPolicy() {
        @Override protected String getTag() {
            return "ByGuice";
        }

        @Override
        public String getClassName(String prefix, String source, Object key,
                                   net.sf.cglib.core.Predicate names) {
            // we explicitly set the source here to "FastClass" so that our jarjar renaming
            // to $FastClass doesn't leak into the class names.  if we did not do this,
            // classes would end up looking like $$$FastClassByGuice$$, with the extra $
            // at the front.
            return super.getClassName(prefix, "FastClass", key, names);
        }
    };

    public static net.sf.cglib.reflect.FastClass newFastClass(Class<?> type, Visibility visibility) {
        net.sf.cglib.reflect.FastClass.Generator generator
                = new net.sf.cglib.reflect.FastClass.Generator();
        generator.setType(type);
        if (visibility == Visibility.PUBLIC) {
            generator.setClassLoader(type.getClassLoader());
        }
        generator.setNamingPolicy(FASTCLASS_NAMING_POLICY);
        logger.info("Loading " + type + " FastClass with " + generator.getClassLoader());
        return generator.create();
    }

    public enum Visibility {

        /**
         * Indicates that Guice-generated classes only need to call and override public members of the
         * target class. These generated classes may be loaded by our bridge classloader.
         */
        PUBLIC {
            @Override
            public Visibility and(Visibility that) {
                return that;
            }
        },

        /**
         * Indicates that Guice-generated classes need to call or override package-private members.
         * These generated classes must be loaded in the same classloader as the target class. They
         * won't work with OSGi, and won't get garbage collected until the target class' classloader is
         * garbage collected.
         */
        SAME_PACKAGE {
            @Override
            public Visibility and(Visibility that) {
                return this;
            }
        };

        public static Visibility forMember(Member member) {
            if ((member.getModifiers() & (Modifier.PROTECTED | Modifier.PUBLIC)) == 0) {
                return SAME_PACKAGE;
            }

            Class[] parameterTypes;
            if (member instanceof Constructor) {
                parameterTypes = ((Constructor) member).getParameterTypes();
            } else {
                Method method = (Method) member;
                if (forType(method.getReturnType()) == SAME_PACKAGE) {
                    return SAME_PACKAGE;
                }
                parameterTypes = method.getParameterTypes();
            }

            for (Class<?> type : parameterTypes) {
                if (forType(type) == SAME_PACKAGE) {
                    return SAME_PACKAGE;
                }
            }

            return PUBLIC;
        }

        public static Visibility forType(Class<?> type) {
            return (type.getModifiers() & (Modifier.PROTECTED | Modifier.PUBLIC)) != 0
                    ? PUBLIC
                    : SAME_PACKAGE;
        }

        public abstract Visibility and(Visibility that);
    }

}
