#!/bin/csh -f
set SEEDS = $1
set TREES = $2
set COMPS = $3
set BIASED = $4
set EXP = $5

if ($BIASED == "0") then
    set MODE = "Unbiased RDS/Completions"
endif
if ($BIASED == "1") then
    set MODE = "Biased RDS/Completions"
endif

#----------------------------------------
if ($EXP == "BC") then
    set MEASURE = "Betweenness Centrality"
endif
if ($EXP == "ES") then
    set MEASURE = "Effective Size"
endif
if ($EXP == "H") then
    set MEASURE = "Hubness"
endif
if ($EXP == "A") then
    set MEASURE = "Authority"
endif
if ($EXP == "CON") then
    set MEASURE = "Constraint"
endif

set TITLE = "Estimating $MEASURE w/ $MODE ($TREES trs x $COMPS cmps)"

java -cp ./classes:lib/colt.jar:lib/jung-1.7.6.jar:lib/resolver.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/concurrent.jar:lib/jung-1.7.6-src.jar:lib/serializer.jar:lib/xercesSamples.jar:lib/commons-collections-3.2.jar:lib/commons-collections-testframework-3.2.jar netsci.Test /home/bilal/dev/netsci/dyads.txt $SEEDS $TREES $COMPS $BIASED $EXP

set DIR = "exp-$EXP.$BIASED.$SEEDS.$TREES.$COMPS.dat"

mkdir -p $DIR

set PEARSON = `cat output.$SEEDS.$TREES.$COMPS.pearson`

mv "output.$SEEDS.$TREES.$COMPS.error" $DIR
mv "output.$SEEDS.$TREES.$COMPS.pearson" $DIR

echo "set terminal png" > "$DIR"/gnuplot.script
echo "set output "\""output.png"\""" >> "$DIR"/gnuplot.script
echo "set xlabel "\""Actual $MEASURE"\""" >> "$DIR"/gnuplot.script
echo "set ylabel "\""Estimated $MEASURE"\""" >> "$DIR"/gnuplot.script
echo "set title "\""$TITLE"\""" >> "$DIR"/gnuplot.script
echo "plot "\""output.$SEEDS.$TREES.$COMPS-final"\"" using 3:4 title "\""$PEARSON"\""" >> "$DIR"/gnuplot.script

foreach F (`ls output.$SEEDS.$TREES.$COMPS-*`) 
    cat $F | sort -k 1 -n > $F.sorted
    mv $F.sorted $F
    mv $F "$DIR"
end

cd "$DIR"
gnuplot < gnuplot.script

pngtopnm output.png > output.pnm
pnmtops output.pnm > output.ps
echo "Done"

mv output.png "$EXP-$BIASED-$SEEDS-$TREES-$COMPS.png"

evince output.ps
