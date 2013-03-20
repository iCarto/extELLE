HOW TO GENERATE DOCUMENTATION
-----------------------------

COMMANDS:

Documentation can be generated in both HTML and PDF formats. Simply set yourself inside this folder and execute the following commands:

- HTML: make html
- PDF: make latexpdf

HTML output will be located inside 'build/html', whereas pdf files will be moved right to this folder.


HOW TO CHANGE BETWEEN EXISTING LANGUAGES:

Language can be changed inside the 'Makefile' file, by setting the variable 'LANGUAGE' to the desired language code (e.g. en, es, fr, pt). It can also be set in the command itself by adding a parameter like 'LANGUAGE=es' (e.g. make html LANGUAGE=es).


HOW TO ADD NEW LANGUAGES

All the language related configuration is done inside the 'source/conf.py' file. There is a variable named 'language' which you can set to the preferred language code (e.g. en, es, fr, pt) and,
if you follow the same convention as the included rst files and folders, there'd be no problem compiling other languages. Just keep in mind the conf.py file includes
other variables that might be sensitive to the language, mainly 'today_fmt' (date format) and 'latex_documents' (latex files data, which includes the title among other values).
