CLASSPATH=${HOME}/projects/assert/src:${HOME}/projects/assert/lib/JonsInfra-0.1.jar:${HOME}/projects/assert/lib/antlr.jar:${HOME}/projects/assert/lib/java-getopt-1.0.7.jar:${HOME}/projects/assert/lib/junit-3.6.jar:${JAVA_HOME}/jre/lib/rt.jar
JAVA=java -classpath "${CLASSPATH}"
JAVAC=javac -g -deprecation -classpath "${CLASSPATH}"
#JAVAC=javac -g -deprecation -classpath "${CLASSPATH}"

#%.class : %.java
#	$(JAVAC) $<

all:
	$(JAVA) antlr.Tool java.g
	$(JAVA) antlr.Tool assert.g
	$(JAVAC) *.java

debug: clean
	$(JAVA) antlr.Tool -debug java.g
	$(JAVA) antlr.Tool -debug assert.g
	$(JAVAC) *.java

clean:
	rm -f AssertLexer.* AssertTokenTypes.* JavaLexer.* JavaRecognizer.* JavaTokenTypes.* *.class