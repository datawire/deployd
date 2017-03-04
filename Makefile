# Makefile: deployd

.PHONY: all

all: clean build

build:
	./gradlew test shadowJar

clean:
	./gradlew clean

compile:
	./gradlew build
 
test:
	./gradlew test
 
unit-test:
	./gradlew test

version:
	@echo SERVICE_VERSION

service-name:
	@echo SERVICE_NAME
