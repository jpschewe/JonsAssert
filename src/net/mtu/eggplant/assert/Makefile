JAVA=java -green -classpath "/home/jpschewe/lib/java/antlr.jar:/home/jpschewe/lib/java/rt.jar:/home/jpschewe/projects/java"
JAVAC=jikes -classpath "/home/jpschewe/lib/java/antlr.jar:/home/jpschewe/lib/java/rt.jar:/home/jpschewe/projects/java"

#%.class : %.java
#	$(JAVAC) $<

all:
	$(JAVA) antlr.Tool java.g
	$(JAVA) antlr.Tool assert.g
	$(JAVAC) *.java

clean:
	rm -f AssertLexer.* AssertTokenTypes.* JavaLexer.* JavaRecognizer.* JavaTokenTypes.* *.class