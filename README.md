# fphps-web-example
SMARTCORE FASTPASS SDK JAVA API Library usage example web application
   - JAVA 17
   - Spring Boot 3.5.0
   - JNA 5.17.0
   - JNA Plateform 5.17.0


1. Add FASTPASS SDK bin directory(ex. C:\SMARTCORE\FASTpass\Bin64) to windows PATH env 
2. add fphps-1.0.0.jar dependency to gradle.build file (ex.
   implementation files("D:\\Workspaces\\java\\FPHPS\\lib\\build\\local-repository\\com\\smartcoreinc\\fphps\\1.0.0\\fphps-1.0.0.jar")
3. run npm install in the src/main/frontend directory
4. gradlew clean build in the project root path (ex. fhphps-web-examle\gradlew clean build)
5. run gradlew bootRun
