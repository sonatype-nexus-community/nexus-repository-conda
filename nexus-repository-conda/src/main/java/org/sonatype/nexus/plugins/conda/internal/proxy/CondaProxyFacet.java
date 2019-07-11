package org.sonatype.nexus.plugins.conda.internal.proxy;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.view.Content;

@Facet.Exposed
public interface CondaProxyFacet
    extends ProxyFacet
{
  Content getAsset(final String assetPath);
}
