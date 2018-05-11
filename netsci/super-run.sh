#!/bin/csh -f
set SEEDS = $1
set TREES = $2
set COMPS = $3
set BIASED = $4
set EXP = $5

set DIR = "exp-$EXP.$BIASED.$SEEDS.$TREES.$COMPS.dat"
mkdir -p $DIR
rm -f $DIR/rds-trees
touch -f $DIR/rds-trees

foreach i (1 2 3 4 5 6 7 8 9 10)
    ./run-fast.sh $*
    cat $DIR/*.pearson2 >> $DIR/rds-trees
end

 ./summary < $DIR/rds-trees > $DIR/rds-stats

cat $DIR/rds-stats
