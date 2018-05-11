
SRC=Stats.java StatsECSTC.java StatsTrue.java\
VertexMapper.java VertexMapperTrue.java VertexMapperECSTC.java \
Measurement.java MeasurementBC.java MeasurementES.java MeasurementCON.java \
MeasurementHIER.java MeasurementDijkstra.java \
StatsVector.java StatsTrueVector.java StatsECSTCVector.java \
MeasurementVector.java \
RDS.java Completion.java Params.java \
EdgeSelector.java EdgeWeightAssigner.java EdgeWeightAssigner_Constant.java \
Randomness.java GraphUtils.java Log.java \
Analysis.java AnalysisCoverage.java AnalysisMisclassificationDegreeHeuristic.java \
AnalysisMisclassification.java AnalysisPearson.java AnalysisSlope.java AnalysisIntercept.java \
AnalysisWorstCaseError.java \
AnalysisStdMean.java AnalysisMeanStd.java AnalysisStdStd.java \
AnalysisWorstVertexMeanStd.java AnalysisStdVertexMeanStd.java \
Experimenter.java Main.java Run.java \
ErdosRenyi.java BarabasiAlbert.java RandomGraph.java 

LIB=lib/colt.jar:lib/jung-1.7.6.jar:lib/resolver.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/concurrent.jar:lib/jung-1.7.6-src.jar:lib/serializer.jar:lib/xercesSamples.jar:lib/commons-collections-3.2.jar:lib/commons-collections-testframework-3.2.jar

all:	
	javac -d ./classes -classpath $(LIB) $(SRC)

# Usage: <filename> <numSeeds> <numRDStrees> <numCompletions> <Experiment> <NormGraph> <normCompletion> <Symmetrize> <rndSeed>

help:
	echo "java -cp ./classes:$(LIB) ecstc.Main "

dyads:
	java -cp ./classes:$(LIB) ecstc.Main ./data/dyads.txt 20 20 20 BC 0 0 1

cycle:
	java -cp ./classes:$(LIB) ecstc.Main ./data/cycle.txt 2 10 10 BC 0 0 1

cycle2:	
	java -cp ./classes:$(LIB) ecstc.Main ./data/cycle.txt 2 100 100 BC 0 0 1

er100:
	java -cp ./classes:$(LIB) ecstc.ErdosRenyi 100 0.1 ./data/test 0

ba100:
	java -cp ./classes:$(LIB) ecstc.BarabasiAlbert 100 1 5 0.0 ./data/test 0

tidy:
	rm *~;

clean:
	rm -rf ./classes/*;
