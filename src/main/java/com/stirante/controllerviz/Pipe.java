package com.stirante.controllerviz;

import java.io.*;
import java.util.function.Function;

public class Pipe {

    private InputStream in;

    public Pipe(InputStream in) {
        this.in = in;
    }

    public void to(OutputStream out) {
        to(out, true);
    }

    public void to(OutputStream out, boolean close) {
        try {
            pipe(in, out);
            in.close();
            if (close) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void to(File out, boolean close) {
        try {
            to(new FileOutputStream(out), close);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void to(File out) {
        to(out, true);
    }

    public Pipe through(Function<byte[], byte[]> processor) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pipe(in, baos);
            in.close();
            in = new ByteArrayInputStream(processor.apply(baos.toByteArray()));
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Pipe from(InputStream in) {
        return new Pipe(in);
    }

    public static Pipe from(File in) {
        try {
            return from(new FileInputStream(in));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Pipe from(byte[] in) {
        return from(new ByteArrayInputStream(in));
    }

    private static void pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public String toString(boolean close) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        to(baos, close);
        return new String(baos.toByteArray());
    }

    public String toString() {
        return toString(true);
    }

}
