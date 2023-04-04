# XPathTest
Simple tool to test XPath expressions in Java

## Build and run
XPathTest can simply be built and run using Maven, e.g.:
```
mvn compile exec:java -Dexec.args="-n cmd1=http://www.clarin.eu/cmd/1 \ 
  -n cmdp=http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1659015263839 \
  -i /tmp/DeReKo-Bag_getrennt/BAGIT/data/Metadata/DeReKo.cmdi \
  -x /cmd1:CMD/cmd1:Components/cmdp:CollectionProfile/cmdp:GeneralInfo/cmdp:Descriptions/cmdp:Description \
  -t"
```

You can use `--help` to list all possible command line arguments
