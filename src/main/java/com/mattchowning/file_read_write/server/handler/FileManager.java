package com.mattchowning.file_read_write.server.handler;

import java.io.*;

import io.netty.buffer.ByteBuf;

public class FileManager {

    private final String fullFilePath;

    public FileManager(String fullFilePath) {
        this.fullFilePath = fullFilePath;
    }

    public File getFile() {
        return new File(fullFilePath);
    }

    public boolean writeFileContent(ByteBuf byteBuf) {
        try {
            byte[] bytes = getBytes(byteBuf);
            FileOutputStream fileStream = new FileOutputStream(getFile());
            fileStream.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] getBytes(ByteBuf buf) {
        byte[] result = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), result);
        return result;
    }
}
