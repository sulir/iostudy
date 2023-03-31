-- How many projects were successfully statically analyzed?

SELECT
    COUNT(*) as project_count
FROM projects;

-- What is the minimum, maximum, average, and total number of classes in the analyzed projects?

SELECT
    MIN(classes) AS minimum,
    MAX(classes) AS maximum,
    AVG(classes) as average,
    SUM(classes) AS total
FROM projects;

-- What is the minimum, maximum, average, and total number of methods in the analyzed projects?

SELECT
    MIN(methods) AS minimum,
    MAX(methods) AS maximum,
    AVG(methods) as average,
    SUM(methods) AS total
FROM projects;

-- What portion of projects' methods are reachable in the call graph, in total?

SELECT
    100.0 * COUNT(*) / (
        SELECT SUM(methods) FROM projects
    ) AS reachable_percent
FROM callers;

-- What portion of methods call natives?

SELECT
    100.0 * COUNT(DISTINCT(caller_id)) / (
        SELECT COUNT(*) FROM callers
    ) AS native_percent
FROM callers
JOIN callers_natives USING (caller_id);

-- What portion of methods call IO natives?

SELECT
    100.0 * COUNT(DISTINCT(caller_id)) / (
        SELECT COUNT(*) FROM callers
    ) AS io_percent
FROM io_calls;

-- What portion of methods call each category of natives?

SELECT
    category,
    100.0 * COUNT(DISTINCT(caller_id)) / (
        SELECT COUNT(*) FROM callers
    ) AS percent
FROM callers
JOIN callers_natives USING (caller_id)
JOIN natives USING (native_id)
GROUP BY category
ORDER BY CASE category
    WHEN 'internal' THEN 1
    WHEN 'invocation' THEN 2
    WHEN 'desktop' THEN 3
    WHEN 'time' THEN 4
    WHEN 'files' THEN 5
    WHEN 'network' THEN 6
    WHEN 'os' THEN 7
END;

-- What portion of methods call IO natives, per project, ordered by the portions?
--- io-per-project.tsv

SELECT
    a.name AS project_name,
    100.0 * COALESCE(io_callers, 0) / all_callers AS io_percent
FROM (
    SELECT
        project_id,
        COUNT(DISTINCT(caller_id)) AS io_callers
    FROM io_calls
    GROUP BY project_id
) AS n
RIGHT JOIN (
    SELECT
        project_id,
        name,
        COUNT(*) AS all_callers
    FROM callers
    JOIN projects USING (project_id)
    GROUP BY project_id
) AS a
ON n.project_id = a.project_id
ORDER BY io_percent;

-- Which natives are called from the largest number of methods?
--- frequent-natives.tsv

SELECT
    class,
    signature,
    COUNT(*) AS called_from
FROM callers_natives
JOIN natives USING (native_id)
GROUP BY native_id
ORDER BY called_from DESC;

-- Which natives are called from the largest number of methods, top 10 per category?
--- frequent-natives-per-category.tsv

SELECT * FROM (
    SELECT
        category,
        ROW_NUMBER() OVER (PARTITION BY category ORDER BY COUNT(*) DESC) AS rank,
        class,
        signature,
        COUNT(*) AS called_from
    FROM callers_natives
    JOIN natives USING (native_id)
    GROUP BY native_id
    ORDER BY category, called_from DESC, native_id
)
WHERE rank <= 10;

-- What is the number of methods calling/not calling IO natives for each method size, ordered by the method sizes?
--- io-per-size.tsv

SELECT
    COALESCE(n.units, t.units) AS size,
    COALESCE(io, 0) AS io,
    COALESCE(total, 0) - COALESCE(io, 0) AS non_io
FROM (
    SELECT
        units,
        COUNT(DISTINCT(caller_id)) AS io
    FROM io_calls
    GROUP BY units
) AS n
FULL JOIN (
    SELECT
        units,
        COUNT(*) AS total FROM callers
    GROUP BY units
) AS t
ON n.units = t.units
ORDER BY size;

-- What is the portion of test/non-test methods, and what portion of test/non-test methods call IO natives?

