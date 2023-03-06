CREATE TABLE IF NOT EXISTS natives (
    native_id INTEGER PRIMARY KEY,
    module TEXT NOT NULL,
    class TEXT NOT NULL,
    signature TEXT NOT NULL,
    category TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
    project_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    classes INTEGER NOT NULL,
    methods INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS callers (
    caller_id INTEGER PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
    class TEXT NOT NULL,
    signature TEXT NOT NULL,
    units INTEGER NOT NULL,
    empty BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS callers_natives (
    caller_id INTEGER NOT NULL REFERENCES callers(caller_id) ON DELETE CASCADE,
    native_id INTEGER NOT NULL REFERENCES natives(native_id),
    PRIMARY KEY (caller_id, native_id)
);
