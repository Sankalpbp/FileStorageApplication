# FileStorageApplication

This application is a cloud file storage system. It uses Google Cloud Storage to store the uploaded files and provides the ability to download, update, and delete the file details conveniently.


### Installation for Windows Operating System


#### Requirements
Please set the following before running the application: 

1. A running MySQL ( or any other SQL ) database on the local.
2. A running Google Cloud Storage account.
3. Create a new file: `gcs-configurations.json` in the `src/main/resources` directory of the project.
4. Paste the Google Cloud Storage configurations ( service account details and the API key ) which are provided while setting up the account, into the file created in the previous step.
5. Make sure Java 17 ( or higher ) is installed on the machine.

  

#### Running the application
The application is an Electron App, which makes creating the application in working condition for your specific Operating System very convenient. 
Follow these steps: 

1. Initialize the Database instance you're running and set the following details in the `file-storage-service/src/main/resources/application-dev2.properties` file which has the required properties to run the application on a new machine.

```
# Google Cloud Storage details

gcs.config.file = gcs-configurations.json
gcs.project.id = # your project id here
gcs.bucket.id = # your GCS bucket id here
gcs.dir.name = # the name of the directory in GCS where you want to store your files

# Database properties

spring.datasource.url = jdbc:mysql://localhost:PORT_NUMBER/file_metadata
spring.datasource.username = # the username of the root user
spring.datasource.password = # the password of the root user

```
In most cases, MySQL runs on `3306`, so replace the `PORT_NUMBER` in the `spring.datasource.url` attribute. But if you have configured it to run on some other port, make sure you put that there.

2. Avoid updating any other properties unless you are sure about what you are doing or if you have certain special configurations. 
3. This file contains the link to the DDL scripts which will be run at the startup of the application to make sure that the required database and tables are there to work on. This makes setting up easier without any extra effort. 

```
file-storage-service/src/main/resources/schema.sql
```

4. Open the `file-storage-frontend/` directory in the terminal.
5. Run 
```
npm install
```

This command will install all the required dependencies for the project.
6. Run 
```
npm run dist
```
This command will create a file in the `dist/` directory in the `file-storage-frontend/` directory where an application file will be created specific to your Operating System. 

##### For the Windows Operating System, a `.nsis` file will be created. 
##### For MacOS Operating System, a `.dmg` file will be created. 
##### For the Linux Operating System, an `AppImage` file will be created. 

These are installer files for each of the Operating Systems and a simple double-click would initiate the installation process and the app would be ready to run. 
These configurations can be updated in the `package.json` file. 


### Making updates

For making updates in the application and seeing them without updating the distribution file again and again, run 
```
electron .
```

from the `file-storage-frontend/` directory, this will spawn the application directly and will 
take any new updates that you will make just killing the current Electron process using `^C` and re-running the same command again. 




