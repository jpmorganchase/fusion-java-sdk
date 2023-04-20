package io.github.jpmorganchase.fusion.pact.util;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FileHelper {

    private FileHelper(){}

    @SneakyThrows
    public static String readContentsFromStream(InputStream is){
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = is.read(bytes);
        while(read!=-1){
            baos.write(bytes, 0, read);
            read = is.read(bytes);
        }
        baos.close();
        return baos.toString();
    }


}
