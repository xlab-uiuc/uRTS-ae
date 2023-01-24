#!/bin/bash

mode=$1
project=$2

function usage() {
    echo 'Usage: ./run_azure.sh [mode] [project]'
    echo '[mode]: (1) urts (2) reall (3) ekst (4) unsafe'
    echo '[project]: (1) hcommon (2) hbase (3) hdfs (4) alluxio (5) zookeeper'
    exit 1
}

function runExperiment() {
    if [ $project = "hcommon" ] || [ $project = "hbase" ] || [ $project = "hdfs" ] || [ $project = "alluxio" ] || [ $project = "zookeeper" ]
    then
        cd $mode/$project/
        echo '============== Start Running '$mode' '$project' =============='
        python3 run_azure.py | tee output.out
        cd ../..
        echo '============== Finish Running '$mode' '$project' =============='
        echo 'Done!'
    else
        usage
    fi
}

function main() {
    if [ -z $mode ] || [ -z $project ]; then
        usage
    elif [ $mode = "urts" ] || [ $mode = "retestall" ] || [ $mode = "ekstazi-ext" ] || [ $mode = "ekstazi-unsafe" ]; then
        runExperiment
    else
        usage
    fi
}

main
