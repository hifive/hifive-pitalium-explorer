java -jar "../../lib/sqltool-2.3.2.jar" --rcfile "sqltool.rc" \
  --sql "INSERT INTO Config VALUES ('absolutePath', '$PWD'); COMMIT;" pitalium-explorer
