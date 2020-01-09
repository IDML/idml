#!/bin/sh
echo "Generating index"
cd docs/src/main/tut/functions
./generateindex.py > index.md
cd ../../../../../
