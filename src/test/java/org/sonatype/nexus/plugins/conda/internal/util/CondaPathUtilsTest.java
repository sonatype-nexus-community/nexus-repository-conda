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
package org.sonatype.nexus.plugins.conda.internal.util;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class CondaPathUtilsTest
    extends TestSupport
{
  private CondaPathUtils underTest;

  @Mock
  TokenMatcher.State state;

  private Map<String, String> tokens;

  @Before
  public void setUp() throws Exception {
    underTest = new CondaPathUtils();
    tokens = setupTokens("3.0.0", "imaginary/path", "123", "numpy", "osx");
  }

  @Test
  public void arch() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.arch(state);

    assertThat(result, is(equalTo("osx")));
  }

  @Test
  public void name() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.name(state);

    assertThat(result, is(equalTo("numpy")));
  }

  @Test
  public void path() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.path(state);

    assertThat(result, is(equalTo("imaginary/path")));
  }

  @Test
  public void version() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.version(state);

    assertThat(result, is(equalTo("3.0.0")));
  }

  @Test
  public void build() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.build(state);

    assertThat(result, is(equalTo("123")));
  }

  @Test
  public void buildAssetPath() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.buildAssetPath(state, CondaPathUtils.INDEX_HTML);

    assertThat(result, is(equalTo("/imaginary/path/index.html")));
  }

  @Test
  public void buildArchAssetPath() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.buildArchAssetPath(state, CondaPathUtils.INDEX_HTML);

    assertThat(result, is(equalTo("/imaginary/path/osx/index.html")));
  }

  @Test
  public void buildCondaPackagePath() throws Exception {
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.buildCondaPackagePath(state);

    assertThat(result, is(equalTo("/imaginary/path/osx/numpy-3.0.0-123.tar.bz2")));
  }

  private Map<String, String> setupTokens(final String version,
                                          final String path,
                                          final String build,
                                          final String name,
                                          final String arch)
  {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("version", version);
    tokens.put("path", path);
    tokens.put("build", build);
    tokens.put("name", name);
    tokens.put("arch", arch);

    return tokens;
  }
}