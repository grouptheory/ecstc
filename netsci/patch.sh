#!/bin/csh -f
set F = `ls *.png`
gnuplot < gnuplot.script
pngtopnm output.png > output.pnm
pnmtops output.pnm > output.ps
mv output.png $F
cp $F ~/Desktop/RDS
