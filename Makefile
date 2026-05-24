JAVAC = javac
JAVA  = java
SRC   = $(shell find src -name '*.java')
OUT   = out
JAR   = normalhttp.jar

.PHONY: all compile jar gen test clean

all: jar

compile:
	mkdir -p $(OUT)
	$(JAVAC) -d $(OUT) $(SRC)

jar: compile
	echo "Main-Class: normalhttp.Main" > $(OUT)/manifest.txt
	jar cfm $(JAR) $(OUT)/manifest.txt -C $(OUT) .

gen: jar
	mkdir -p generated/src
	$(JAVA) -jar $(JAR) generate --examples examples/ --out generated/src/

test: gen
	mkdir -p generated/out
	$(JAVAC) -d generated/out generated/src/GeneratedServer.java
	$(JAVA) -cp generated/out GeneratedServer &
	sleep 1
	$(JAVAC) -d generated/out generated/src/GeneratedClient.java
	$(JAVA) -cp generated/out GeneratedClient http://localhost:8080
	kill $$(lsof -ti:8080) 2>/dev/null || true

clean:
	rm -rf $(OUT) $(JAR) generated/
