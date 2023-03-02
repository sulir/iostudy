CREATE TABLE IF NOT EXISTS "natives" (
	"native_id"	INTEGER,
	"module"	TEXT NOT NULL,
	"class"	TEXT NOT NULL,
	"signature"	TEXT NOT NULL,
	"category"	TEXT NOT NULL,
	PRIMARY KEY("native_id")
);
