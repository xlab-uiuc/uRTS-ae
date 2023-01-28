# Experiment

## Setup Environment
Pre-requisites for use:
- Java8 installed
- Maven installed
- Protobuf2.5 installed
- Python3 installed and dependency packages installed: matplotlib, numpy, pandas, scipy

You can also run the following command to install the dependencies.
```
$ bash setup_ubuntu.sh
```

## Install uRTS / Ekstazi

Use the following command to install uRTS and Ekstazi:
```
$ bash install_tool.sh urts
$ bash install_tool.sh ekst
```

## Run Experiment
*Warning*: This step would require 2000+ hours to finish on a single machine.
- `hcommon` takes around 140+ hours
- `hdfs` takes around 1700+ hours
- `hbase` takes around 90+ hours
- `alluxio` takes around 60+ hours
- `zookeeper` takes around 80+ hours

Use `run.sh` script to run all experiments in our evaluation.
```
$ bash run.sh [mode] [project]
```
`mode` can be `urts`, `reall`, `ekst` (Ekstazi+ in the paper), and  `unsafe` (Ekstazi- in the paper);\
`project` can be `hcommon`, `hdfs`, `hbase`, `alluxio`, and `zookeeper`.

Because this step is time-consuming, we provided the results of the five projects in [data/csv_files](https://github.com/xlab-uiuc/uRTS-ae/tree/main/data/csv_files).

## Parse Experiment Results
After finishing all 4 modes for one project, you can follow the steps to get CSV files and PDF figures shown in the paper.

For example, to get HCommon results, execute:
```
# Parse hcommon results and put CSV files into `csv_files/hcommon/` directory
$ bash parse.sh hcommon csv_files/hcommon/

# Generate the summary CSV of all 4 modes for hcommon
$ bash parse_csv.py hcommon csv_files/hcommon/

# Generate figures into `figures/hcommon` directory
$ python3 draw.py hcommon csv_files/hcommon/summary.csv figures/hcommon 
```

PS: We have provided all experimental results as CSV format stored in [data/csv_files](https://github.com/xlab-uiuc/uRTS-ae/tree/main/data/csv_files).
You can generate figures with the provided results by calling:
```
$ python3 draw.py hcommon ../data/csv_files/hcommon/summary.csv figures/hcommon
$ python3 draw.py hdfs ../data/csv_files/hdfs/summary.csv figures/hdfs
$ python3 draw.py hbase ../data/csv_files/hbase/summary.csv figures/hbase
$ python3 draw.py alluxio ../data/csv_files/alluxio/summary.csv figures/alluxio
$ python3 draw.py zookeeper ../data/csv_files/zookeeper/summary.csv figures/zookeeper
```
The output figures will be saved in `figures` under the current directory.
The output figures are the main results we show in our [paper](https://github.com/xlab-uiuc/uRTS-ae/blob/main/paper.pdf) Figure 7.
