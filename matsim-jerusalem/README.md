#JLM-MATSim
some text about the project

##Getting started
###Downloading and installing java and eclipse
Golan, I need help here

###Downloading and installing MATSim
Also here

###Downloading and installing external software
In order to work properly, JLM-MATSim requires three external softwares: 
1. PostgreSQL. [This](https://www.2ndquadrant.com/en/blog/pginstaller-install-postgresql/) is a good guide for downloading, installing and setting up PostgreSQL. It is important to write down the installation directory, username, password and port. This project uses PostgreSQL 10.
2. PostGIS. PostGIS is available to download and install through PostgreSQL stack builder executable. More instructions [here](https://postgis.net/install/). This project uses PostGIS 3. 
3. OSGeo4W64, downloadable from [here](https://trac.osgeo.org/osgeo4w/). It is needed for the ogr2ogr binary file.

###Data
in order to get initialize the database, a few files are required:
1. Trips - JTMT
2. Persons - JTMT
3. Households - JTMT
4. Bental GDB - MAPI
5. TAZ shape - JTMT
6. Nodes - JTMT
7. Links - JTMT
8. Line_path - JTMT
9. Lines - JTMT
10. Headway - JTMT

###mock.properties
mock.properties contains all properties to be defined. All uppercase should be replaced in order that the model will run. after this is done, change file name to "database.properties".