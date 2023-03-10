package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.code.ProjectPersistence;
import com.sulir.github.iostudy.methods.DynamicCaller;

import java.sql.*;

public class BenchmarkPersistence {
    private static final String REPLACE_BENCHMARKS = "REPLACE INTO benchmarks " +
            "(name) VALUES (?)";
    private static final String INSERT_CALLER = "INSERT INTO dyn_callers " +
            "(benchmark_id, class, signature, bytes) VALUES (?, ?, ?, ?)";

    private final Benchmark benchmark;
    private long benchmarkId;

    public BenchmarkPersistence(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    public void saveToDB() throws SQLException {
        benchmarkId = saveBenchmark();

        for (DynamicCaller caller : benchmark.getCallers()) {
            saveCaller(caller);
        }

        Database.getConnection().commit();
    }

    private long saveBenchmark() throws SQLException {
        Connection connection = Database.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(REPLACE_BENCHMARKS,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, benchmark.getName());
            statement.execute();

            ResultSet newId = statement.getGeneratedKeys();
            newId.next();
            return newId.getLong(1);
        }
    }

    private void saveCaller(DynamicCaller caller) throws SQLException {
        Connection connection = Database.getConnection();

        try (PreparedStatement insert = connection.prepareStatement(INSERT_CALLER, Statement.RETURN_GENERATED_KEYS)) {
            Database.setValues(insert, benchmarkId, caller.getClassName(), caller.getSignature(),
                    caller.getByteCount());
            insert.execute();

            ResultSet newId = insert.getGeneratedKeys();
            newId.next();
            ProjectPersistence.saveCalledNatives(caller, newId.getLong(1), "dyn_");
        }
    }
}
