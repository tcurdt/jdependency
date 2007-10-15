package org.vafer.jar.merging;

import java.io.IOException;

import org.vafer.jar.Jar;
import org.vafer.jar.handler.DelegatingJarHandler;

public interface MergeStrategy {

	public DelegatingJarHandler getMergeHandler( final Jar[] jars ) throws IOException;
}
