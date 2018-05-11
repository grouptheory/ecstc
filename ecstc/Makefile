
SRC=RDS.java Test.java GraphUtils.java Randomness.java Log.java EdgeSelector.java EdgeWeightAssigner.java EdgeWeightAssigner_Constant.java Completion.java Measurable.java MeasurableBC.java Stats.java

LIB=lib/colt.jar:lib/jung-1.7.6.jar:lib/resolver.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/concurrent.jar:lib/jung-1.7.6-src.jar:lib/serializer.jar:lib/xercesSamples.jar:lib/commons-collections-3.2.jar:lib/commons-collections-testframework-3.2.jar

all:	
	javac -d ./classes -classpath $(LIB) $(SRC)

# Usage: <filename> <numSeeds> <numRDStrees> <numCompletions>

dyads:
	java -cp ./classes:$(LIB) ecstc.Test ./data/dyads.txt 20 20 20 BC

cycle:
	java -cp ./classes:$(LIB) ecstc.Test ./data/cycle.txt 2 10 10 BC

cycle2:
	java -cp ./classes:$(LIB) ecstc.Test ./data/cycle.txt 2 100 100 BC

tidy:
	rm *~;

clean:
	rm -rf ./classes/*;
