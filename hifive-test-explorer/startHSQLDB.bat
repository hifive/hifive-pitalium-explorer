@echo Must set the JAVA_HOME environment variable to point to the JRE or JDK.
java -cp lib/hsqldb-2.3.2.jar org.hsqldb.Server --props src/main/resources/appConf/hsqldb.properties
