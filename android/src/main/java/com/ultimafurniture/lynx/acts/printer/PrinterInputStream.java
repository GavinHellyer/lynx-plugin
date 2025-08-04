package com.ultimafurniture.lynx.acts.printer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PrinterInputStream implements IHandler {
    /**
     * Creates a <code>PrinterInputStream</code> by
     * opening a connection to an actual file,
     * the file named by the path name <code>name</code>
     * in the file system.  A new <code>FileDescriptor</code>
     * object is created to represent this file
     * connection.
     * <p>
     * First, if there is a security
     * manager, its <code>checkRead</code> method
     * is called with the <code>name</code> argument
     * as its argument.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param name the system-dependent file name.
     * @throws FileNotFoundException if the file does not exist,
     *                               is a directory rather than a regular file,
     *                               or for some other reason cannot be opened for
     *                               reading.
     * @throws SecurityException     if a security manager exists and its
     *                               <code>checkRead</code> method denies read access
     *                               to the file.
     * @see java.lang.SecurityManager#checkRead(java.lang.String)
     */
    public PrinterInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }

    /**
     * Creates a <code>PrinterInputStream</code> by
     * opening a connection to an actual file,
     * the file named by the <code>File</code>
     * object <code>file</code> in the file system.
     * A new <code>FileDescriptor</code> object
     * is created to represent this file connection.
     * <p>
     * First, if there is a security manager,
     * its <code>checkRead</code> method  is called
     * with the path represented by the <code>file</code>
     * argument as its argument.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param file the file to be opened for reading.
     * @throws FileNotFoundException if the file does not exist,
     *                               is a directory rather than a regular file,
     *                               or for some other reason cannot be opened for
     *                               reading.
     * @throws SecurityException     if a security manager exists and its
     *                               <code>checkRead</code> method denies read access to the file.
     * @see java.io.File#getPath()
     * @see java.lang.SecurityManager#checkRead(java.lang.String)
     */
    public PrinterInputStream(File file) throws FileNotFoundException {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        if (!file.canRead()) {
            throw new FileNotFoundException("Invalid file path");
        }
        fd = -1;
        path = name;
        open(name);
    }

    @Override
    public boolean Connect(String addr, int port) throws Exception {
        return true;
    }

    @Override
    public boolean IsConnected() {
        return isOpened();
    }

    @Override
    public boolean Disconnect() throws Exception {
        close();
        return true;
    }

    @Override
    public boolean SendBytes(int offset, int length, byte[] bytes, int timeoutMs) throws Exception {
        return false;
    }

    @Override
    public boolean RecvBytes(int offset, int length, byte[] bytes, int timeoutMs) throws Exception {
        return true;
    }

    //region Stream
    /**
     * The path of the referenced file
     * (null if the stream is created with a file descriptor)
     */
    private final String path;

    private final Object closeLock = new Object();
    private volatile boolean closed = false;

    // wrap native call to allow instrumentation

    /**
     * Opens the specified file for reading.
     *
     * @param name the name of the file
     */
    private void open(String name) throws FileNotFoundException {
        open0(name);
    }

    /**
     * Reads a byte of data from this input stream. This method blocks
     * if no input is yet available.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * file is reached.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] read() throws IOException {
        return read0();
    }

    /**
     * Reads up to <code>b.length</code> bytes of data from this input
     * stream into an array of bytes. This method blocks until some input
     * is available.
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the file has been reached.
     * @throws IOException if an I/O error occurs.
     */
    public int read(byte b[]) throws IOException {
        return readBytes(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the file has been reached.
     * @throws NullPointerException      If <code>b</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If <code>off</code> is negative,
     *                                   <code>len</code> is negative, or <code>len</code> is greater than
     *                                   <code>b.length - off</code>
     * @throws IOException               if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    /**
     * Closes this file input stream and releases any system resources
     * associated with the stream.
     *
     * <p> If this stream has an associated channel then the channel is closed
     * as well.
     *
     * @throws IOException if an I/O error occurs.
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }
        close0();
    }

    public final int getFD() throws IOException {
        if (fd != -1) {
            return fd;
        }
        throw new IOException();
    }

    protected void finalize() throws IOException {
        if (fd != -1) {
            close();
        }
    }
    //endregion Stream

    //region JNI
    private final int fd;

    private static native void initIDs();

    /**
     * Opens the specified file for reading.
     *
     * @param name the name of the file
     */
    private native void open0(String name) throws FileNotFoundException;

    private native byte[] read0() throws IOException;

    /**
     * Reads a subarray as a sequence of bytes.
     *
     * @param b   the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @throws IOException If an I/O error has occurred.
     */
    private native int readBytes(byte b[], int off, int len) throws IOException;

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream.
     *
     * <p>The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. If <code>n</code> is negative, the method
     * will try to skip backwards. In case the backing file does not support
     * backward skip at its current position, an <code>IOException</code> is
     * thrown. The actual number of bytes skipped is returned. If it skips
     * forwards, it returns a positive value. If it skips backwards, it
     * returns a negative value.
     *
     * <p>This method may skip more bytes than what are remaining in the
     * backing file. This produces no exception and the number of bytes skipped
     * may include some number of bytes that were beyond the EOF of the
     * backing file. Attempting to read from the stream after skipping past
     * the end will result in -1 indicating the end of the file.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if n is negative, if the stream does not
     *                     support seek, or if an I/O error occurs.
     */
    public native long skip(long n) throws IOException;

    public native boolean isOpened();

    /**
     * Returns an estimate of the number of remaining bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. Returns 0 when the file
     * position is beyond EOF. The next invocation might be the same thread
     * or another thread. A single read or skip of this many bytes will not
     * block, but may read or skip fewer bytes.
     *
     * <p> In some cases, a non-blocking read (or skip) may appear to be
     * blocked when it is merely slow, for example when reading large
     * files over slow networks.
     *
     * @return an estimate of the number of remaining bytes that can be read
     * (or skipped over) from this input stream without blocking.
     * @throws IOException if this file input stream has been closed by calling
     *                     {@code close} or an I/O error occurs.
     */
    public native int available() throws IOException;

    private native void close0() throws IOException;

    static {
        System.loadLibrary("NativeInputStream");
        initIDs();
    }
    //endregion JNI
}
