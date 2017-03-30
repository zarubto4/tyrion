
## Tyrion Stage Server - Create new one ## 

1) **Set Ubuntu server on Azure**

        You need Azure Global Admin Account. Now its a Lexa, Tomas, and David H. 
        
2) **Log in with ssh**
 
        ssh qwfasdsdfsdfsdgqw31@52.168.28.29
        
        # password #
        
3) **Install Git** (after login wait 2 minutes for Azure synchronization)

        sudo apt-get install git-all
        sudo apt-get install gedit
                
        ssh-keygen -t rsa -b 4096 -C "admin@byzance.cz" 
        (3x enter)
        
        
 7) **Install java**
 
         sudo apt-get install default-jre
                
4) **Copy Public SSH**         
   
        cat ~/.ssh/id_rsa.pub 
        
5) **Add public SSH key to Git** (Ask David if its working)
        
        Go to http://git.byzance.cz/admin/deploy_keys and create new key with public SSH string
   
        Go to project on Git - There is Deploy Key and set Deploy keys allowed read-only access to the repository
       
6) **Registr GIT on server**        
        
        ssh git@git.byzance.cz
        
        git clone git@git.byzance.cz:Tyrion/Tyrion-build.git

        
--------



## Tyrion Stage Server - Set Branche ## 

    cd Tyrion-build
    
    git pull fg5CCFQU2d3/Ut6EFN8SivyYZpgCMkxOukPDy50bualN6lAiobvQc4
    
    ./restart_tyrion 
    
    (Možná bude potřeba git reset --hard) 

Logs    
    
    tail -n 50 tyrion_run.log  //last 50 logs
    tail -f tyrion_run.log  // sleduje log dokud se nezmáčkne Ctrl+C