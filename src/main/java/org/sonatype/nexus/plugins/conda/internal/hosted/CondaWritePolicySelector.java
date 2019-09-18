package org.sonatype.nexus.plugins.conda.internal.hosted;
/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
