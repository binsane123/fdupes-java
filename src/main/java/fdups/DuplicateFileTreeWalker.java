package fdups;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;
import static java.nio.file.Files.isDirectory;
import static org.slf4j.LoggerFactory.getLogger;

public class DuplicateFileTreeWalker {

    private static final Logger LOGGER = getLogger(DuplicateFileTreeWalker.class);

    private final MetricRegistry metricRegistry;
    private final FileMetadataContainer fileMetadataContainer;

    public DuplicateFileTreeWalker(final MetricRegistry metricRegistry) {
        this(metricRegistry, new FileMetadataContainer(metricRegistry));
    }

    DuplicateFileTreeWalker(final MetricRegistry metricRegistry, final FileMetadataContainer fileMetadataContainer) {
        this.metricRegistry = metricRegistry;
        this.fileMetadataContainer = fileMetadataContainer;
    }

    public Set<String> extractDuplicates(final Iterable<String> rootPaths) {
        fileMetadataContainer.clear();

        rootPaths.forEach(rootPath -> {
            final Path path = Paths.get(rootPath);

            if (isDirectory(path)) {
                handleDirectory(path);
            } else {
                LOGGER.warn("[{}] is not a directory", rootPath);
            }
        });

        return fileMetadataContainer.extractDuplicates();
    }

    private void handleDirectory(final Path path) {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, e -> isDirectory(e) || !e.toString().startsWith("."))) {
            stream.forEach(p -> {
                if (isDirectory(p)) {
                    handleDirectory(p);
                } else {
                    handleFile(p);
                }
            });
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void handleFile(final Path path) {
        fileMetadataContainer.addFile(path);

        metricRegistry.counter(name("filesToHandle", "counter")).inc();
    }

}
