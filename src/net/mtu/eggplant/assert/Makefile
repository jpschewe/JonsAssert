CLASSPATH=/home/jpschewe/lib/java/antlr.jar:${JAVA_HOME}/jre/lib/rt.jar:/home/jpschewe/projects/java:${HOME}/lib/java/gnu-regexp-1.0.8.jar:${HOME}/lib/java/java-getopt-1.0.7.jar
JAVA=java -classpath "${CLASSPATH}"
JAVAC=jikes +E -deprecation -classpath "${CLASSPATH}"

#%.class : %.java
#	$(JAVAC) $<

all:
	$(JAVA) antlr.Tool java.g
	$(JAVA) antlr.Tool assert.g
	$(JAVAC) *.java

clean:
	rm -f AssertLexer.* AssertTokenTypes.* JavaLexer.* JavaRecognizer.* JavaTokenTypes.* *.class