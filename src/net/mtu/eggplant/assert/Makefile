JAVA=java -green
JAVAC=jikes

%.class : %.java
	$(JAVAC) $<

all: AssertLexer.class \
	JavaTreeParser.class \
	JavaTreeParserTokenTypes.class \
	JavaTreeParserTokenTypes.txt \
	JavaLexer.class \
	JavaRecognizer.class \
	JavaTokenTypes.class \
	JavaTokenTypes.txt \
	Main.class

#assert2.g:	JavaTokenTypes.txt
#
#AssertHelperLexer.java: assert2.g
#	 java antlr.Tool assert2.g
#
assert.g:	JavaTokenTypes.txt

AssertLexer.java: assert.g 
	$(JAVA) antlr.Tool assert.g

#AssertTokenTypes.java: assert.g
#	 java antlr.Tool assert.g
#
#AssertTokenTypes.txt: assert.g
#	 java antlr.Tool assert.g
#
JavaLexer.java: java.g
	$(JAVA) antlr.Tool java.g

JavaRecognizer.java: java.g
	$(JAVA) antlr.Tool java.g

JavaTokenTypes.java: java.g
	$(JAVA) antlr.Tool java.g

JavaTokenTypes.txt: java.g
	$(JAVA) antlr.Tool java.g

JavaTreeParser.java: java.tree.g
	$(JAVA) antlr.Tool java.tree.g

JavaTreeParserTokenTypes.java: java.tree.g 
	$(JAVA) antlr.Tool java.tree.g

JavaTreeParserTokenTypes.txt: java.tree.g
	$(JAVA) antlr.Tool java.tree.g

Main.class: JavaTreeParser.class JavaRecognizer.class JavaLexer.class AssertLexer.class #AssertHelperLexer.class

clean:
	rm -f AssertLexer.* AssertTokenTypes.* JavaLexer.* JavaRecognizer.* JavaTokenTypes.* JavaTreeParser.* JavaTreeParserTokenTypes.*