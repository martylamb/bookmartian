# powershell script to launch the bookmartian server
#
# Example usage:
#
# usage: . ./go.ps1 -d ./data/ -p 80

$env:BOOM_DEBUG=1
mvn "exec:java" "-Dexec.mainClass=com.martiansoftware.bookmartian.App" "-Dexec.classpathScope=compile" "-Dexec.args=$args"