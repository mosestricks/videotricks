package com.lightricks.videotricks.util;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;

public class DataSource {
    private final FileDescriptor fileDescriptor;
    private final long offset;
    private final long length;
    private final Closeable closeable;

    public DataSource(FileDescriptor fileDescriptor, long offset, long length,
                      Closeable closeable) {
        this.fileDescriptor = fileDescriptor;
        this.offset = offset;
        this.length = length;
        this.closeable = closeable;
    }

    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    public void release() {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
