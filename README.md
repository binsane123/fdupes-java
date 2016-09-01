# fdups

A simple file duplicate files finder implemented in Java 8 the functional way.
 
* Find all non-hidden files from input paths and their subdirectories
* Files are first compared by file size in bytes
* Duplicates by file size are compared by native MD5
* Double quoted absolute paths of MD5-based duplicates are reported in a `duplicates.log` file dumped in current working directory
* To delete this duplicates just use this *nix one-liner `duplicates.log | xargs echo rm | sh`
* An executable uber JAR is produced by the Maven project descriptor to ease usage (just run `mvn clean install`)
* Dropwizard-based metrics are logged along the way

```
java -jar fdups-<version>-all.jar <dir1> <dir2>
```

Written by Christophe Bismuth, licensed under the [The MIT License (MIT)](LICENSE.md).
