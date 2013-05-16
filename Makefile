JC = ecj -1.6 -nowarn -cp .
JFLAGS = -cp /usr/share/java/eclipse-ecj.jar:/usr/share/java/ecj.jar
OUTPUT_FILE = lateralgm.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

JAVA_FILES = $(shell find org -name "*.java")
JAR_INC_FILES = $(shell find org -type f \( -not -wholename '*/.git/*' \) -a \( -not -name "*.java" \) | sed 's/\$$/\\\$$/g')

default: classes jar

classes: $(JAVA_FILES:.java=.class)

clean:
	find org/lateralgm -name "*.class" -exec rm {} \;
	rm -f $(OUTPUT_FILE)

jar:
	@jar cfm $(OUTPUT_FILE) META-INF/MANIFEST.MF COPYING README LICENSE $(JAR_INC_FILES)
