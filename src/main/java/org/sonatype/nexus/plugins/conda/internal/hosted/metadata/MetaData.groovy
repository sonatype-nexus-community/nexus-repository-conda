package org.sonatype.nexus.plugins.conda.internal.hosted.metadata


import groovy.json.JsonSlurper
import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import java.util.stream.Collectors

class PackageDesc {
    //String activate.d
    boolean binary_prefix
    //deactivate.d
    String description
    String dev_url
    String doc_url
    String home
    String license
    boolean post_link
    boolean pre_link
    boolean post_unlink
    Object run_exports
    String source_url
    Set<String> subdirs
    String summary
    boolean text_prefix
    String version
}

class ChannelData {
    int channeldata_version
    Map<String, PackageDesc> packages
    Set<String> subdirs
}

class Info {
    String subdir
}

class PackageIndex {
    String arch
    String build
    int build_number
    List<String> depends
    String license
    String license_family
    String md5
    String name
    String sha256
    String noarch
    String platform
    long size
    String subdir
    long timestamp
    String version
}

class RepoData {
    Info info
    Map<String, PackageIndex> packages
    int repodata_version
    Object packages_conda
    List<String> removed
}

class MetaData {
    static String readIndexJson(InputStream input) throws IOException {
        TarArchiveInputStream tarInputStream = null
        try {
            tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", new BZip2CompressorInputStream(input))
            TarArchiveEntry entry
            while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase("info/index.json")) {
                    break
                }
            }
            return entry != null ? readAsString(tarInputStream) : null
        } finally {
            tarInputStream?.close()
        }
    }

    static String readAsString(InputStream inputStream) throws IOException {
        BufferedReader br = null
        try {
            new BufferedReader(new InputStreamReader(inputStream, "utf-8"))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()))
        } finally {
            br?.close()
        }
    }

    static PackageIndex asIndex(String json) {
        new JsonSlurper().parseText(json) as PackageIndex
    }

    static RepoData asRepoData(String json) {
        def cleanJson = json.replaceAll('packages.conda', 'packages_conda')
        new JsonSlurper().parseText(cleanJson) as RepoData
    }

    static ChannelData asChannelData(String json) {
        new JsonSlurper().parseText(json) as ChannelData
    }

}
