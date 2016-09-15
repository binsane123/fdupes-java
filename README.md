# fdupes-java

[![build](https://travis-ci.org/cbismuth/fdupes-java.svg?branch=master)](https://travis-ci.org/cbismuth/fdupes-java)
[![coverage](https://coveralls.io/repos/github/cbismuth/fdupes-java/badge.svg?branch=master)](https://coveralls.io/github/cbismuth/fdupes-java?branch=master)
[![javadoc](http://javadoc.io/badge/com.github.cbismuth/fdupes-java.svg)](http://javadoc.io/doc/com.github.cbismuth/fdupes-java)
[![repository](https://maven-badges.herokuapp.com/maven-central/com.github.cbismuth/fdupes-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.cbismuth/fdupes-java/)
[![issues](https://img.shields.io/github/issues/cbismuth/fdupes-java.svg)](https://github.com/cbismuth/fdupes-java/issues)
[![licence](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/cbismuth/fdupes-java/master/LICENSE.md)

## Description

A command line duplicated files finder written in Java 8 which finds all duplicated files from input paths and their subdirectories.

## Usage

Executable files are available on the [release page](https://github.com/cbismuth/fdupes-java/releases), download the
latest one and run the command line below. 

```
java -jar fdupes-<version>-all.jar <PATH1> [<PATH2>]...
```

### Options

Here are optional command line switches:

```
-Dlogging.level.fdupes=<LEVEL>    the logging level of fdupes-java, may be one of [ALL,TRACE,DEBUG,INFO,WARN,ERROR,OFF] (default is INFO)
-Dlogging.level.root=<LEVEL>      the logging level of embedded libraries, may be one of [ALL,TRACE,DEBUG,INFO,WARN,ERROR,OFF] (default is WARN)

-Xmx<SIZE><UNIT>                  the max amount of memory to allocate to the Java Runtime Environment (e.g. 512m)

-Dfdupes.parallelism=<NUMBER>     the numbers of threads to parallelize execution  (default is 1)
-Dfdupes.buffer.size=<SIZE><UNIT> the buffer size used for byte-by-byte comparison (default is 64k)
```

### Examples

Find duplicated files in a single directory and its subdirectories with default options:

```bash
java -jar fdupes-1.2.0-all.jar ~/pictures
```

Find duplicated files in a two directories with custom options:

```bash
java -Xmx1g                       \
     -Dfdupes.parallelism=8       \
     -Dfdupes.buffer.size=3m      \
     -Dlogging.level.fdupes=DEBUG \
     -Dlogging.level.root=DEBUG   \
     -jar fdupes-1.2.0-all.jar    \
     ~/pictures                   \
     ~/downloads                  \
     ~/desktop/DSC00042.JPG
```

**Note**: `<PATH1> [<PATH2>]...` can be either regular files, directories or both.

## Output

Paths of duplicated files are reported in a `duplicates.log` file dumped in the current working directory.

**Note**: reported paths are **double-quoted** and **whitespace-escaped** to be *nix-compliant.

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
