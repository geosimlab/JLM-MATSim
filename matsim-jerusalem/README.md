# JLM-MATSim
This is The GeoSimLab Tel Aviv University Jersulam MATSim project. For more info contact:

* Ido klein idshklein@gmail.com
* Golan Ben-Dor golanben@mail.tau.ac.il
* Itzhak Benenson bennya@tauex.tau.ac.il

## Getting started
### Downloading necessary software
In order to run JLM-MATSim model, a few softwares are required:

1. Java jdk 11 [(Download)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
2. DELETE Eclipse for Java developers [(Download)](https://www.eclipse.org/downloads/packages/release/kepler/sr1/eclipse-ide-java-developers)
2a. maybe eclipse [(Download)](https://www.eclipse.org/downloads/)
3. PostgreSQL 10 [(Download)](https://www.postgresql.org/download/windows/)
4. PostGIS. PostGIS is available to download and install through PostgreSQL stack builder executable (you will be prompted to use of at the end of PostgreSQL installation ----> categories ----> Spatial Extensions ----> PostGIS 3.0). More instructions [here](https://postgis.net/install/). This project uses PostGIS 3.
5. OSGeo4W [(Download)](https://trac.osgeo.org/osgeo4w/)

### Download project data
This project is using data from Jerusalem Master Plan Team Activity Based model results and Survey of Israel. Due to data licensing issues, data is not presented. For more information, contact the developers. 

### Setting up  
1. [Add java to path](https://www.java.com/en/download/help/path.xml) in order to run eclipse. a restart might be necessary. NOT SURE, CHECK IN NEXT SETUP
2. Open a GitHub account. 
3. Use git in eclipse to clone JLM-MATSim: https://github.com/geosimlab/JLM-MATSim.git as such: file->import->git->projects from git->clone URI. It will go through a sequence of windows; it is important that you import as 'general project'.
4. Open the project explorer, right click project -> Configure -> Configure and detect nested folders -> choose the matsim-jerusalem folder. 
  * there might be problems with the maven build. right click pom.xml -> run as -> maven install. then, right click pom.xml-> maven -> update project.

5. Right click on project -> build path -> configure build path -> libaries (tab) -> JRE System library (choose) -> edit -> alternate JRE -> choose jdk.
6. conttact the developers for the input files.
7. After the project is cloned, create a copy of mock.properties . Call the newly added file database.properties.
8. Edit the UPPERCASE values in database.properties as instructed (make sure that none of your folders include a space characther).
9. run dbinitialize.java
