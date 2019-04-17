package org.sonatype.nexus.testsuite.testsupport.conda;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.sonatype.nexus.common.log.LogManager;
import org.sonatype.nexus.plugins.conda.internal.proxy.CondaProxyFacet;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;
import org.sonatype.nexus.testsuite.testsupport.conda.fixtures.RepositoryRuleConda;
import org.sonatype.nexus.testsuite.testsupport.raw.RawClient;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.io.Files;
import org.apache.http.entity.ContentType;
import org.junit.Rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.bytes;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class CondaITSupport
    extends RepositoryITSupport
{
  @Inject
  protected LogManager logManager;

  @Rule
  public RepositoryRuleConda repos = new RepositoryRuleConda(() -> repositoryManager);

  public CondaITSupport() {
    testData.addDirectory(resolveBaseFile("target/it-resources/conda"));
  }

  protected Content read(final Repository repository, final String path) throws IOException {
    CondaProxyFacet condaProxyFacet = repository.facet(CondaProxyFacet.class);
    UnitOfWork.begin(repository.facet(StorageFacet.class).txSupplier());
    try {
      //return condaProxyFacet.get(path);
      return condaProxyFacet.getAsset(path);
    }
    finally {
      UnitOfWork.end();
    }
  }

  protected void assertReadable(final Repository repository, final String... paths) throws IOException {
    for (String path : paths) {
      assertThat(path, read(repository, path), notNullValue());
    }
  }

  protected void assertNotReadable(final Repository repository, final String... paths) throws IOException {
    for (String path : paths) {
      assertThat(path, read(repository, path), nullValue());
    }
  }

  protected void uploadAndDownload(RawClient rawClient, String file) throws Exception {
    final File testFile = resolveTestFile(file);
    final int response = rawClient.put(file, ContentType.TEXT_PLAIN, testFile);
    assertThat(response, is(HttpStatus.CREATED));

    assertThat(bytes(rawClient.get(file)), is(Files.toByteArray(testFile)));

    assertThat(status(rawClient.delete(file)), is(HttpStatus.NO_CONTENT));

    assertThat("content should be deleted", status(rawClient.get(file)), is(HttpStatus.NOT_FOUND));
  }

}
