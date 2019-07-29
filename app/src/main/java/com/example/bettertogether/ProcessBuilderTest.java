package com.example.bettertogether;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ProcessBuilderTest {
    public void main(String arg[]) throws IOException {

    }

    public void writeCommand(String token) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "curl",
                "-X POST --header \"Authorization: key = <AIzaSyDaOiWuPf5sCXO8bV0ujizMAsgCsU_P6_A>\"",
                "--Header \"Content-Type: application/json\"",
                "https://fcm.googleapis.com/fcm/send",
                "-d \"{\"to\":\"" + token + "\",\"notification\":{\"title\":\"Better Together\", \"body\":\"New group created!\"},\"priority\":10}\"");

        pb.directory(new File("/home/reneeli/Pictures"));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        InputStream is = p.getInputStream();

        FileOutputStream outputStream = new FileOutputStream(
                "/home/reneeli/Pictures/profile_icon.jpg");

        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] bytes = new byte[100];
        int numberByteReaded;
        while ((numberByteReaded = bis.read(bytes, 0, 100)) != -1) {
            outputStream.write(bytes, 0, numberByteReaded);
            Arrays.fill(bytes, (byte) 0);
        }
        outputStream.flush();
        outputStream.close();
    }
}