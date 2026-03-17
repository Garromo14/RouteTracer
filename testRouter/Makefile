# -*- Mode: Makefile -*-
#
# Makefile - Java version
#
# use: make
# or:  make test
#

JAVA = /usr/bin/java
JAVAC = /usr/bin/javac

all: RouterSimulator.class

RouterSimulator.class: RouterSimulator.java
	-@$(JAVAC) RouterSimulator.java


clean:
	-@touch ./abc~ core
	-@rm -f *~ core

clobber: clean
	-@touch ./abc.class
	-@rm -f *.class

test: RouterSimulator.class
	$(JAVA) -DTrace=3 RouterSimulator

test3nc: RouterSimulator.class
	$(JAVA) -DNodes=3 -DChange=false -DPoison=false RouterSimulator

test3np: RouterSimulator.class
	$(JAVA) -DNodes=3 -DChange=true -DPoison=false RouterSimulator

test3p: RouterSimulator.class
	$(JAVA) -DNodes=3 -DChange=true -DPoison=true RouterSimulator

test4nc: RouterSimulator.class
	$(JAVA) -DNodes=4 -DChange=false -DPoison=false RouterSimulator

test4np: RouterSimulator.class
	$(JAVA) -DNodes=4 -DChange=true -DPoison=false RouterSimulator

test4p: RouterSimulator.class
	$(JAVA) -DNodes=4 -DChange=true -DPoison=true RouterSimulator

test5nc: RouterSimulator.class
	$(JAVA) -DNodes=5 -DChange=false -DPoison=false RouterSimulator

test5np: RouterSimulator.class
	$(JAVA) -DNodes=5 -DChange=true -DPoison=false RouterSimulator

test5p: RouterSimulator.class
	$(JAVA) -DNodes=5 -DChange=true -DPoison=true RouterSimulator
