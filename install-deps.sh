#!/bin/bash

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2.jar

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver-buildingSMARTLibrary -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2-buildingSMARTLibrary.jar

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver-client-lib -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2-client-lib.jar

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver-ifc -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2-ifc.jar

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver-ifcplugins -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2-ifcplugins.jar

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver-shared -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2-shared.jar

mvn install:install-file -DgroupId=org.bimserver -DartifactId=bimserver-utils -Dversion=1.2 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/bimserver-1.2-utils.jar

mvn install:install-file -DgroupId=org.eclipse.emf -DartifactId=codegen -Dversion=2.8.0.v20130125-0826 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/bim/bimserver/src/lib/org.eclipse.emf.codegen_2.8.0.v20130125-0826.jar


mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-clientapi -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/sharkclientapi.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-commonapi -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/sharkcommonapi.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-clientutilities-misc -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/client/sharkclientutilities-misc.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-utilities-misc -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/engine/sharkutilities-misc.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-utilities-wmentity -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/engine/sharkutilities-wmentity.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-ejbandws-client -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/client/sharkejbandws-client.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-adminapi -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/sharkadminapi.jar

mvn install:install-file -DgroupId=net.sourceforge.sharkwf -DartifactId=shark-internalapi -Dversion=4.4-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/sharkinternalapi.jar

mvn install:install-file -DgroupId=net.sourceforge.jxpdl -DartifactId=jxpdl -Dversion=1.3-1 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/shark/lib/jxpdl/jxpdl.jar

mvn install:install-file -DgroupId=net.sourceforge.supercsv -DartifactId=supercsv -Dversion=1.52 -Dpackaging=jar -Dfile=/Users/heidsoft/work/cloudcmdb/core/lib/SuperCSV-1.52.jar
