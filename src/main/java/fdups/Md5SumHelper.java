package fdups;

import com.google.common.base.Throwables;
import com.google.common.primitives.UnsignedBytes;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

class Md5SumHelper {

    private final Optional<String> binaryName;

    Md5SumHelper() {
        super();

        binaryName = new Md5SumCommandChecker().getBinaryName();
    }

    Md5SumHelper(final Optional<String> binaryName) {
        this.binaryName = binaryName;
    }

    String md5sum(final FileMetadata fileMetadata) {
        if (binaryName.isPresent()) {
            return nativeMd5Sum(fileMetadata);
        } else {
            return jvmMd5Sum(fileMetadata);
        }
    }

    private String jvmMd5Sum(final FileMetadata fileMetadata) {
        try {
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

    private String nativeMd5Sum(final FileMetadata fileMetadata) {
        try {
            return new ProcessExecutor().command(getNativeMd5SumCommand(fileMetadata))
                                        .readOutput(true)
                                        .redirectOutput(Slf4jStream.of("md5sum").asTrace())
                                        .execute()
                                        .outputUTF8()
                                        .split("\\s")[0];
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Iterable<String> getNativeMd5SumCommand(final FileMetadata fileMetadata) {
        final Collection<String> command = newArrayList();

        if (binaryName.isPresent() && Objects.equals("md5sum", binaryName.get())) {
            command.add("md5sum");
        } else if (binaryName.isPresent() && Objects.equals("md5", binaryName.get())) {
            command.add("md5");
            command.add("-q");
        } else {
            throw new UnsupportedOperationException(format("Unsupported binary name [%s]!", binaryName));
        }

        command.add(fileMetadata.getAbsolutePath());

        return command;
    }

}
