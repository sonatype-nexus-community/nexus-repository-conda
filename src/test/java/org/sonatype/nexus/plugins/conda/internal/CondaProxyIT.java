package org.sonatype.nexus.plugins.conda.internal;

import javax.cache.CacheManager;
import javax.inject.Inject;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.testsuite.testsupport.conda.CondaITSupport;
import org.sonatype.nexus.testsuite.testsupport.raw.RawClient;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class CondaProxyIT
    extends CondaITSupport
{
  public static final String TEST_PATH = "alphabet.txt";

  private RawClient proxyClient;

  private Repository proxyRepo;



  @Inject
  private CacheManager cacheManager;

  @Before
  public void setUpRepositories() throws Exception {
    proxyRepo = repos.createCondaProxy("conda-test-proxy", "someCondaRemoteURL");
    proxyClient = rawClient(proxyRepo);
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    assertThat(status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
  }

}
