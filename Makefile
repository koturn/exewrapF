JAVAC      = javac
JAR        = jar
EXEWRAP    = other/exewrap.exe

ENCODE     = utf-8
SRCDIR     = src
DISTDIR    = bin
SRCVERSION = 1.7
BINVERSION = 1.7

JFLAGS     = -encoding $(ENCODE) -source $(SRCVERSION) -target $(BINVERSION)
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
	$(EXEWRAP) $(EWFLAGS) -j $< -o $@

$(JARFILE) : $(DISTDIR)/$(MAIN_CLASS).class
	$(JAR) cvmf $(MFFILE) $@ -C $(dir $<) . resource

$(DISTDIR)/$(MAIN_CLASS).class : $(SRCS)
	@ls $(dir $@) >/dev/null 2>&1 || mkdir $(dir $@)
	$(JAVAC) $(MAIN_SRC) $(JFLAGS) -sourcepath $(SRCDIR) -d $(dir $@)


.PHONY : clean
clean :
	$(RM) $(DISTDIR)/*.class $(JARFILE) $(EXEFILE)
.PHONY : objclean
objclean :
	$(RM) $(DISTDIR)/*.class
