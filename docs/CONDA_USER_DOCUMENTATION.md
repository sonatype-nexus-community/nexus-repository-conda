<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2019-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
## Conda Repositories

### Introduction

[Conda](https://conda.io/en/latest/) provides Package, dependency and environment management for any language—Python, R, 
Ruby, Lua, Scala, Java, JavaScript, C/ C++, FORTRAN.

### Installing Conda

Full documentation on installing `conda` can be found on [the Conda project website](https://conda.io/projects/conda/en/latest/user-guide/install/index.html?highlight=conda).

### Proxying The Conda Continuum repository

You can create a proxy repository in Nexus Repository Manager that will cache packages from a remote anaconda repository, like
[Continuum](https://repo.continuum.io/pkgs). Then, you can make the `conda` client use your Nexus Repository Proxy 
instead of the remote repository.
 
To proxy a Conda repository, you simply create a new 'conda (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name' - e.g. `conda-proxy`
- Define URL for 'Remote storage' e.g. [https://repo.continuum.io/pkgs/](https://repo.continuum.io/pkgs/)

Using the `conda` client, you can now download packages from your Nexus Conda proxy like so:

    $ conda install -c http://localhost:8081/repository/conda-proxy/main numpy
    
The command above tells conda to fetch (and install) packages from your Nexus Conda proxy. The Nexus Conda proxy will 
download any missing packages from the remote Conda repository, and cache the packages on the Nexus Conda proxy.
The next time any client requests the same package from your Nexus Conda proxy, the already cached package will
be returned to the client.
