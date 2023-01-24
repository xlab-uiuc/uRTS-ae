#!/bin/bash
set -e

proj=$1   # hcommon hdfs hbase alluxio zookeeper
csv_dir=$2   # target directory to store parsed csv files

modes=("urts" "ekst" "unsafe" "reall")
 
# Print array values in  lines
echo "Print every element in new line"
for mode in ${modes[*]}; do
     bash parse_single.sh $proj $mode ${mode}/${proj}/ $csv_dir
done



