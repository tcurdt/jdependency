package org.vafer.minijar.maven;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

public final class MiniJarMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private MavenProject getProject() {
        if (project.getExecutionProject() != null) {
            return project.getExecutionProject();
        }

        return project;
    }

	
	private File[] getDependencies() {
        final Set projectArtifacts = getProject().getArtifacts(); 
		return null;
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException {
	}

}
