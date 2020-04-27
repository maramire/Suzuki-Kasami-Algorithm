JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  Process.java \
		  ProcessInterface.java \
		  Token.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

