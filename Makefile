JAVAC      = javac
JAR        = jar
EXEWRAP    = other/exewrap.exe

ENCODE     = utf-8
SRCDIR     = src
DISTDIR    = bin
SRCVERSION = 1.7
BINVERSION = 1.7

JFLAGS     = -sourcepath $(SRCDIR) -encoding $(ENCODE) -d $(DISTDIR) -source $(SRCVERSION) -target $(BINVERSION)
EWFLAGS    = -d "exewrapのフロントエンド" -e "SINGLE;NOLOG" -g -j ${jar.name} -o ${exe.name} -t 1.6 -v 1.9.8.2

MAIN_SRC   = $(SRCDIR)/exewrapFrontend.java
SRCS       = $(MAIN_SRC) $(SRCDIR)/exewrapFrontend.java $(SRCDIR)/AppState.java
MAIN_CLASS = exewrapFrontend
EXEFILE    = exewrapF.exe
JARFILE    = exewrapF.jar
MFFILE     = other/MANIFEST.MF


.PHONY : all
all : $(EXEFILE)

$(EXEFILE) : $(JARFILE)
	$(EXEWRAP) $(EWFLAGS) -j $(JARFILE) -o $(EXEFILE)

$(JARFILE) : $(DISTDIR)/$(MAIN_CLASS).class
	$(JAR) cvmf $(MFFILE) $(JARFILE) -C $(DISTDIR) . resource

$(DISTDIR)/$(MAIN_CLASS).class : $(SRCS)
	@if [ ! -d $(dir $@) ];  \
	  then mkdir $(dir $@);  \
	fi
	$(JAVAC) $(JFLAGS) $(MAIN_SRC)


.PHONY : clean
clean :
	$(RM) $(DISTDIR)/*.class $(JARFILE) $(EXEFILE)
.PHONY : objclean
objclean :
	$(RM) $(DISTDIR)/*.class
