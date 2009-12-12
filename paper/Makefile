#
# Modified from $Id: Makefile 161 2007-11-30 07:19:52Z balaji $ by Pavan Balaji.
#

TARGETS: paper.pdf
.PHONY: clean wordcount tags

tex_files = $(wildcard *.tex */*.tex */*/*.tex */*/*/*.tex)
bib_files = $(wildcard *.bib */*.bib */*/*.bib */*/*/*.bib)
pdf_files = $(wildcard */*.pdf */*/*.pdf */*/*/*.pdf)

all: paper.pdf

paper.pdf: $(tex_files) $(bib_files) $(pdf_files)
	pdflatex paper | tee latex.out
	bibtex paper; \
	pdflatex paper | tee latex.out; \
	if ! grep -q '^LaTeX Warning: .*undefined' latex.out; then break; fi; \
	bibtex paper; \
	pdflatex paper | tee latex.out; \
	if ! grep -q '^LaTeX Warning: .*undefined' latex.out; then break; fi; \
	bibtex paper; \
	pdflatex paper | tee latex.out; \
	if ! grep -q '^LaTeX Warning: .*undefined' latex.out; then break; fi; \
	rm latex.out

tags: $(tex_files) $(bib_files)
	find . \( -name '*.tex' -print \) -or \( -name '*.bib' -print \) | \
		xargs etags

wordcount: paper.pdf
	pdftotext paper.pdf - | wc -w 2> /dev/null || true

clean:
	find . \( -name '*.blg' -print \) -or \( -name '*.aux' -print \) -or \
		\( -name '*.bbl' -print \) -or \( -name '*~' -print \) -or \
		\( -name '*.lof' -print \) -or \( -name '*.lot' -print \) -or \
		\( -name '*.toc' -print \) | xargs rm -f; \
	rm -f paper.log paper.pdf

camera_ready: paper.pdf
	gs -dSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite \
	    -dCompatibilityLevel=1.4 -dPDFSETTINGS=/prepress          \
	    -dCompatibilityLevel=1.4               \
	    -dSubsetFonts=true -dEmbedAllFonts=true                  \
	    -sOutputFile=camera_ready.pdf -f paper.pdf
	@if  pdfinfo camera_ready.pdf | grep version | grep 1.4 > /dev/null ; \
	then \
	  if pdffonts camera_ready.pdf 2>&1 | grep -v Error | cut -c 56-62 | grep no > /dev/null ; \
	  then \
	    echo pdf failed test on fonts ; \
	  else \
	    echo pdf passed test, all fonts embedded and Acrobat 5 compatible ; \
	  fi \
	else \
	  echo pdf failed test on version ; \
	fi
