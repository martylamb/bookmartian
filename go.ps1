# powershell script to launch the bookmartian server
$env:BOOM_DEBUG=1
mvn "exec:java" "-Dexec.mainClass=com.martiansoftware.bookmartian.App" "-Dexec.classpathScope=compile" "-Dexec.args=-d ."