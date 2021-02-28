package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook;

import java.io.IOException;
import java.io.InputStream;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.orgApacheCommons.io.output.ByteArrayOutputStream;

public class InputStreamWrapper extends InputStream {

    private InputStream delegate;

    private SocketMonitor socketMonitor;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    private static ThreadLocal<Object> reentryFlag = new ThreadLocal<>();

    private Throwable stackTrace = null;

    public SocketPackEvent genEvent() {
        if (stackTrace == null) {
            //最后socket关闭的时候，堆栈数据为空
            stackTrace = new Throwable();
        }
        SocketPackEvent socketPackEvent = new SocketPackEvent();
        socketPackEvent.stackTrace = stackTrace;
        socketPackEvent.body = byteArrayOutputStream.toByteArray();
        socketPackEvent.readAndWrite = SocketMonitor.statusRead;
        stackTrace = null;
        byteArrayOutputStream = new ByteArrayOutputStream();
        return socketPackEvent;
    }

    @Override
    public int read() throws IOException {
        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            int data = delegate.read();
            if (reEntry) {
                return data;
            }
            if (data > 0) {
                byteArrayOutputStream.write(data);
                check();
            }
            return data;
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    public InputStreamWrapper(InputStream delegate, SocketMonitor socketMonitor) {
        this.delegate = delegate;
        this.socketMonitor = socketMonitor;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        boolean reEntry = reentryFlag.get() != null;

        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            int readSize = delegate.read(bytes);
            if (reEntry) {
                return readSize;
            }
            if (readSize > 0) {
                byteArrayOutputStream.write(bytes, 0, readSize);
                check();
            }
            return readSize;
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        boolean reEntry = reentryFlag.get() != null;

        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            int readSize = delegate.read(bytes, off, len);
            if (reEntry) {
                return readSize;
            }
            if (readSize > 0) {
                byteArrayOutputStream.write(bytes, off, readSize);
                check();
            }
            return readSize;
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public long skip(long l) throws IOException {
        return delegate.skip(l);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        socketMonitor.destroy();
    }

    @Override
    public synchronized void mark(int i) {
        delegate.mark(i);
    }

    @Override
    public synchronized void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    private void check() {
        if (socketMonitor.getNowStatus() == SocketMonitor.statusRead) {
            return;
        }
        stackTrace = new Throwable();
        socketMonitor.checkStatus(SocketMonitor.statusRead);
    }
}
