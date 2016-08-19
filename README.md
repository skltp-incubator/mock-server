# mock-server

Build
======
Build with:
> gradlew clean build

To create a standalone jar-file (for spring-boot) run
> gradlew clean build bootRepackage

Deploy
=======
Copy jar-file into dedicated directory
Use environment variable or system variable BASEPATH to define base path
Create a new directory or symbolic link called ${BASEPATH}/log
Start application for example (in linux)

  nohup java -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null &