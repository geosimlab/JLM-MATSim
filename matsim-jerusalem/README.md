#JLM-MATSim
some text about the project

##Getting started
###Downloading necessary software
In order to run JLM-MATSim model, a few softwares are required:

1. Java jdk 11 [(Download)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
2. DELETE Eclipse for Java developers [(Download)](https://www.eclipse.org/downloads/packages/release/kepler/sr1/eclipse-ide-java-developers)
2a. maybe eclipse [(Download)](https://www.eclipse.org/downloads/)
3. PostgreSQL 10 [(Download)](https://www.postgresql.org/download/windows/)
4. PostGIS. PostGIS is available to download and install through PostgreSQL stack builder executable (you will be prompted to use of at the end of PostgreSQL installation). More instructions [here](https://postgis.net/install/). This project uses PostGIS 3.
5. OSGeo4W [(Download)](https://trac.osgeo.org/osgeo4w/)

###Download project data
This project is using data from Jerusalem Master Plan Team Activity Based model results and Survey of Israel. Due to data licensing issues, data is not presented. For more information, contact the developers. 

###Setting up  
0. [Add java to path](https://www.java.com/en/download/help/path.xml) in order to run eclipse. a restart might be necessary. NOT SURE, CHECK IN NEXT SETUP
0. Open a GitHub account. 
1. Use git in eclipse to clone JLM-MATSim: https://github.com/geosimlab/JLM-MATSim.git as such: file->import->git->projects from git->clone URI. It will go through a sequence of windows; it is important that you import as 'general project', go back and then forth, and coohe the matsim-jerusalem library. 
2. Open the project explorer, and connect Java jdk 11 to the project's build path(right click project name -> Build Path -> Configure Build Path -> choose Libraries tab -> TODO)
3. After project is cloned, copy the mock.properties file into the project. Call the newly added file database.properties.
4. Edit the UPPERCASE values as instructed. 
5. run dbinitialize.java 

