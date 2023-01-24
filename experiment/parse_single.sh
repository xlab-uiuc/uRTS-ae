#!/bin/bash
cur_dir=$(pwd)

proj=$1
mode=$2
output_dir=${cur_dir}/$3
csv_dir=$4
 

wd=$(echo ${output_dir})
echo "===========Start Parsing Result in "${wd}"==========="

(cd $wd && grep -ihR "evaluation:" --include \*.out > tmp) 
python3 parse_util.py $proj $mode ${wd}/tmp ${csv_dir}/${proj}-${mode}.csv

echo "===========Finish Parsing Result in "${wd}"==========="
