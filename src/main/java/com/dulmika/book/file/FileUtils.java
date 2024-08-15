package com.dulmika.book.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUtils {
    public static byte[] readFileFromLocation(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }

        try {
            //todo
            // Convert the String file path to a Path object.
            // cause we need a Path object for read or write a file.
            Path filePath = Paths.get(fileUrl);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.warn("No file found in the path {}", fileUrl);
        }
        return null;
    }
}
