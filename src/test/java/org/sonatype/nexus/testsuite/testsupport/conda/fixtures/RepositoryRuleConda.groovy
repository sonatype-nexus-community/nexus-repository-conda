package org.sonatype.nexus.testsuite.testsupport.conda.fixtures

import javax.inject.Provider

import org.sonatype.nexus.repository.manager.RepositoryManager
import org.sonatype.nexus.testsuite.testsupport.fixtures.RepositoryRule

class RepositoryRuleConda
  extends RepositoryRule
  implements CondaRepoRecipes
{
  RepositoryRuleConda(final Provider<RepositoryManager> repositoryManagerProvider) {
    super(repositoryManagerProvider)
  }
}
