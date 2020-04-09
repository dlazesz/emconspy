all: test

test:
	python3 -m emconspy -i parse_test.xtsv

compile:
	javac -sourcepath ./emconspy/ -cp ./emconspy/BerkeleyProdParser.jar:. ./emconspy/hu/u_szeged/cons/*.java  # TODO Put build in setup.py
	jar -cvfe emconspy/PPReplaceParser.jar hu.u_szeged.cons.PPReplaceParser -C ./emconspy hu/u_szeged/cons/PPReplaceModel.class -C ./emconspy hu/u_szeged/cons/PPReplaceParser.class
	rm -rf emconspy/hu/u_szeged/cons/*.class

build: compile
	python3 setup.py sdist bdist_wheel

clean:
	rm -rf dist/ build/ emconspy.egg-info/
	rm -rf include emconspy/hu/u_szeged/cons/PPReplaceModel.class emconspy/hu/u_szeged/cons/PPReplaceParser.class

clean-build: clean build
