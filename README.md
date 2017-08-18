# jdependency - explore your classpath

jdependency is small library that helps you analyze class level dependencies,
clashes and missing classes.

Check the documentation on how to use it with [javadocs](http://tcurdt.github.com/jdependency/release/1.2/apidocs/) and a source
[xref](http://tcurdt.github.com/jdependency/release/1.2/xref/) is also available.

## Where to get it

The jars are available on [maven central](http://repo1.maven.org/maven2/org/vafer/jdependency/).
The source releases you can get in the [download section](http://github.com/tcurdt/jdependency/downloads).

If feel adventures or want to help out feel free to get the latest code
[via git](http://github.com/tcurdt/jdependency/tree/master).

    git clone git://github.com/tcurdt/jdependency.git

## How to use it

### finding classpath clashes

    final Clazzpath cp = new Clazzpath();
    cp.addClazzpathUnit(jar1, "jar1.jar");
    cp.addClazzpathUnit(jar2, "jar2.jar");

    final Set<Clazz> clashed = cp.getClashedClazzes();
    for(Clazz clazz : clashed) {
      System.out.println("class " + clazz + " is contained in " + clazz.getClasspathUnits());
    }

### finding missing classes

    final Clazzpath cp = new Clazzpath();
    cp.addClazzpathUnit(jar1, "jar1.jar");

    final Set<Clazz> missing = cp.getMissingClazzes();
    for(Clazz clazz : missing) {
      System.out.println("class " + clazz + " is missing");
    }

### finding unused classes

    final Clazzpath cp = new Clazzpath();
    final ClazzpathUnit artifact = cp.addClazzpathUnit(jar1, "artifact.jar");
    cp.addClazzpathUnit(jar2, "dependency.jar");

    final Set<Clazz> removable = cp.getClazzes();
    removable.removeAll(artifact.getClazzes());
    removable.removeAll(artifact.getTransitiveDependencies());

    for(Clazz clazz : removable) {
      System.out.println("class " + clazz + " is not required");
    }

## Related projects

* [maven-shade-plugin](http://maven.apache.org/plugins/maven-shade-plugin/)
* [jarjar](http://code.google.com/p/jarjar/)
* [proguard](http://proguard.sourceforge.net/)

## License

All code and data is released under the Apache License 2.0.