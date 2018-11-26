
  # Tyrion #
  
  Back-end server for Becki, managing **Code,** **Blocko** and **Grid.** Server is used for interaction with **PostgreSQL** DB.
  
  Server also keeps user information and does the billing.
  
  This server knows all wanted states that **Hardware** or **Instances** should be in and gives directives if it is needed.
  
  ### DEV mode ###
  #### Prerequisites ####
  
  - [Java JDK or SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  - [Java Cryptography Extension - Unlimited Strength](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) 
  - [Play! Framework 2.4 (activator minimal)](https://playframework.com/documentation/2.4.x/Installing)
  - [PostgreSQL](https://www.postgresql.org/download/)
  
  #### Setup ####
  
  - Install *Java* and *JCE - Unlimited Strength* policy files (only replace files in your_java/lib/security folder)
  - (You might have to add location of java.exe and javac.exe to JAVA_HOME or PATH variable)
  - Install PostgreSQL and create password "admin" for superuser "postgres" and create DB "byzance"
  - In root of the application run terminal command "activator run" (might have to add path of your activator/bin to PATH variable)
  
  #### Tips ####
  
  - Command "activator clean" removes the compiled application. (can be helpful if the application is somehow badly compiled)
  - For building the production version, use command "activator dist"
  
---------------------------------------------------------------------------------------------------------------------------------------
  
  ####  DOCU on GIT DOCU #### 
 
    https://github.com/ByzanceIoT/tyrion/wiki
    
// Create DIR 
 
    mkdir mongodb-charts
    cd mongodb-charts
 
// Stažení souboru
    
    wget "https://webassets.mongodb.com/com-download-center/charts/charts-docker-compose-v0.10.0.yml"   
    
// Enable docker file    
    
    sudo docker swarm init    
    
    docker pull quay.io/mongodb/charts:v0.10.0
    
    docker run --rm quay.io/mongodb/charts:v0.10.0 charts-cli test-connection mongodb+srv://chart:PracujVic@byzancecluster-4qtus.gcp.mongodb.net/admin

    echo "mongodb+srv://chart:PracujVic@byzancecluster-4qtus.gcp.mongodb.net/admin" | docker secret create charts-mongodb-uri -
 
    
# Mongo Chats loig: docker run --rm quay.io/mongodb/charts:v0.10.0 charts-cli test-connection mongodb+srv://tyrion:UGoYBIZUwUhvth0q@byzancecluster-4qtus.gcp.mongodb.net/admin
##echo "docker run --rm quay.io/mongodb/charts:v0.10.0 charts-cli test-connection mongodb+srv://tyrion:UGoYBIZUwUhvth0q@byzancecluster-4qtus.gcp.mongodb.net/admin" | docker secret create charts-mongodb-uri -
    
    
    sudo docker stack deploy -c charts-docker-compose-v0.10.0.yml mongodb-charts
    
    sudo docker service ls

    sudo docker exec -it \
      $(docker container ls --filter name=_charts -q) \
      charts-cli add-user --first-name "Tomas" --last-name "Zaruba" \
      --email "tomas.zaruba@byzance.cz" --password "PracujVic" \
      --role "UserAdmin"
      
      
      docker exec -it \
        $(docker container ls --filter name=_charts -q) \
        charts-cli add-user --first-name "<First>" --last-name "<Last>" \
        --email "tomas.zaruba@byzance.cz" --password "PracujVic" \
        --role "UserAdmin"


// DB name

ac42202a-2363-414b-9eef-9b87dd304b2d

// Connection to charts success
mongodb+srv://Franta:PracujVic@byzancecluster-4qtus.gcp.mongodb.net/ac42202a-2363-414b-9eef-9b87dd304b2d


// Import Success
mongoimport --host byzancecluster-shard-00-00-4qtus.gcp.mongodb.net:27017,byzancecluster-shard-00-01-4qtus.gcp.mongodb.net:27017,byzancecluster-shard-00-02-4qtus.gcp.mongodb.net:27017 --ssl -u tyrion -p UGoYBIZUwUhvth0q --authenticationDatabase admin \
--db ac42202a-2363-414b-9eef-9b87dd304b2d --collection movieDetails \
--drop --file movieDetails.json
