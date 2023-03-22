# A study of IO methods in Java

This repository contains the source code of the programs for our paper "Outside the Sandbox: A Study of Input/Output Methods in Java" (under review).

A complete dataset with the results is available at [OSF.io](https://doi.org/10.17605/OSF.IO/CNSRJ).

## Reproducibility

The Docker image can be built with the following command:
```shell
docker build -t iostudy .
```

Next, use with the following command to reproduce the complete analysis:
```shell
docker run -itv ~/study:/opt/data iostudy
```
where `~/study` is a directory on the host that will be shared with the Docker container. The results of the analysis will be then written to `~/study/results`.

The whole process can last a few days. For demonstration purposes, it is possible to analyze a small corpus of 5 projects only:
```shell
docker run -itv ~/study:/opt/data iostudy download-few static results
```

## Arguments

The study contains multiple phases. A list of phases to run can be supplied as arguments to the Docker image's entry point, e.g.,
```shell
docker run -itv ~/study:/opt/data iostudy build-all static
```
will build all projects from source code and then perform static analysis on this corpus.

Available phases are:
- `natives` - Export a list of native methods in the bundled Java Runtime Environment to file `natives.tsv`. It can be subsequently used for manual categorization.
- `download-all` - Download and extract the pre-built corpus of 821 projects used in the original study to the directory named `corpus`.
- `download-few` - Download a small pre-built corpus of 5 projects for a demonstration to the `corpus` directory.
- `build-all` - Build the original 812 projects from source code using Maven. It is advised to create `ghtoken.txt` in the root directory of the container's data volume. This file should contain a generated GitHub personal access token (starting with `ghp_`) with public-only access (no scopes selected, i.e., without any additional permissions). The output is written to `corpus/`.
- `build-custom` - Build a custom list of projects from source code using Maven. The list is a text file `projects.txt` in the root directory of the data volume. It consists of GitHub repository names (e.g., `apache/commons-cli`) separated by newlines. Similarly to `build-all`, creating `ghtoken.txt` is advised, and the output is saved to `corpus/`.
- `static` - Perform static analysis on the corpus of projects prepared by the download/build phases. The output is written to the SQLite 3 database `results.db3`.
- `results` - Perform SQL queries on the static analysis results. The output is written as text and TSV files to `results/`.

If no options are supplied, the default list of phases is `download-all static results`.

## Files

The Docker image has one volume, `/opt/data`, which is used to exchange all input and output files with the host. Its structure, when considering all possible phases, is as follows:

```
corpus/
    user__repository/
        deps/
            *.jar
        jars/
            *.jar
results/
    results.txt
    *.tsv
ghtoken.txt
natives.tsv
projects.txt
results.db3