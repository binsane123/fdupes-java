package fdups;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;
import static org.slf4j.LoggerFactory.getLogger;

class FileMetadataContainer {

    private static final Logger LOGGER = getLogger(FileMetadataContainer.class);

    private final MetricRegistry metricRegistry;
    private final Collection<FileMetadata> fileMetadataCollection = newHashSet();

    FileMetadataContainer(final MetricRegistry metricRegistry) {
        super();

        this.metricRegistry = metricRegistry;
    }

    void clear() {
        fileMetadataCollection.clear();
    }

    void addFile(final Path path) {
        try {
            fileMetadataCollection.add(new FileMetadata(path.toString(), Files.size(path)));
        } catch (final Exception e) {
            LOGGER.error("Can't read file [{}] ({})", path, e.getClass().getSimpleName());
        }
    }

    Set<String> extractDuplicates() {
        return fileMetadataCollection.stream()
                                     // index file metadata elements by file size
                                     .collect(MultimapCollector.toMultimap(metricRegistry, "duplicatesBySize", FileMetadata::getSize))
                                     // get a stream of entries
                                     .asMap()
                                     .entrySet()
                                     .stream()
                                     // keep duplicates by size only
                                     .filter(e -> e.getValue().size() > 1)
                                     // flatten all duplicates by size
                                     .flatMap(e -> e.getValue().stream())
                                     .peek(e -> LOGGER.debug("Duplicate by size detected at [{}]", e.getAbsolutePath()))
                                     // index file metadata elements by md5sum
                                     .collect(MultimapCollector.toMultimap(metricRegistry, "duplicatesByMd5Sum", this::md5sum))
                                     // get a stream of entries
                                     .asMap()
                                     .entrySet()
                                     .stream()
                                     // keep duplicates by md5sum only
                                     .filter(e -> e.getValue().size() > 1)
                                     // remove first element of each collection (i.e. original file)
                                     .peek(e -> sortAndRemoveFirst((List<FileMetadata>) e.getValue()))
                                     // flatten all duplicates by md5sum (i.e. all identified duplicates to remove)
                                     .flatMap(e -> e.getValue().stream())
                                     .peek(e -> LOGGER.debug("Duplicate by md5sum detected at [{}]", e.getAbsolutePath()))
                                     // extract absolute paths only
                                     .map(e -> format("\"%s\"", e.getAbsolutePath()))
                                     // collect absolute paths
                                     .collect(Collectors.toCollection(TreeSet::new));

    }

    private String md5sum(final FileMetadata fileMetadata) {
        try (final Timer.Context ignored = metricRegistry.timer(name("md5sum", "timer")).time()) {
            final String md5sum = new ProcessExecutor().command(getNativeMd5SumCommand(fileMetadata))
                                                       .readOutput(true)
                                                       .redirectOutput(Slf4jStream.of("md5sum").asTrace())
                                                       .execute()
                                                       .outputUTF8()
                                                       .split("\\s")[0];
            return md5sum;
        } catch (final Exception e) {
            LOGGER.error("Can't compute md5sum from file [{}] ({})", fileMetadata.getAbsolutePath(), e.getClass().getSimpleName());
            return randomUUID().toString();
        }
    }

    private Iterable<String> getNativeMd5SumCommand(final FileMetadata fileMetadata) {
        final Collection<String> command = newArrayList();

        if (IS_OS_LINUX) {
            command.add("md5sum");
        } else if (IS_OS_MAC_OSX) {
            command.add("md5");
            command.add("-q");
        } else {
            throw new UnsupportedOperationException("Only Linux and OS X operating systems are supported");
        }

        command.add(fileMetadata.getAbsolutePath());

        return command;
    }

    private void sortAndRemoveFirst(final List<FileMetadata> value) {
        Collections.sort(value, (o1, o2) -> o1.getAbsolutePath().compareTo(o2.getAbsolutePath()));
        value.remove(0);
    }

}
