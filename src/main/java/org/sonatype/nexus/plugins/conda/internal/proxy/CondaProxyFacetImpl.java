/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2019-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.conda.internal.proxy;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.conda.internal.AssetKind;
import org.sonatype.nexus.plugins.conda.internal.util.CondaDataAccess;
import org.sonatype.nexus.plugins.conda.internal.util.CondaPathUtils;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.transaction.UnitOfWork;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.conda.internal.util.CondaDataAccess.toContent;
import static org.sonatype.nexus.plugins.conda.internal.util.CondaPathUtils.*;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

/**
 * Conda {@link ProxyFacet} implementation.
 *
 * @since 0.0.1
 */
@Named
public class CondaProxyFacetImpl
    extends ProxyFacetSupport
{
  private CondaPathUtils condaPathUtils;

  private CondaDataAccess condaDataAccess;

  @Inject
  public CondaProxyFacetImpl(final CondaPathUtils condaPathUtils,
                             final CondaDataAccess condaDataAccess)
  {
    this.condaPathUtils = checkNotNull(condaPathUtils);
    this.condaDataAccess = checkNotNull(condaDataAccess);
  }

  // HACK: Workaround for known CGLIB issue, forces an Import-PackageIndex for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = condaPathUtils.matcherState(context);
    switch (assetKind) {
      case CHANNEL_INDEX_HTML:
        return getAsset(condaPathUtils.buildAssetPath(matcherState, INDEX_HTML));
      case CHANNEL_DATA_JSON:
        return getAsset(condaPathUtils.buildAssetPath(matcherState, CHANNELDATA_JSON));
      case CHANNEL_RSS_XML:
        return getAsset(condaPathUtils.buildAssetPath(matcherState, RSS_XML));
      case ARCH_INDEX_HTML:
        return getAsset(condaPathUtils.buildArchAssetPath(matcherState, INDEX_HTML));
      case ARCH_REPODATA_JSON:
        return getAsset(condaPathUtils.buildArchAssetPath(matcherState, REPODATA_JSON));
      case ARCH_REPODATA_JSON_BZ2:
        return getAsset(condaPathUtils.buildArchAssetPath(matcherState, REPODATA_JSON_BZ2));
      case ARCH_REPODATA2_JSON:
        return getAsset(condaPathUtils.buildArchAssetPath(matcherState, REPODATA2_JSON));
      case ARCH_CONDA_PACKAGE:
        return getAsset(condaPathUtils.buildCondaPackagePath(matcherState));
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String assetPath) {
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = condaDataAccess.findAsset(tx, tx.findBucket(getRepository()), assetPath);
    if (asset == null) {
      return null;
    }
    return toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = condaPathUtils.matcherState(context);
    switch (assetKind) {
      case CHANNEL_INDEX_HTML:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildAssetPath(matcherState, INDEX_HTML));
      case CHANNEL_DATA_JSON:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildAssetPath(matcherState, CHANNELDATA_JSON));
      case CHANNEL_RSS_XML:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildAssetPath(matcherState, RSS_XML));
      case ARCH_INDEX_HTML:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildArchAssetPath(matcherState, INDEX_HTML));
      case ARCH_REPODATA_JSON:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildArchAssetPath(matcherState, REPODATA_JSON));
      case ARCH_REPODATA_JSON_BZ2:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildArchAssetPath(matcherState, REPODATA_JSON_BZ2));
      case ARCH_REPODATA2_JSON:
        return putMetadata(content,
            assetKind,
            condaPathUtils.buildArchAssetPath(matcherState, REPODATA2_JSON));
      case ARCH_CONDA_PACKAGE:
        return putCondaPackage(content,
            assetKind,
            condaPathUtils.buildCondaPackagePath(matcherState), condaPathUtils.arch(matcherState),
            condaPathUtils.name(matcherState), condaPathUtils.version(matcherState));
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  private Content putCondaPackage(final Content content,
                                  final AssetKind assetKind,
                                  final String assetPath, final String arch, final String name, final String version)
      throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);

    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), CondaDataAccess.HASH_ALGORITHMS)) {
      Component component = findOrCreateComponent(arch, name, version);

      return findOrCreateAsset(tempBlob, content, assetKind, assetPath, component);
    }
  }

  @TransactionalStoreBlob
  protected Component findOrCreateComponent(final String arch, final String name, final String version) {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    // TODO: Not sure if group is right for arch, but seems ok at first pass
    Component component = condaDataAccess.findComponent(tx,
        getRepository(),
        arch,
        name,
        version);

    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .group(arch)
          .name(name)
          .version(version);
    }
    tx.saveComponent(component);

    return component;
  }

  private Content putMetadata(final Content content,
                              final AssetKind assetKind,
                              final String assetPath) throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);

    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), CondaDataAccess.HASH_ALGORITHMS)) {
      return findOrCreateAsset(tempBlob, content, assetKind, assetPath, null);
    }
  }

  @TransactionalStoreBlob
  protected Content findOrCreateAsset(final TempBlob tempBlob,
                                      final Content content,
                                      final AssetKind assetKind,
                                      final String assetPath,
                                      final Component component) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = condaDataAccess.findAsset(tx, bucket, assetPath);

    if (assetKind.equals(AssetKind.ARCH_CONDA_PACKAGE)) {
      if (asset == null) {
        asset = tx.createAsset(bucket, component);
        asset.name(assetPath);
        asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());
      }
    } else {
      if (asset == null) {
        asset = tx.createAsset(bucket, getRepository().getFormat());
        asset.name(assetPath);
        asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());
      }
    }

    return condaDataAccess.saveAsset(tx, asset, tempBlob, content);
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent Conda asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }
}
