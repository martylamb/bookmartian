# powershell script to launch the bookmartian server

mvn 'exec:java' '-Dexec.mainClass="com.martiansoftware.bookmartian.App"' '-Dexec.classpathScope="compile"' '-Dexec.args="bookmarks.json --root src/main/resources/static-content"'