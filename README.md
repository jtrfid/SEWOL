SEWOL: Security-oriented Workflow Lib
=====================================
Java Library for workflow handling
----------------------------------

### About

The Security-oriented Workflow Library (SEWOL) provides support for the handling of workflow traces. It allows to specify the shape and content of process traces in terms of entries representing the execution of a specific workflow activity. SEWOL also allows to write these traces on disk. For this it uses a specific file writer for process logs. Currently it supports plain text, Petrify, MXML and XES log file types.

In order to specify security-related context information, SEWOL provides access control models such as access control lists (ACL) and role-based access control models (RBAC). All types of models can be conveniently edited with the help of appropriate dialogues.

### Library Dependencies

SEWOL builds upon and encloses the following tools:

* TOVAL, located at [https://github.com/GerdHolz/TOVAL](https://github.com/GerdHolz/TOVAL "TOVAL: Tom's Java Library")
* JAGAL, located at [https://github.com/iig-uni-freiburg/JAGAL](https://github.com/iig-uni-freiburg/JAGAL "JAGAL: Java Graph Library")
* OpenXES, located at [http://www.xes-standard.org/openxes/](http://www.xes-standard.org/openxes/ "OpenXES")
* Spex, located at [http://code.deckfour.org/Spex/](http://code.deckfour.org/Spex/ "Spex")
* Google Guava, located at [https://github.com/google/guava](https://github.com/google/guava "Google Guava")
* XStream, located at [http://xstream.codehaus.org/](http://xstream.codehaus.org/ "XStream")
* Jung 2, located at [http://jung.sourceforge.net/](http://jung.sourceforge.net/ "Jung 2")

### Documentation

A detailled documentation of SEWOL can be found under [http://doku.telematik.uni-freiburg.de/sewol](http://doku.telematik.uni-freiburg.de/sewol "http://doku.telematik.uni-freiburg.de/sewol").
