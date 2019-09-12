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
package org.sonatype.nexus.plugins.conda.internal.hosted

import org.sonatype.nexus.plugins.conda.internal.CondaFormat
import org.sonatype.nexus.plugins.conda.internal.CondaRecipeSupport
import org.sonatype.nexus.plugins.conda.internal.util.CondaPathUtils
import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.*
import org.sonatype.nexus.repository.view.handlers.BrowseUnsupportedHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import static org.sonatype.nexus.repository.http.HttpMethods.*
import static org.sonatype.nexus.repository.http.HttpResponses.methodNotAllowed
import static org.sonatype.nexus.repository.http.HttpResponses.notFound
import static org.sonatype.nexus.repository.http.HttpResponses.ok
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.and

/**
 * Conda Hosted Recipe
 */
@Named(CondaHostedRecipe.NAME)
@Singleton
class CondaHostedRecipe
        extends CondaRecipeSupport {
    public static final String NAME = 'conda-hosted'

    @Inject
    Provider<CondaHostedFacetImpl> hostedFacet

    @Inject
    CondaHostedRecipe(@Named(HostedType.NAME) final Type type, @Named(CondaFormat.NAME) final Format format) {
        super(type, format)
    }

    @Override
    void apply(@Nonnull final Repository repository) throws Exception {
        repository.attach(securityFacet.get())
        repository.attach(configure(viewFacet.get()))
        repository.attach(httpClientFacet.get())
        repository.attach(componentMaintenanceFacet.get())
        repository.attach(storageFacet.get())
        repository.attach(hostedFacet.get())
        repository.attach(searchFacet.get())
        repository.attach(attributesFacet.get())
    }


    /**
     * Match conda metadata files
     */
    static Matcher condaMetaDataMather = {
        Context context ->
            def path = context.getRequest().getPath()
            log.warn("Searching condaMetaDataMather " + path)
            String candidate = path.substring(path.lastIndexOf('/'))
            CondaPathUtils.CONDA_META_DATA.contains(candidate)
    }

    /**
     * Conda path matcher - verifies path has conda alike structure
     */
    static Matcher condaPathMatcher = {
        Context context ->
            // TODO: fix logging, use try monad
            try {
                final String path = context.getRequest().getPath()
                log.warn("Searching condaPathMatcher " + path)
                CondaPath.build(path)
            }
            catch (Exception ex) {
                false
            }
            true
    }

    static Matcher fetchMetaDataMatcher = and(new ActionMatcher(GET, HEAD), condaMetaDataMather)
    static Matcher fetchCondaFileMatcher = and(new ActionMatcher(GET, HEAD), condaPathMatcher)
    static Matcher uploadCondaFileMatcher = and(new ActionMatcher(PUT), condaPathMatcher)
    static Matcher deleteCondaFileMatcher = and(new ActionMatcher(DELETE), condaPathMatcher)

    static String removeLeadingSlash(String path) {
        path.length() > 1 && path[0] == '/' ? path.substring(1) : path
    }

    final Handler handler = new Handler() {
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

    /**
     * Configure {@link org.sonatype.nexus.repository.view.ViewFacet}.
     */
    private ViewFacet configure(final ConfigurableViewFacet facet) {

        Router.Builder builder = new Router.Builder()
        [fetchCondaFileMatcher, fetchMetaDataMatcher, uploadCondaFileMatcher, deleteCondaFileMatcher].each { matcher ->
            builder.route(new Route.Builder().matcher(matcher)
                    .handler(timingHandler)
                    .handler(securityHandler)
                    .handler(exceptionHandler)
                    .handler(handlerContributor)
                    .handler(partialFetchHandler)
                    .handler(contentHeadersHandler)
                    .handler(unitOfWorkHandler)
                    .handler(handler)
                    .create())
        }

        builder.route(new Route.Builder()
                .matcher(BrowseUnsupportedHandler.MATCHER)
                .handler(browseUnsupportedHandler)
                .create())

        builder.defaultHandlers(HttpHandlers.notFound())
        facet.configure(builder.create())
        return facet
    }

}
