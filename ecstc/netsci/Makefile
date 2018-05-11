
all:	summary
	javac -d ./classes -classpath lib/colt.jar:lib/jung-1.7.6.jar:lib/resolver.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/concurrent.jar:lib/jung-1.7.6-src.jar:lib/serializer.jar:lib/xercesSamples.jar:lib/commons-collections-3.2.jar:lib/commons-collections-testframework-3.2.jar RDS.java RDSBiased.java Test.java Completion.java CompletionBiased.java Measurable.java MeasurableBC.java MeasurableBCRaw.java MeasurableEffectiveSize.java MeasurableHierarchy.java MeasurableConstraint.java MeasurableAggregateConstraint.java MeasurableHubness.java MeasurableAuthority.java Stats.java

summary:	summary.c
	gcc summary.c -o summary -lm

run:
	java -cp ./classes:lib/colt.jar:lib/jung-1.7.6.jar:lib/resolver.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/concurrent.jar:lib/jung-1.7.6-src.jar:lib/serializer.jar:lib/xercesSamples.jar:lib/commons-collections-3.2.jar:lib/commons-collections-testframework-3.2.jar netsci.Test /home/bilal/dev/netsci/dyads.txt

demo:
	java -cp ./classes:lib/colt.jar:lib/jung-1.7.6.jar:lib/resolver.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/concurrent.jar:lib/jung-1.7.6-src.jar:lib/serializer.jar:lib/xercesSamples.jar:lib/commons-collections-3.2.jar:lib/commons-collections-testframework-3.2.jar samples.graph.GraphEditorDemo 5 5

tidy:
	rm *~;

clean:
	rm -rf ./classes/*;
