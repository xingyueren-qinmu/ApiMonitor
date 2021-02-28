package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook;

import java.io.IOException;
import java.io.OutputStream;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.orgApacheCommons.io.output.ByteArrayOutputStream;

public class OutputStreamWrapper extends OutputStream {
    private OutputStream delegate;

    private SocketMonitor socketMonitor;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    private static ThreadLocal<Object> reentryFlag = new ThreadLocal<>();

    private Throwable stackTrace = null;


    public SocketPackEvent genEvent() {
        SocketPackEvent socketPackEvent = new SocketPackEvent();
        socketPackEvent.stackTrace = stackTrace;
        socketPackEvent.body = byteArrayOutputStream.toByteArray();
        socketPackEvent.readAndWrite = SocketMonitor.statusWrite;
        stackTrace = null;
        byteArrayOutputStream = new ByteArrayOutputStream();
        return socketPackEvent;
    }

    public OutputStreamWrapper(OutputStream delegate, SocketMonitor socketMonitor) {
        this.delegate = delegate;
        this.socketMonitor = socketMonitor;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        delegate.write(bytes);

        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            if (reEntry) {
                return;
            }
            byteArrayOutputStream.write(bytes);
            check();
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        delegate.write(bytes, i, i1);
        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            if (reEntry) {
                return;
            }
            byteArrayOutputStream.write(bytes, i, i1);
            check();
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        socketMonitor.destroy();
    }

    @Override
    public void write(int i) throws IOException {
        delegate.write(i);
        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            if (reEntry) {
                return;
            }
            byteArrayOutputStream.write(i);
            check();
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    private void check() {
        if (socketMonitor.getNowStatus() == SocketMonitor.statusWrite) {
            return;
        }
        stackTrace = new Throwable();
        socketMonitor.checkStatus(SocketMonitor.statusWrite);
    }
}