SELECT
    CASE WHEN a.test IS TRUE then 'yes' ELSE 'no' END AS test,
    100.0 * all_callers / SUM(all_callers) OVER () AS test_percent,
    100.0 * io_callers / all_callers AS io_percent
FROM (
    SELECT
        test,
        COUNT(DISTINCT(caller_id)) AS io_callers
    FROM io_calls
    GROUP BY test
) AS n
JOIN (
    SELECT
        test,
        COUNT(*) AS all_callers
    FROM callers
    GROUP BY test
) AS a
ON n.test = a.test
ORDER BY test DESC;

-- How many benchmarks were successfully dynamically analyzed?

SELECT
    COUNT(*) as benchmark_count
FROM benchmarks;

-- What portion of the executed methods call natives?

SELECT
    100.0 * COUNT(DISTINCT(dyn_caller_id)) / (
        SELECT COUNT(*) FROM dyn_callers
    ) AS native_percent
FROM dyn_callers
JOIN dyn_callers_natives USING (dyn_caller_id);

-- What portion of the executed methods call IO natives?

SELECT
    100.0 * COUNT(DISTINCT(dyn_caller_id)) / (
        SELECT COUNT(*) FROM dyn_callers
    ) AS io_percent
FROM dyn_io_calls;

-- What portion of methods call each category of natives at runtime?

SELECT
    category,
    100.0 * COUNT(DISTINCT(dyn_caller_id)) / (
        SELECT COUNT(*) FROM dyn_callers
    ) AS percent
FROM dyn_callers
JOIN dyn_callers_natives USING (dyn_caller_id)
JOIN natives USING (native_id)
GROUP BY category
ORDER BY CASE category
    WHEN 'internal' THEN 1
    WHEN 'invocation' THEN 2
    WHEN 'desktop' THEN 3
    WHEN 'time' THEN 4
    WHEN 'files' THEN 5
    WHEN 'network' THEN 6
    WHEN 'os' THEN 7
END;

-- What portion of methods call IO natives at runtime, per benchmark, ordered by the portions?
--- dyn-io-per-project.tsv

SELECT
    a.name AS benchmark_name,
    100.0 * COALESCE(io_callers, 0) / all_callers AS io_percent
FROM (
    SELECT
        benchmark_id,
        COUNT(DISTINCT(dyn_caller_id)) AS io_callers
    FROM dyn_io_calls
    GROUP BY benchmark_id
) AS n
RIGHT JOIN (
    SELECT
        benchmark_id,
        name,
        COUNT(*) AS all_callers
    FROM dyn_callers
    JOIN benchmarks USING (benchmark_id)
    GROUP BY benchmark_id
) AS a
ON n.benchmark_id = a.benchmark_id
ORDER BY io_percent;

-- Which natives are called from the largest number of methods at runtime?
--- dyn-frequent-natives.tsv

SELECT
    class,
    signature,
    COUNT(*) AS called_from
FROM dyn_callers_natives
JOIN natives USING (native_id)
GROUP BY native_id
ORDER BY called_from DESC;

-- Which natives are called from the largest number of methods at runtime, top 10 per category?
--- dyn-frequent-natives-per-category.tsv

SELECT * FROM (
    SELECT
        category,
        ROW_NUMBER() OVER (PARTITION BY category ORDER BY COUNT(*) DESC) AS rank,
        class,
        signature,
        COUNT(*) AS called_from
    FROM dyn_callers_natives
    JOIN natives USING (native_id)
    GROUP BY native_id
    ORDER BY category, called_from DESC, native_id
)
WHERE rank <= 10;

-- What is the number of methods calling/not calling IO natives at runtime for each method size, ordered by the sizes?
--- dyn-io-per-size.tsv

SELECT
    COALESCE(n.bytes, t.bytes) AS size,
    COALESCE(io, 0) AS io,
    COALESCE(total, 0) - COALESCE(io, 0) AS non_io
FROM (
    SELECT
        bytes,
        COUNT(DISTINCT(dyn_caller_id)) AS io
    FROM dyn_io_calls
    GROUP BY bytes
) AS n
FULL JOIN (
    SELECT
        bytes,
        COUNT(*) AS total FROM dyn_callers
    GROUP BY bytes
) AS t
ON n.bytes = t.bytes
ORDER BY size;
