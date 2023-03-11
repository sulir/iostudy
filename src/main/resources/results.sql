-- How many projects were successfully statically analyzed?

SELECT COUNT(*) as project_count
FROM projects;

-- What portion of projects' methods are reachable in the call graph (i.e., they are among "callers"), in total?

SELECT 100.0 * COUNT(*) / (
  SELECT SUM(methods) FROM projects
) AS reachable
FROM callers;

-- What portion of methods call natives?

SELECT 100.0 * COUNT(DISTINCT(caller_id)) / (
  SELECT COUNT(*) FROM callers
) AS portion
FROM callers
JOIN callers_natives USING (caller_id);

-- What portion of methods call IO natives?

SELECT 100.0 * COUNT(DISTINCT(caller_id)) / (
  SELECT COUNT(*) FROM callers
) AS portion
FROM callers
JOIN callers_natives USING (caller_id)
JOIN natives USING (native_id)
WHERE category IN ("invocation", "desktop", "time", "files", "network", "os");

-- What portion of methods call each category of natives?

SELECT category, 100.0 * COUNT(DISTINCT(caller_id)) / (
  SELECT COUNT(*) FROM callers
) AS portion
FROM callers
JOIN callers_natives USING (caller_id)
JOIN natives USING (native_id)
GROUP BY category;

-- What portion of methods call IO natives, per project, ordered by the portions?
--- io-per-project.tsv

SELECT a.project_id, 100.0 * native_callers / all_callers AS portion FROM (
  SELECT project_id, COUNT(DISTINCT(caller_id)) AS native_callers
  FROM callers
  JOIN callers_natives USING (caller_id)
  JOIN natives USING (native_id)
  WHERE category IN ("invocation", "desktop", "time", "files", "network", "os")
  GROUP BY project_id
) AS n
JOIN (
  SELECT project_id, COUNT(*) AS all_callers
  FROM callers
  GROUP BY project_id
) AS a
ON n.project_id = a.project_id
ORDER BY portion;

-- Which natives are called from the largest number of methods?
--- frequent-natives.tsv

SELECT class, signature, COUNT(*) AS called_from
FROM callers_natives
JOIN natives USING (native_id)
GROUP BY native_id
ORDER BY called_from DESC;

-- Which natives are called from the largest number of methods, per category?
--- frequent-natives-per-category.tsv

SELECT category, class, signature, COUNT(*) AS called_from
FROM callers_natives
JOIN natives USING (native_id)
GROUP BY native_id
ORDER BY category, called_from DESC;

-- What is the number of methods calling/not calling IO natives for each method size (number of "units"), ordered by the method sizes?
--- io-per-size.tsv



-- What is the portion of methods calling/not calling IO natives for test methods and for non-test methods?


