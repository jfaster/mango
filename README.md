Mango Project
=============

[![Build Status](https://travis-ci.org/jfaster/mango.svg?branch=master)](https://travis-ci.org/jfaster/mango)
[![Coverage Status](https://coveralls.io/repos/github/jfaster/mango/badge.svg?branch=master)](https://coveralls.io/github/jfaster/mango?branch=master)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Fast, simple, reliable. Mango is a "zero-overhead" production ready data access layer framework.
Now more and more large-scale projects using mango as their core framework.
In our payment system, we use mango to build a distributed payment order system
that can handle 120000+ payment order per second.

Documentation is at http://mango.jfaster.org/

Requires JDK 1.6 or higher.

Latest release
--------------

The most recent release is Mango 1.3.5, released April 12, 2016.

To add a dependency on Mango using Maven, use the following:

```xml
<dependency>
    <groupId>org.jfaster</groupId>
    <artifactId>mango</artifactId>
    <version>1.3.5</version>
</dependency>
```

To add a dependency using Gradle:

```
dependencies {
    compile 'org.jfaster:mango:1.3.5'
}
```

Or [download from here](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.jfaster%22%20AND%20a%3A%22mango%22).

