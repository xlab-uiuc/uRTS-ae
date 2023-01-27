# Test Selection for Unified Regression Testing
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7566915.svg)](https://doi.org/10.5281/zenodo.7566915)

This is the artifact of the research paper:

[Test Selection for Unified Regression Testing](https://mir.cs.illinois.edu/~swang516/uRTS.pdf)\
In Proceedings of the 45th IEEE/ACM International Conference on Software Engineering (ICSE'23), 2023.

## Code
The source code of modified-Ekstazi and uRTS mentioned in the paper are under [code](https://github.com/xlab-uiuc/uRTS-ae/tree/main/code) directory.

## Data
Check [data](https://github.com/xlab-uiuc/uRTS-ae/tree/main/data) directory to get all data in the paper.

## Experiment
To reproduce the experimental results, follow the instruction in [experiment](https://github.com/xlab-uiuc/uRTS-ae/tree/main/experiment) directory.\
The experiments have two phases:
1. run tests of various commits and configuration to obtain log files
2. process log files

To fully reprodcue the first phase is time-consuming and may take up to 2000+ hours machine time. For the first phase, reviewers may want to run for only one of the five projects on a laptop.\
We provide our results of the first phase in [data/csv_files](https://github.com/xlab-uiuc/uRTS-ae/tree/main/data/csv_files).
Reviewer can quickly get the results by running the second phase, detailed instructions are provided [here](https://github.com/xlab-uiuc/uRTS-ae/tree/main/experiment#parse-experiment-results).
