DIR := ${CURDIR}
all:
	@echo "See Makefile for possible targets!"

emconspy/PPReplaceParser.jar:
	@echo "Compiling JAVA wrapper..."  # TODO Put build in setup.py
	javac -sourcepath ./emconspy/ -cp ./emconspy/BerkeleyProdParser.jar:. ./emconspy/hu/u_szeged/cons/*.java
	jar -cvfe emconspy/PPReplaceParser.jar hu.u_szeged.cons.PPReplaceParser \
        -C ./emconspy hu/u_szeged/cons/PPReplaceModel.class -C ./emconspy hu/u_szeged/cons/PPReplaceParser.class
	rm -rf emconspy/hu/u_szeged/cons/*.class

compile: emconspy/PPReplaceParser.jar

dist/*.whl dist/*.tar.gz: emconspy/PPReplaceParser.jar
	@echo "Building package..."
	python3 setup.py sdist bdist_wheel

build: dist/*.whl dist/*.tar.gz

install-user: build
	@echo "Installing package to user..."
	pip3 install dist/*.whl

test:
	@echo "Running tests..."
	cd /tmp && python3 -m emconspy -i $(DIR)/tests/parse_kutya.in | diff - $(DIR)/tests/parse_kutya.out && cd ${CURDIR}

install-user-test: install-user test
	@echo "The test was completed successfully!"

ci-test: install-user-test

uninstall:
	@echo "Uninstalling..."
	pip3 uninstall -y emconspy

install-user-test-uninstall: install-user-test uninstall

clean:
	rm -rf dist/ build/ emconspy.egg-info/
	rm -rf include emconspy/hu/u_szeged/cons/PPReplaceModel.class emconspy/hu/u_szeged/cons/PPReplaceParser.class \
        emconspy/PPReplaceParser.jar

clean-build: clean build
