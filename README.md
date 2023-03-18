# A study of IO methods in Java

This repository contains the source code of the analysis programs for our paper "Outside the Sandbox: A Study of Input/Output Methods in Java" (under review).

A complete dataset with the results is available at [OSF.io](https://doi.org/10.17605/OSF.IO/CNSRJ).

The Docker image can be built with the following command:
```shell
docker build -t iostudy .
```

To reproduce the study, first prepare a data directory, e.g., `~/study`. At the beginning, it should contain at least the file `projects.txt` with a list of GitHub repositories to analyze. To use the list of projects analyzed in the original study, you can [download it](https://osf.io/download/9b8wc/).

Then run the study with the following command:
```shell
docker run -itv ~/study:/opt/data iostudy
```

The results of the analysis will be then written to `~/study/results`.

More details will be added soon.
