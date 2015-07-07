echo "Must set the JAVA_HOME environment variable to point to the JRE or JDK."

echo "execute this SQL file $1"
echo "start"

java -jar "../../lib/sqltool-2.3.2.jar" --rcfile "sqltool.rc" testexplorer "$1"

echo "end"
