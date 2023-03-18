package com.sulir.github.iostudy.results;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.Program;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Program(name = "results")
public class ResultAnalysis implements Runnable {
    private static final Path dir = Path.of("results");
    private static final Path file = Path.of("results.txt");

    @Override
    public void run() {
        try (InputStream sqlStream = Database.class.getResourceAsStream("/results.sql");
             PrintWriter writer = new PrintWriter(Files.createDirectories(dir).resolve(file).toFile())) {
            assert sqlStream != null;
            String sqlContent = new String(sqlStream.readAllBytes());

            for (String statement : sqlContent.split(";\\s*")) {
                Query query = new Query(statement);
                try {
                    query.perform(writer, dir);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
