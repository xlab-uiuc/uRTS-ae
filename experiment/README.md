# Experiment

## Setup Environment
```
$ bash setup_ubuntu.sh
```

## Install uRTS / Ekstazi

Use the following command to install urts or ekstazi:
```
$ bash install_tool.sh [tool]
```
`tool` can be `urts`, `reall` (`ReTestAll` in paper), `ekst` (`Ekstazi+` in paper), and `unsafe` (`Ekstazi-` in paper).

## Run Experiment
Use `run.sh` script to run all experiments in our evaluation.
```
$ bash run.sh [mode] [project]
```
`mode` can be `urts`, `reall`, `ekst`, and  `unsafe`;\
`project` can be `hcommon`, `hdfs`, `hbase`, `alluxio`, and `zookeeper`.

## Parse Experiment Results
After finishing all 4 modes for one project, you can follow the steps to get CSV files and PDF figures shown in the paper.

For example, to get HCommon result, execute:
```
# Parse hcommon results and put CSV files into `csv_files/hcommon/` directory
$ bash parse.sh hcommon csv_files/hcommon/

# Generate the summary CSV of all 4 modes for hcommon
$ bash parse_csv.py hcommon csv_files/hcommon/

# Generate figures into `figures/hcommon` directory
$ python3 draw.py hcommon csv_files/hcommon/summary.csv figures/hcommon 
```

