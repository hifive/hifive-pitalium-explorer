@echo Must set the JAVA_HOME environment variable to point to the JRE or JDK.
java -jar lib/sqltool-2.3.2.jar --rcfile src/main/resources/appConf/sqltool.rc --sql SHUTDOWN; testexplorer
