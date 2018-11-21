# FLExText Modules for the Pepper conversion framework for linguistic data

[![Build Status](https://travis-ci.org/sdruskat/pepperModules-FLExModules.svg?branch=develop)](https://travis-ci.org/sdruskat/pepperModules-FLExModules) [![Coverage Status](https://coveralls.io/repos/github/sdruskat/pepperModules-FLExModules/badge.svg?branch=develop)](https://coveralls.io/github/sdruskat/pepperModules-FLExModules?branch=develop) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1492292.svg)](https://doi.org/10.5281/zenodo.1492292) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.corpus-tools/pepperModules-FLExModules/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.corpus-tools/pepperModules-FLExModules)

## How to cite

If you publish research for which this software has been used, you are required
to cite the software. The respective metadata can be found in the file
[CITATION.cff](CITATION.cff).

## General information


[Pepper](http://corpus-tools.org/pepper) is a conversion framework for linguistic data. 
*pepperModules-FLExModules* is a plugin for *Pepper* and provides an
importer for **FLEx XML**, i.e., the XML
export format from 
[SIL Fieldworks Language Explorer](https://software.sil.org/fieldworks/). 
The format
is used frequently for persisting language documentation data.

With the *pepperModules-FLExModules* importer, the data stored in FLEx XML 
interlinear text files can be transferred to another format. This way, the data 
can be re-used for other
purposes (such as adding different annotation types), or visualized and analyzed,
e.g., in [ANNIS](http://corpus-tools.org/annis), a search and visualization 
platform for linguistic data. For a list of available format converters for Pepper,
see the [list of known Pepper modules](http://corpus-tools.org/pepper/knownModules.html).

## Context

The development of pepperModules-ToolboxTextModules has been initiated in the 
[MelaTAMP research project](https://hu.berlin/melatamp).

## Requirements

`Pepper >= 3.1.1-SNAPSHOT`

## Usage

- Create a [Pepper workflow 
file](http://corpus-tools.org/pepper/userGuide.html#workflow_file) for the 
conversion, with the importer set to `FLExImporter`. Configure #properties as
needed.
- [Download Pepper](http://corpus-tools.org/pepper/), and run it with the 
workflow file.

## Importer

### Requirements, assumptions, behaviour

#### Annotation mapping

FLEx XML has features that necessitate a certain importer behaviour with regard
to annotation namespace and names.

In *Salt*, the data model onto which data is mapped during import, annotations
can have a `namespace`, and a `name`. In *FLEx XML*, one and the same annotation
name, i.e., the `'type'` of an `<item>` can be used on different *levels*, i.e.,
`<phrase>`, `<word>` or `<morph>`, etc. Additionally, an `<item>` also has a
`'lang'`, so 3 attributes in *FLEx XML* (*level*, *'lang'*, *'item'*) must be 
mapped onto 2 attributes in *Salt* annotations.

To preserve the *level* information of annotation during conversion, the
*FLExImporter* maps it by adding the container (node/edge) of the annotation
to a layer with the name of the level, i.e., `phrase`, `word`, and `morph`.
Annotations on the document (FLEx level `interlinear-text`) are being made
on the Salt document (`SDocument`), which itself cannot be added to a layer -
the layer is a node in an `SDocument`'s graph. Instead, all annotations on the
document itself can be assumed to belong the `interlinear-text` level.

At the same time, the *'lang'* information is recorded in the namespace of the
*Salt* annotation.

Therefore, if clients such as exporters need to re-combine this information, 
they need to retrieve language information from the namespace, and type 
information from the name of the annotation, and the *level* of the annotation
from the *layer name* of the layer included in the set of layers which the 
container of the annotation is a part of, or the information whether an 
annotation is attached to an `SDocument`. The importer will create exactly one
layer for each level, which will be named `phrase`, `word`, `morph` (according 
to the XML schema XSD file supplied by SIL, paragraphs cannot have annotations).


### Properties

| Property | Description | Example |
|----------|-------------|---------|
|`languageMap`| A map with original 'lang' strings and the target strings the original should be changed to during conversion. | `<property key="languageMap">ENGLISH=en,NORTH-AMBRYM=mmg</property>`|
|`typeMap`| A map with original 'type' strings and the target strings the original should be changed to during conversion. | `<property key="typeMap">txt=tx,gls=ge</property>`|


## One document per file

As *FLExText* files can contain `n` documents (corresponding to the XML element `interlinear-text`).
However, files with more than one `interlinear-text` element cannot currently
be processed by the FLExImporter.
