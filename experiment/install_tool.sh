#!/bin/bash

if [[ $1 == "urts" || $1 == "reall" ]]; then
    (cd ../code/urts_code/ && mvn install -DskipTests)
elif [[ $1 == "ekst" || $1 == "unsafe" ]]; then
    (cd ../code/ekstazi_code && mvn install -DskipTests)
else
    echo "Must specify tool name, supported value: reall, ekst, unsafe, urts"
fi
