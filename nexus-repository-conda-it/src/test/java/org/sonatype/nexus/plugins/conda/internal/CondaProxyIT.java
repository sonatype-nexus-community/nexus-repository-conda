package org.sonatype.nexus.plugins.conda.internal;

import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.testsuite.testsupport.FormatClientSupport;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.goodies.httpfixture.server.fluent.Behaviours.content;
import static org.sonatype.goodies.httpfixture.server.fluent.Behaviours.error;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class CondaProxyIT
    extends CondaITSupport
{
  private static final String TEST_PATH = "imaginary/path/index.html";
  //private static final String TEST_PATH = "imaginary/path/osx/numpy-3.0.0-123.tar.bz2";

  private CondaClient proxyClient;

  private Repository proxyRepo;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-conda")
    );
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    Server server = Server.withPort(0).serve("/*")
        .withBehaviours(error(HttpStatus.NOT_FOUND))
        .start();
    try {
      proxyRepo = repos.createCondaProxy("conda-test-proxy-notfound", server.getUrl().toExternalForm());
      proxyClient = condaClient(proxyRepo);
      MatcherAssert.assertThat(FormatClientSupport.status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
    }
    finally {
      server.stop();
    }
  }

  @Test
  public void retrieveCondaWhenRemoteOffline() throws Exception {
    Server server = Server.withPort(0).serve("/*")
        .withBehaviours(content("Response"))
        .start();
    try {
      proxyRepo = repos.createCondaProxy("conda-test-proxy-offline", server.getUrl().toExternalForm());
      proxyClient = condaClient(proxyRepo);
      proxyClient.get(TEST_PATH);
    }
    finally {
      server.stop();
    }
    assertThat(status(proxyClient.get(TEST_PATH)), is(200));
  }
}
