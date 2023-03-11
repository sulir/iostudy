package com.sulir.github.iostudy.results;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.Program;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.SQLException;

@Program(name = "results", arguments = "<dir>")
public class ResultAnalysis implements Runnable {
    private final String directory;

    public ResultAnalysis(String directory) {
        this.directory = directory;
        Database.setDirectory(directory);
    }

    @Override
    public void run() {
        try (InputStream sqlStream = Database.class.getResourceAsStream("/results.sql");
             PrintWriter writer = new PrintWriter(Path.of(directory, "results.txt").toFile())) {
            assert sqlStream != null;
            String sqlContent = new String(sqlStream.readAllBytes());

            for (String statement : sqlContent.split(";\\s*")) {
                Query query = new Query(statement);
                try {
                    query.perform(writer, directory);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
