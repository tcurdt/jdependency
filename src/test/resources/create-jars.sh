#!/bin/sh

cd jar1
javac org/vafer/Jar1.java
jar cvfm ../jar1.jar manifest.mf org/vafer/some.properties org/vafer/Jar1.class org/vafer/jar1.properties
cd ..

cd jar2
javac org/vafer/Jar2.java
jar cvfm ../jar2.jar manifest.mf org/vafer/some.properties org/vafer/Jar2.class org/vafer/jar2.properties
cd ..
