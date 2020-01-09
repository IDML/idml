#!/usr/bin/env python
from __future__ import print_function
import os
from glob import glob


def getsections():
    for filename in (filename for filename in glob("*.md") if filename != "index.md"):
        with open(filename, 'r') as f:
            yield (True, filename, "")
            for section in filter(lambda x:x.startswith("## "), f.readlines()):
                yield (False, filename, section.replace("## ", "").strip())

def generateindex(sections):
    yield "---"
    yield "layout: docsplus"
    yield "title: Functions"
    yield "section: language"
    yield "---"
    for (toggle, filename, section) in sections:
        if toggle:
            yield "## {}".format(filename.replace(".md", ""))
        else:
            yield "[{}]({})".format(section, "/functions/{}#{}".format(filename.replace(".md", ".html"), section.lower().replace(" ", "-").replace(",", "")))

if __name__=="__main__":
    print('\n\n'.join(list(generateindex(getsections()))))
