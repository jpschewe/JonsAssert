CLASSPATH=${HOME}/projects/java/lib/antlr.jar:${JAVA_HOME}/jre/lib/rt.jar:/home/jpschewe/projects/java:${HOME}/projects/java/lib/java-getopt-1.0.7.jar
JAVA=java -classpath "${CLASSPATH}"
JAVAC=jikes -g +E -deprecation -classpath "${CLASSPATH}"
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