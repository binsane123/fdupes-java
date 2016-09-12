# fdupes-java

[![Build status](https://travis-ci.org/cbismuth/fdupes-java.svg?branch=master)](https://travis-ci.org/cbismuth/fdupes-java)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/cbismuth/fdupes-java/master/LICENSE.md)
[![GitHub issues](https://img.shields.io/github/issues/cbismuth/fdupes-java.svg)](https://github.com/cbismuth/fdupes-java/issues)

## Description

A command line duplicated files finder written in Java 8 which finds all duplicated files from input paths and their subdirectories.

## Usage

Executable files are available on the [release page](https://github.com/cbismuth/fdupes-java/releases), download the
latest one and run the command line below. 

```
java -jar fdupes-<version>-all.jar <PATH1> [<PATH2>]...
```

## Output

Paths of duplicated files are reported in a `duplicates.log` file dumped in the current working directory.

**Notes**:

* Reported paths are "double quoted" and "escaped" to be *nix-compliant.

`<PATH1> [<PATH2>]...` can be either regular files, directories or both.

## Requirements

Java 8 Runtime environment is the only requirement, it can be downloaded [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

## Motivation

Original [fdupes](https://github.com/adrianlopezroche/fdupes) application has two major caveats [fdupes-java](https://github.com/cbismuth/fdupes-java) works around.

> When  used  together  with  options  -s  or  --symlink,  a  user  could
  accidentally preserve a symlink while deleting the file it points to.

Symlinks are ignored in [fdupes-java](https://github.com/cbismuth/fdupes-java).

> Furthermore, when specifying a particular directory more than once, all
  files  within  that  directory  will be listed as their own duplicates,
  leading to data  loss  should  a  user  preserve  a  file  without  its
  "duplicate" (the file itself!).

Duplicated input directories and files are filtered in [fdupes-java](https://github.com/cbismuth/fdupes-java).

## Algorithms

* Files are compared by **file sizes**, then by **MD5 signatures**, finally a buffered **byte-by-byte** comparison is done.
* Original file is detected by comparing creation, last access and last modification time.

## Issues

Here is how issues are triaged:

* **Bug**: identifies an unexpected result or application behaviour.
* **Feature**: adds an new end-user feature.
* **Enhancement**: improves the way the application behaves but produces the same result.
* **Spike**: improves implementation design but does not change application behaviour and produces the same result.

## Credits

Written by Christophe Bismuth, licensed under the [The MIT License (MIT)](LICENSE.md).
