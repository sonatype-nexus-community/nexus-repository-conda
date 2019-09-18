package org.sonatype.nexus.plugins.conda.internal.hosted
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
import org.sonatype.nexus.repository.view.*

import javax.annotation.Nonnull

import static org.sonatype.nexus.repository.http.HttpMethods.*
import static org.sonatype.nexus.repository.http.HttpResponses.*

class HandlerProvider {
    static String removeLeadingSlash(String path) {
        path.length() > 1 && path[0] == '/' ? path.substring(1) : path
    }

    static Handler handler = new Handler() {
        @Override
        Response handle(@Nonnull Context context) throws Exception {
            String method = context.getRequest().getAction()
            String path = removeLeadingSlash(context.getRequest().getPath())

            CondaHostedFacet condaHostedFacet = context
                    .getRepository()
                    .facet(CondaHostedFacet.class)

            switch (method) {
                case GET:
                case HEAD:
                    return condaHostedFacet
                            .fetch(path)
                            .map({ Content content -> ok(content) })
                            .orElseGet({ notFound() })
                case PUT:
                    Payload payload = context.getRequest().getPayload()
                    return ok(condaHostedFacet.upload(path, payload))
                case DELETE:
                    def success = condaHostedFacet.delete(path)
                    return success ? ok() : notFound()
                default:
                    return methodNotAllowed(context.getRequest().getAction(), GET, HEAD, PUT, DELETE)
            }
        }
    }
}
