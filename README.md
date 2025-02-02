[![Build Status](https://img.shields.io/github/actions/workflow/status/tcurdt/jdependency/ci.yml?style=for-the-badge)](https://github.com/tcurdt/jdependency/actions)
[![Coverage Status](https://img.shields.io/codecov/c/github/tcurdt/jdependency/master?style=for-the-badge)](https://codecov.io/gh/tcurdt/jdependency)
[![Maven Central](https://img.shields.io/maven-central/v/org.vafer/jdependency.svg?style=for-the-badge&maxAge=86400)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.vafer%22%20AND%20a%3A%22jdependency%22)
[![Join the chat](https://img.shields.io/gitter/room/tcurdt/jdependency?style=for-the-badge)](https://gitter.im/tcurdt/jdependency?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


# jdependency - explore your classpath

jdependency is small library that helps you analyze class level dependencies,
clashes and missing classes.

Check the documentation on how to use it with [javadocs](https://tcurdt.github.io/jdependency/apidocs/) and a source
[xref](http://tcurdt.github.io/jdependency/xref/) is also available.

## Where to get it

The jars are available on [maven central](https://repo1.maven.org/maven2/org/vafer/jdependency/).
The source releases you can get in the [download section](https://github.com/tcurdt/jdependency/downloads).

If feel adventures or want to help out feel free to get the latest code
[via git](https://github.com/tcurdt/jdependency/tree/master).

```sh
    git clone git://github.com/tcurdt/jdependency.git
```

## How to use it

```java
    final File jar1 = ...
    final File jar2 = ...
```

or

```java
    final Path jar1 = ...
    final Path jar2 = ...
```

### finding classpath clashes

```java
    final Clazzpath cp = new Clazzpath();
    cp.addClazzpathUnit(jar1, "jar1.jar");
    cp.addClazzpathUnit(jar2, "jar2.jar");

    final Set<Clazz> clashed = cp.getClashedClazzes();
    for(Clazz clazz : clashed) {
      System.out.println("class " + clazz + " is contained in " + clazz.getClasspathUnits());
    }
```

### finding different class versions

```java
    final Clazzpath cp = new Clazzpath(true);
    cp.addClazzpathUnit(jar1, "jar1.jar");
    cp.addClazzpathUnit(jar2, "jar2.jar");

    final Set<Clazz> clashed = cp.getClashedClazzes();

    final Set<Clazz> uniq = clashed.stream()
      .filter(c -> c.getVersions().size() == 1)
      .collect(Collectors.toSet());

    clashed.removeAll(uniq);

    for(Clazz clazz : clashed) {
      System.out.println("class " + clazz + " differs accross " + clazz.getClasspathUnits());
    }
```

### finding missing classes

```java
    final Clazzpath cp = new Clazzpath();
    cp.addClazzpathUnit(jar1, "jar1.jar");

    final Set<Clazz> missing = cp.getMissingClazzes();
    for(Clazz clazz : missing) {
      System.out.println("class " + clazz + " is missing");
    }
```

### finding unused classes

```java
    final Clazzpath cp = new Clazzpath();
    final ClazzpathUnit artifact = cp.addClazzpathUnit(jar1, "artifact.jar");
    cp.addClazzpathUnit(jar2, "dependency.jar");

    final Set<Clazz> removable = cp.getClazzes();
    removable.removeAll(artifact.getClazzes());
    removable.removeAll(artifact.getTransitiveDependencies());

    for(Clazz clazz : removable) {
      System.out.println("class " + clazz + " is not required");
    }
```

## Related projects


provides a report of the dependencies used/unused and provides a debloated version of the pom.xml


| Project | Description |
|---|---|
| [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) | allows to inline and optimize dependencies into a single jar |
| [gradle-lean](https://github.com/cuzfrog/gradle-lean) | gradle version of the maven-shade plugin (stale) |
| [shadow](https://github.com/GradleUp/shadow) | gradle version of the maven-shade plugin |
| [jarjar](http://code.google.com/p/jarjar/) | allows to inline and optimize dependencies into a single jar (stale) |
| [proguard](https://github.com/Guardsquare/proguard) | obfuscator, shrinker (GPL) |
| [DepClean](https://github.com/castor-software/depclean) |  provides a report of the dependencies used/unused and provides a debloated version of the pom.xml|


## License

All code and data is released under the Apache License 2.0.
