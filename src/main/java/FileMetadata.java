import com.google.common.base.MoreObjects;

import java.util.Objects;

class FileMetadata implements Comparable<FileMetadata> {

    private final String absolutePath;
    private final long size;

    FileMetadata(final String absolutePath, final long size) {
        super();

        this.absolutePath = absolutePath;
        this.size = size;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        final FileMetadata that = (FileMetadata) o;
        return Objects.equals(absolutePath, that.absolutePath);
    }

    @Override
    public int compareTo(final FileMetadata o) {
        return absolutePath.compareTo(o.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath);
    }

    String getAbsolutePath() {
        return absolutePath;
    }

    long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("absolutePath", absolutePath)
                          .add("size", size)
                          .toString();
    }

}
