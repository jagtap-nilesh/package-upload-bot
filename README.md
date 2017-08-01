# package-upload-bot :: Salesforce package upload using Tooling API

Salesforce now allows to automate package upload process using tooling API's in two simple steps.

Step 1 : Establish connection with salesforce org
Step 2 : Start package upload and poll for upload status.

Before performing above steps, make sure you have added required JAR's to you java build path.

Step 1: Download the Web Service WSDL
•	Log in to your Salesforce account. Make sure you are a system administrator or have "Modify All Data permissions" user.
•	Click Your Name > Setup > Develop > API to display the WSDL download page.
•	Download Partner API and Tooling API WSDLs

Step 2: Download wsc.jar 
Download wsc.jar from http://code.google.com/p/sfdc-wsc/downloads/list 

Step 3: Generate Client-Side Java Code
java -classpath wsc-XX.jar com.sforce.ws.tools.wsdlc tooling.wsdl tooling.jar
java -classpath wsc-XX.jar com.sforce.ws.tools.wsdlc partner.wsdl partner.jar

Once you have all three JAR's (partner.jar, tooling.jar and wsc.jar) you are all set to automate your package upload process.




