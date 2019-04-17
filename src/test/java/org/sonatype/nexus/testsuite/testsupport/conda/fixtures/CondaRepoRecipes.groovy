package org.sonatype.nexus.testsuite.testsupport.conda.fixtures

import javax.annotation.Nonnull

import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.testsuite.testsupport.fixtures.ConfigurationRecipes

import groovy.transform.CompileStatic


/**
 * Factory for Conda {@link Repository} {@link Configuration}
 */
@CompileStatic
trait CondaRepoRecipes
    extends ConfigurationRecipes
{
  @Nonnull
  Repository createCondaProxy(final String name,
                            final String remoteUrl)
  {
    createRepository(createProxy(name, 'conda-proxy', remoteUrl))
  }

  abstract Repository createRepository(final Configuration configuration)
}
