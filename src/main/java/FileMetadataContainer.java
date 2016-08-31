import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Throwables;
import com.google.common.primitives.UnsignedBytes;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.collect.Sets.newHashSet;
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
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    Collection<String> extractDuplicates() {
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
                                     .peek(e -> LOGGER.info("Duplicate by size detected at [{}]", e.getAbsolutePath()))
                                     // index file metadata elements by md5sum
                                     .collect(MultimapCollector.toMultimap(metricRegistry, "duplicatesByMd5Sum", this::md5sum))
                                     // get a stream of entries
                                     .asMap()
                                     .entrySet()
                                     .stream()
                                     // keep duplicates by md5sum only
                                     .filter(e -> e.getValue().size() > 1)
                                     // remove first element of each collection (i.e. original file)
                                     .peek(e -> e.getValue().remove(e.getValue().iterator().next()))
                                     // flatten all duplicates by md5sum (i.e. all identified duplicates to remove)
                                     .flatMap(e -> e.getValue().stream())
                                     .peek(e -> LOGGER.info("Duplicate by md5sum detected at [{}]", e.getAbsolutePath()))
                                     // extract absolute paths only
                                     .map(FileMetadata::getAbsolutePath)
                                     // collect absolute paths
                                     .collect(Collectors.toList());

    }

    private String md5sum(final FileMetadata fileMetadata) {
        try (final Timer.Context ignored = metricRegistry.timer(name("md5sum", "timer")).time()) {
            final String separator = ":";
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final Path path = Paths.get(fileMetadata.getAbsolutePath());
            final byte[] bytes = Files.readAllBytes(path);
            final byte[] digest = md.digest(bytes);

            return UnsignedBytes.join(separator, digest);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
