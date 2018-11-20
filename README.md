# FLExText Modules for the Pepper conversion framework for linguistic data

[![Build Status](https://travis-ci.org/sdruskat/pepperModules-FLExModules.svg?branch=develop)](https://travis-ci.org/sdruskat/pepperModules-FLExModules) [![Coverage Status](https://coveralls.io/repos/github/sdruskat/pepperModules-FLExModules/badge.svg?branch=develop)](https://coveralls.io/github/sdruskat/pepperModules-FLExModules?branch=develop)

## How to cite

TBA

## General information

TBA

## Context

TBA

## Requirements

TBA

## Usage

TBA

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