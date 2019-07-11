package org.sonatype.nexus.plugins.conda.internal;

import javax.cache.CacheManager;
import javax.inject.Inject;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.testsuite.testsupport.FormatClientSupport;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;
import org.sonatype.nexus.testsuite.testsupport.raw.RawClient;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.Matchers.is;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

public class CondaProxyIT
    extends CondaITSupport
{
  public static final String TEST_PATH = "alphabet.txt";

  private RawClient proxyClient;

  private Repository proxyRepo;

  @Inject
  private CacheManager cacheManager;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-conda")
    );
  }

  @Before
  public void setUpRepositories() throws Exception {
    proxyRepo = repos.createCondaProxy("conda-test-proxy", "http://someCondaRemoteURL");
    proxyClient = rawClient(proxyRepo);
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    MatcherAssert.assertThat(FormatClientSupport.status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
  }

}
