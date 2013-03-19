HOW TO GENERATE DOCUMENTATION
-----------------------------

COMMANDS:

Documentation can be generated in both HTML and PDF formats.
Prior to anything, you should check you have the package 'python-sphinx' installed, and then simply set yourself inside this folder and execute the following commands:

- HTML: make html
- PDF: make latexpdf

HTML output will be located inside 'build/html', whereas pdf files will be moved right to this folder.


HOW TO CHANGE LANGUAGES:

Language can be changed inside the 'source/conf.py' file. There is a variable named 'language' which you can set to the preferred language code (e.g. en, es, fr, pt) and,
if you follow the same convention as the included rst files and folders, there'd be no problem compiling other languages. Just keep in mind the conf.py file includes
other variables that might be sensitive to the language, mainly 'today_fmt' (date format) and 'latex_documents' (latex files data, which includes the title among other values).
