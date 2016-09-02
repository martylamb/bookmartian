# powershell script to launch the bookmartian server

mvn "exec:java" "-Dexec.mainClass=com.martiansoftware.bookmartian.App" "-Dexec.classpathScope=compile" "-Dexec.args=bookmarks.json --root C:/Users/john5/source/Repos/bookmartian/src/main/resources/static-content"