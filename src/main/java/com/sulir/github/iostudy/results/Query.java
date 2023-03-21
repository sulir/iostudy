package com.sulir.github.iostudy.results;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.sulir.github.iostudy.Database;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.opencsv.ICSVWriter.*;

public class Query {
    private static final Pattern descriptionPattern = Pattern.compile("^-- (.*)$", Pattern.MULTILINE);
    private static final Pattern filePattern = Pattern.compile("^---\\s+(\\S+)\\s*$", Pattern.MULTILINE);

    private final String description;
    private final String file;
    private final String sql;

    public Query(String statement) {
        description = descriptionPattern.matcher(statement).results()
                .map(r -> r.group(1))
                .collect(Collectors.joining("\n"));

        Matcher fileMatcher = filePattern.matcher(statement);
        file = fileMatcher.find() ? fileMatcher.group(1) : null;

        sql = statement.lines()
                .filter(l -> !l.startsWith("--"))
                .collect(Collectors.joining("\n"));
    }

    public void perform(PrintWriter writer, Path directory) throws SQLException, IOException {
        if (sql.isBlank())
            return;
        writer.println(description);

        try (Statement statement = Database.getConnection().createStatement()) {
            statement.execute(sql);
            ResultSet resultSet = statement.getResultSet();

            if (file == null) {
                writer.println();
                writeTable(resultSet, writer);
            } else {
                writer.println("--> " + file);
                writeTable(resultSet, new PrintWriter(directory.resolve(file).toFile()));
            }
        }
        writer.println();
    }

    private void writeTable(ResultSet resultSet, PrintWriter writer) throws IOException {
        ICSVWriter tsvWriter = new CSVWriter(writer, '\t', NO_QUOTE_CHARACTER,
                    DEFAULT_ESCAPE_CHARACTER, DEFAULT_LINE_END);

        try {
            tsvWriter.writeAll(resultSet, true);
        } catch (SQLException e) {
            // OpenCSV does not handle empty result set
        }

        tsvWriter.flush();
    }
}
