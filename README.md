
# emConsPy
A wrapper implemented in Python for __emCons__ (Berkeley parser a.k.a. Product Parser).

__WARNING__: This wrapper is only compatible with [JAVA 11](https://askubuntu.com/questions/1037646/why-is-openjdk-10-packaged-as-openjdk-11/1037655#1037655) or higher!

## Requirements

  - _(Included in this repository)_ [Berkeley Parser](http://nlp.cs.berkeley.edu/software.shtml) compiled (stripped from [Magyarlánc 3.0](https://github.com/antaljanosbenjamin/magyarlanc/))
  - _(Included in this repository)_ A modified version of the Wrapper stripped from [Magyarlánc 3.0](https://github.com/antaljanosbenjamin/magyarlanc/)
  - _(Included in this repository)_ Modelfile for the parser (stripped from [e-magyar](https://github.com/dlt-rilmta/hunlp-GATE/))
  - Java JRE as in Aptfile (for building dependencies)
  - Python 3 (tested with 3.6)
  - Pip to install the additional requirements in requirements.txt

## Install on local machine

  - Install [git-lfs](https://git-lfs.github.com/)
  - `git-lfs install`
  - Clone the repository: `git clone https://github.com/dlt-rilmta/emconspy` (It should clone the model file also!)
  - ``sudo apt install `cat Aptfile` ``
  - `make build`
  - `sudo pip3 install dist/*.whl`
  - Use from Python

## Usage

  - From Python:

	```python
	>>> import emconspy
	>>> p = emconspy.EmconsPy('szk.const.model')
	>>> ex = 'A a [/Det|Art.Def]\n' \
             'kutya kutya [/N][Nom]\n' \
             'elment elmegy [/V][Prs.NDef.3Sg]\n' \
             'sétálni sétál [/V][Inf]\n' \
             '. . OTHER'
	>>> sentence = ex.split('\n')  # Like reading a file with open()
	>>> print(list(p.parse_sentence((t.split('\t') for t in sentence)))
	...
	>>> p.parse_stream(ex)  # Same as parse_sentence, but sentences are separated with empty lines (like CoNLL-* fomrat)
	...
	```

	`szk.cons.model` is the previously trained model file (eg. from Szeged Korpusz).

	`parse_sentence` takes one sentence as a list of tokens,
a token is a wsp-separated list of 3 fields:
string, lemma, hfstana.
It returns an iterator by tokens with one field:
cons.

	`parse_stream` Parses multiple sentences which are separated with newlines like in the CoNLL-* formats (uses `parse_sentence` internally)

## License

This Python and JAVA wrapper is licensed under the LGPL 3.0 license.
The model and the included jar file have their own licenses.
