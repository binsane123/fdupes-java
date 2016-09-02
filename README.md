# fdups

[![Build Status](https://travis-ci.org/cbismuth/fdups.svg?branch=master)](https://travis-ci.org/cbismuth/fdups)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg?style=plastic)](https://raw.githubusercontent.com/cbismuth/fdups/master/LICENSE.md)
[![GitHub issues](https://img.shields.io/github/issues/cbismuth/fdups.svg?style=plastic)](https://github.com/cbismuth/fdups/issues)

A simple duplicated files finder implemented in Java 8, the functional way.

* Find all non-hidden files from input paths and their subdirectories
* Files are first compared by file size in bytes
* Duplicates by file size in bytes are compared by native MD5
* Double quoted absolute paths of MD5-based duplicated files are reported in a `duplicates.log` file dumped in the current working directory
* To delete these duplicates just use this *nix one-liner `duplicates.log | xargs echo rm | sh`
* An executable *uber* JAR is produced by the Maven project descriptor to ease usage (just run `mvn clean install`)
* Dropwizard-based metrics are logged every minute

```
java -jar fdups-<version>-all.jar <dir1> [<dir2>]...
```

Core implementation is located [here](https://github.com/cbismuth/fdups/blob/1.1.0/src/main/java/fdups/FileMetadataContainer.java#L51).

Written by Christophe Bismuth, licensed under the [The MIT License (MIT)](LICENSE.md).
