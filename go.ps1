# powershell script to compile and launch the bookmartian server

mvn clean package

java -jar target/bookmartian-0.1.0-SNAPSHOT-jar-with-dependencies.jar bookmarks.json