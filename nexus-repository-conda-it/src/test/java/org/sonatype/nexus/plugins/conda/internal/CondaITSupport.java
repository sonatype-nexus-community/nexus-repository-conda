package org.sonatype.nexus.plugins.conda.internal;

import java.net.URL;

import javax.annotation.Nonnull;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.conda.internal.fixtures.RepositoryRuleConda;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;

import org.junit.Rule;

import static com.google.common.base.Preconditions.checkNotNull;

public class CondaITSupport
    extends RepositoryITSupport
{
  @Rule
  public RepositoryRuleConda repos = new RepositoryRuleConda(() -> repositoryManager);

  @Override
  protected RepositoryRuleConda createRepositoryRule() {
    return new RepositoryRuleConda(() -> repositoryManager);
  }

  public CondaITSupport() {
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/it-resources/conda"));
  }

  @Nonnull
  protected CondaClient condaClient(final Repository repository) throws Exception {
    checkNotNull(repository);
    return condaClient(repositoryBaseUrl(repository));
  }

  protected CondaClient condaClient(final URL repositoryUrl) throws Exception {
    return new CondaClient(
        clientBuilder(repositoryUrl).build(),
        clientContext(),
        repositoryUrl.toURI()
    );
  }
}
