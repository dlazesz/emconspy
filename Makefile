all: test

test:
	python3 -m emconspy -i parse_test.xtsv

compile:
	javac -sourcepath ./emconspy/ -cp ./emconspy/BerkeleyProdParser.jar:. ./emconspy/hu/u_szeged/cons/*.java  # TODO Put build in setup.py

build: compile
	python3 setup.py sdist bdist_wheel

clean:
	rm -rf dist/ build/ emdeppy.egg-info/
	rm -rf include emconspy/hu/u_szeged/cons/PPReplaceModel.class emconspy/hu/u_szeged/cons/PPReplaceParser.class

clean-build: clean build
