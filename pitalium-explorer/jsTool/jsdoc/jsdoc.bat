rmdir /s/q ..\..\doc\jsdoc
cd bin
jsdoc -r ..\..\..\src\main\webapp\src -d ..\..\..\doc\jsdoc
cd ..