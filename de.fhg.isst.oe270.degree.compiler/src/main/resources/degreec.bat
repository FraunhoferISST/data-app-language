echo off

set absolutePath=%~dp0
set compilerJar=de.fhg.isst.oe270.degree.compiler-1.8.1-SNAPSHOT.jar

java -javaagent:%absolutePath%%compilerJar% -jar %compilerJar% %*%