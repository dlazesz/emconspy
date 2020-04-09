#!/usr/bin/env pyhton3
# -*- coding: utf-8, vim: expandtab:ts=4 -*-

import os

from xtsv import jnius_config, import_pyjnius


def get_java_mem():
    """return size of memory in MB for "java -Xmx"
    """
    currmem = os.sysconf('SC_PAGE_SIZE') * os.sysconf('SC_PHYS_PAGES') // (1024 ** 2)
    minmem = 6*1024
    maxmem = 4*minmem
    if currmem > maxmem + 4*1024:
        mem = maxmem
    elif currmem > minmem + 4*1024:
        mem = currmem - 4*1024
    else:
        mem = minmem
    return mem


class EmConsPy:
    class_path = os.path.join(os.path.dirname(__file__), 'BerkeleyProdParser.jar') + ':' + os.path.dirname(__file__)
    vm_opts = '-Xmx{0}m'.format(get_java_mem())
    pass_header = True

    def __init__(self, model_file=os.path.normpath(os.path.join(os.path.dirname(__file__), 'szk.const.pos_only.model')),
                 source_fields=None, target_fields=None):
        self._autoclass = import_pyjnius()
        self._jstr = self._autoclass('java.lang.String')
        self._jlist = self._autoclass('java.util.ArrayList')
        self._parser = self._autoclass('hu.u_szeged.cons.PPReplaceParser')
        self._parser.initReplaceParser(self._jstr(model_file.encode('UTF-8')), 4)
        # Field names for e-magyar TSV
        if source_fields is None:
            source_fields = {}

        if target_fields is None:
            target_fields = []

        self.source_fields = source_fields
        self.target_fields = target_fields

    def process_sentence(self, sen, field_names):
        parsed_sentence = self.parse_sentence((tok[field_names[0]], tok[field_names[1]], tok[field_names[2]])
                                              for tok in sen)
        for tok, out_label in zip(sen, parsed_sentence):
            tok.append(out_label)
        return sen

    @staticmethod
    def prepare_fields(field_names):
        return [field_names['form'], field_names['lemma'], field_names['xpostag']]

    def parse_sentence(self, lines):
        sent = self._jlist()

        # Read the text from TSV style input
        for curr_form, curr_lemma, curr_xpostag in lines:
            tok = self._jlist()
            tok.add(self._jstr(curr_form.encode('UTF-8')))
            tok.add(self._jstr(curr_lemma.encode('UTF-8')))
            tok.add(self._jstr(curr_xpostag.encode('UTF-8')))
            sent.add(tok)

        # Parse
        parsed_sentence = self._parser.parseSentenceEx(sent)

        # Return output as an iterator over tokens...
        return (tok[4] for tok in parsed_sentence)

    def parse_stream(self, stream):
        lines = []
        for line in stream:
            fields = line.strip().split('\t')
            if len(fields) == 0:
                for curr_line, label in zip(lines, self.parse_sentence(lines)):
                    yield '{0}\t{1}\n'.format(curr_line, label).encode('UTF-8')
                yield b'\n'
                lines = []
            else:
                lines.append(fields)
        if len(lines) > 0:
            for curr_line, label in zip(lines, self.parse_sentence(lines)):
                yield '{0}\t{1}\n'.format(curr_line, label).encode('UTF-8')


if not jnius_config.vm_running:
    jnius_config.add_classpath(EmConsPy.class_path)
    jnius_config.add_options(EmConsPy.vm_opts)
