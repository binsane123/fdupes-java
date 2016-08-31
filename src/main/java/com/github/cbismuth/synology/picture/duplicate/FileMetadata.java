package com.github.cbismuth.synology.picture.duplicate;

class FileMetadata {

    private final String absolutePath;
    private final long size;

    FileMetadata(final String absolutePath, final long size) {
        super();

        this.absolutePath = absolutePath;
        this.size = size;
    }

    String getAbsolutePath() {
        return absolutePath;
    }

    long getSize() {
        return size;
    }

}
