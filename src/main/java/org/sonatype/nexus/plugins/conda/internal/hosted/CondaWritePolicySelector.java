package org.sonatype.nexus.plugins.conda.internal.hosted;

import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.repository.storage.WritePolicySelector;

public class CondaWritePolicySelector implements WritePolicySelector {
    /**
     * Allow override of repodata.json
     */
    @Override
    public WritePolicy select(Asset asset, WritePolicy configured) {
        if (WritePolicy.ALLOW_ONCE != configured) {
            return configured;
        }
        String name = asset.name();
        if (name.endsWith(".tar.bz2")) {
            return WritePolicy.ALLOW_ONCE;
        }
        return WritePolicy.ALLOW;
    }
}
