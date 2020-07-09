JC = javac
JFLAGS = -source 1.7 -target 1.7 -cp .:modules/joshedit/src/main/java:modules/joshedit/src/main/resources
OUTPUT_FILE = lateralgm.jar

%.class: %.java
	$(JC) $(JFLAGS) $*.java

JAVA_FILES = $(shell find org -name "*.java")
JAR_INC_FILES = $(shell find org -type f \( -not -wholename '*/.git/*' \) -a \( -not -name "*.java" \))
BASE_CLASSES = $(JAVA_FILES:.java=.class)

default: classes jar

classes: $(BASE_CLASSES)

clean:
	find org/lateralgm -name "*.class" -exec rm {} \;
	rm -f $(OUTPUT_FILE)

jar: $(BASE_CLASSES)
	@echo JAR $(OUTPUT_FILE)
	@jar cfm $(OUTPUT_FILE) META-INF/MANIFEST.MF COPYING README.md LICENSE $(subst $$,\$$,$(JAR_INC_FILES))

.PHONY: clean jar default classes
