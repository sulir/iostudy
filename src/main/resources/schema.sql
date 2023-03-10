CREATE TABLE IF NOT EXISTS natives (
    native_id INTEGER PRIMARY KEY,
    module TEXT NOT NULL,
    class TEXT NOT NULL,
    signature TEXT NOT NULL,
    category TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS natives_category ON natives (category);

CREATE TABLE IF NOT EXISTS projects (
    project_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    classes INTEGER NOT NULL,
    methods INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS projects_classes ON projects (classes);
CREATE INDEX IF NOT EXISTS projects_methods ON projects (methods);

CREATE TABLE IF NOT EXISTS callers (
    caller_id INTEGER PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
    class TEXT NOT NULL,
    signature TEXT NOT NULL,
    units INTEGER NOT NULL,
    empty BOOLEAN NOT NULL,
    test BOOLEAN NOT NULL
);

CREATE INDEX IF NOT EXISTS callers_units ON callers (units);
CREATE INDEX IF NOT EXISTS callers_empty ON callers (empty);

CREATE TABLE IF NOT EXISTS callers_natives (
    caller_id INTEGER NOT NULL REFERENCES callers(caller_id) ON DELETE CASCADE,
    native_id INTEGER NOT NULL REFERENCES natives(native_id),
    PRIMARY KEY (caller_id, native_id)
);

CREATE INDEX IF NOT EXISTS cn_caller_id ON callers_natives (caller_id);
CREATE INDEX IF NOT EXISTS cn_native_id ON callers_natives (native_id);

CREATE TABLE IF NOT EXISTS benchmarks (
    benchmark_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS dyn_callers (
    dyn_caller_id INTEGER PRIMARY KEY,
    benchmark_id INTEGER NOT NULL REFERENCES benchmarks(benchmark_id) ON DELETE CASCADE,
    class TEXT NOT NULL,
    signature TEXT NOT NULL,
    bytes INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS dyn_callers_natives (
    dyn_caller_id INTEGER NOT NULL REFERENCES dyn_callers(dyn_caller_id) ON DELETE CASCADE,
    native_id INTEGER NOT NULL REFERENCES natives(native_id),
    PRIMARY KEY (dyn_caller_id, native_id)
);
