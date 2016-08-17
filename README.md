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
Create a new directory or symbolic link called "logs"
Start application for example (in linux)

  nohup java -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null &