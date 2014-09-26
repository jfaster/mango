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

package org.jfaster.mango.plugin.spring;

import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ash
 */
public final class ClassPath {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(ClassPath.class);
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";

    private final Set<ClassInfo> classes;

    private ClassPath(Set<ClassInfo> classes) {
        this.classes = classes;
    }

    public static ClassPath from(ClassLoader classloader) throws IOException {
        Scanner scanner = new Scanner();
        for (Map.Entry<URI, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
            scanner.scan(entry.getKey(), entry.getValue());
        }
        return new ClassPath(scanner.getClasses());
    }

    public Set<ClassInfo> getTopLevelClasses() {
        Set<ClassInfo> topLevelClasses = new HashSet<ClassInfo>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.getClassName().indexOf('$') == -1) {
                topLevelClasses.add(classInfo);
            }
        }
        return topLevelClasses;
    }

    public static final class ClassInfo {

        private final String className;
        private final ClassLoader loader;

        ClassInfo(String className, ClassLoader loader) {
            this.className = className;
            this.loader = loader;
        }

        public String getPackageName() {
            int lastDot = className.lastIndexOf('.');
            return (lastDot < 0) ? "" : className.substring(0, lastDot);
        }

        public String getClassName() {
            return className;
        }

        public Class<?> load() {
            try {
                return loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                // 不可能发生
                throw new IllegalStateException(e);
            }
        }

        @Override public String toString() {
            return className;
        }

        @Override public int hashCode() {
            return className.hashCode();
        }

        @Override public boolean equals(Object obj) {
            if (obj instanceof ClassInfo) {
                ClassInfo that = (ClassInfo) obj;
                return className.equals(that.className)
                        && loader == that.loader;
            }
            return false;
        }
    }

    static Map<URI, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<URI, ClassLoader> entries = new LinkedHashMap<URI, ClassLoader>();
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
        }
        if (classloader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classloader;
            for (URL entry : urlClassLoader.getURLs()) {
                URI uri;
                try {
                    uri = entry.toURI();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
                if (!entries.containsKey(uri)) {
                    entries.put(uri, classloader);
                }
            }
        }
        return entries;
    }

    static final class Scanner {

        private final Set<ClassInfo> classes = new HashSet<ClassInfo>();
        private final Set<URI> scannedUris = new HashSet<URI>();

        Set<ClassInfo> getClasses() {
            return classes;
        }

        void scan(URI uri, ClassLoader classloader) throws IOException {
            if (uri.getScheme().equals("file") && scannedUris.add(uri)) {
                scanFrom(new File(uri), classloader);
            }
        }

        void scanFrom(File file, ClassLoader classloader)
                throws IOException {
            if (!file.exists()) {
                return;
            }
            if (file.isDirectory()) {
                scanDirectory(file, classloader);
            } else {
                scanJar(file, classloader);
            }
        }

        private void scanDirectory(File directory, ClassLoader classloader) throws IOException {
            scanDirectory(directory, classloader, "", new HashSet<File>());
        }

        private void scanDirectory(
                File directory, ClassLoader classloader, String packagePrefix,
                Set<File> ancestors) throws IOException {
            File canonical = directory.getCanonicalFile();
            if (ancestors.contains(canonical)) {
                // A cycle in the filesystem, for example due to a symbolic link.
                return;
            }
            File[] files = directory.listFiles();
            if (files == null) {
                logger.warn("Cannot read directory " + directory);
                // io错误，跳过此目录
                return;
            }

            Set<File> newAncestors = new HashSet<File>();
            newAncestors.addAll(ancestors);
            newAncestors.add(canonical);

            for (File f : files) {
                String name = f.getName();
                if (f.isDirectory()) {
                    scanDirectory(f, classloader, packagePrefix + name + ".", newAncestors);
                } else {
                    if (name.endsWith(CLASS_FILE_NAME_EXTENSION)) { // 是class文件
                        int classNameEnd = name.length() - CLASS_FILE_NAME_EXTENSION.length();
                        String className = packagePrefix + name.substring(0, classNameEnd);
                        classes.add(new ClassInfo(className, classloader));
                    }
                }
            }
        }

        private void scanJar(File file, ClassLoader classloader) throws IOException {
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                // 不是jar文件
                return;
            }
            try {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory()) { // 是目录则继续
                        continue;
                    }
                    String resourceName = entry.getName();
                    if (resourceName.endsWith(CLASS_FILE_NAME_EXTENSION)) {
                        int classNameEnd = resourceName.length() - CLASS_FILE_NAME_EXTENSION.length();
                        String className = resourceName.substring(0, classNameEnd);
                        classes.add(new ClassInfo(className, classloader));
                    }
                }
            } finally {
                try {
                    jarFile.close();
                } catch (IOException ignored) {}
            }
        }

    }

}
