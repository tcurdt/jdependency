package org.vafer.jdependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.core.AnyOf;
import org.junit.Assert;
import org.junit.Test;
import org.vafer.jdependency.VariousClassSignatureTestCase.Generics.GenericsNested;
import org.vafer.jdependency.VariousClassSignatureTestCase.GenericsExtendedAndBound.GenericNestedExtended;
import org.vafer.jdependency.VariousClassSignatureTestCase.Nest1.Nest2;
import org.vafer.jdependency.utils.DependencyUtils;

public class VariousClassSignatureTestCase {

    static class Nest1 {
        static class Nest2 {
        }
    }

    private static String nm(String class_getname) {
        return class_getname;
    }

    private static <T extends Comparable<T>> String tostr(T... o) {
        return tostr(Arrays.asList(o));
    }

    private static <T extends Comparable<T>> String tostr(Collection<T> o) {
        List<T> x = new ArrayList<T>(o);
        Collections.sort(x);
        return x.toString();
    }

    @Test
    public void testInnerClasses() throws IOException {
        Assert.assertEquals(
                tostr(nm(Nest1.class.getName()), nm(Nest2.class.getName()), nm(Object.class.getName()),
                        nm(VariousClassSignatureTestCase.class.getName())),
                tostr(DependencyUtils.getDependenciesOfClass(Nest2.class)));
    }

    static class Generics<T> {
        class GenericsNested {
        }
    }

    static class GenericsExtendedAndBound extends Generics<Object> {
        class GenericNestedExtended extends GenericsNested {
        }
    }

    @Test
    public void testGenericsInnerClass() throws IOException {
        Assert.assertEquals(
                tostr(nm(GenericNestedExtended.class.getName()), nm(GenericsExtendedAndBound.class.getName()),
                        nm(GenericsNested.class.getName()), nm(Generics.class.getName()), nm(Object.class.getName()),
                        nm(VariousClassSignatureTestCase.class.getName())),
                tostr(DependencyUtils.getDependenciesOfClass(GenericNestedExtended.class)));
    }

}
