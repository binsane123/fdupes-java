# fdupes-java

[![Build Status](https://travis-ci.org/cbismuth/fdupes-java.svg?branch=master)](https://travis-ci.org/cbismuth/fdupes-java)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/cbismuth/fdupes-java/master/LICENSE.md)
[![GitHub issues](https://img.shields.io/github/issues/cbismuth/fdupes-java.svg)](https://github.com/cbismuth/fdupes-java/issues)

## Description

A duplicated files finder implemented in Java 8, the functional way.

* Find all duplicated files from input paths and their subdirectories
  * Such files are found by comparing **file sizes**, followed by **MD5 signatures** comparison then followed by a **byte-by-byte comparison** 
  * Original file is detected by comparing creation, last access and last modification time
* Double quoted absolute paths of duplicated files are reported in a `duplicates.log` file dumped in the current working directory
  * Contained paths are escaped to be *nix-compliant
  * On *nix systems these duplicates can be deleted by running this one-liner `cat duplicates.log | xargs echo rm | sh`

## Motivation

Original [fdupes](https://github.com/adrianlopezroche/fdupes) application has two major caveats I wanted to work around:
 
> When  used  together  with  options  -s  or  --symlink,  a  user  could
  accidentally preserve a symlink while deleting the file it points to.
 
> Furthermore, when specifying a particular directory more than once, all
  files  within  that  directory  will be listed as their own duplicates,
  leading to data  loss  should  a  user  preserve  a  file  without  its
  "duplicate" (the file itself!).

## Usage

Executable files are available on the [release page](https://github.com/cbismuth/fdupes-java/releases), download the
latest one and run the command line below. 

```
java -jar fdupes-<version>-all.jar <dir1> [<dir2>]...
```

Metrics are logged every minute, so you always know what happens.

## Developer corner

Core implementation is located [here](https://github.com/cbismuth/fdupes-java/blob/1.1.0/src/main/java/fdupes-java/FileMetadataContainer.java#L51).

## Credits

Written by Christophe Bismuth, licensed under the [The MIT License (MIT)](LICENSE.md).
