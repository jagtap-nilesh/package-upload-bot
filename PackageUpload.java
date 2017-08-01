
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.SoapConnection;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class PackageUpload {
	static final String USERNAME = "<YOUR_SALESFORCE_USERNAME>";
	static final String PASSWORD = "<PASSWORD+SECURITY_TOKEN>";	
	static private final String packageId = "<PACAKGE_ID>";
	static private final Integer packageMajorVersionNumber = 1;  	//Major Version
	static private final Integer packageMinorVersionNumber = 3;		//Minor Version
	static private final Boolean isReleaseVersion = true;			//If Beta package specify this as false
	static private final String packageVersionDescriptionSuffix = isReleaseVersion ? ""	: "beta";
	static private final String packageVersionDescription = "<YOUR_PACKAGE_NAME>"
			+ packageMinorVersionNumber + packageVersionDescriptionSuffix;
	static private final String packageDescription = String.format("Demo Package uplaoded using Tooling API !! ");
	static private final String packageReleaseNotesUrl = "https://salesforce.com";  //Link to release notes
	static private final String packagePostInstallUrl = "https://salesforce.com";	//Link to post install notes
	// Leave blank or null for no password
	static private final String packagePassword = "";
	static private final String baseUrl = "http://ap2.salesforce.com/";
	
	static PackageUploadRequest packageUploadRequestObj = new PackageUploadRequest();
	static SoapConnection connection;
	
	public static void main(String[] args) {

		ConnectorConfig config = new ConnectorConfig();
		config.setUsername(USERNAME);
		config.setPassword(PASSWORD);
		config.setTraceMessage(true);

		try {

			ConnectorConfig partnerConfig = new ConnectorConfig();
			partnerConfig.setManualLogin(true);
			PartnerConnection partnerConnection = com.sforce.soap.partner.Connector
					.newConnection(partnerConfig);
			LoginResult lr = partnerConnection.login(USERNAME, PASSWORD);

			ConnectorConfig toolingConfig = new ConnectorConfig();
			toolingConfig.setSessionId(lr.getSessionId());
			toolingConfig.setServiceEndpoint(lr.getServerUrl()
					.replace('u', 'T'));

			connection = com.sforce.soap.tooling.Connector
					.newConnection(toolingConfig);
			
			uploadPackage();
			uploadStatusPolling();

		} catch (ConnectionException e1) {
			e1.printStackTrace();
		}

	}

	public static void uploadPackage() throws ConnectionException {
		
		//Set package upload request properties
		packageUploadRequestObj.setMetadataPackageId(packageId);
		packageUploadRequestObj.setVersionName(packageVersionDescription);
		packageUploadRequestObj.setDescription(packageDescription);
		packageUploadRequestObj.setMajorVersion(packageMajorVersionNumber);
		packageUploadRequestObj.setMinorVersion(packageMinorVersionNumber);
		packageUploadRequestObj.setPostInstallUrl(packagePostInstallUrl);
		packageUploadRequestObj.setReleaseNotesUrl(packageReleaseNotesUrl);
		packageUploadRequestObj.setIsReleaseVersion(isReleaseVersion);
		packageUploadRequestObj.setPassword(packagePassword);
		
		SaveResult[] saveResults = connection
				.create(new SObject[] { packageUploadRequestObj });

		if (saveResults[0].isSuccess()) {
			// The save result contains the ID of the created request. Save it
			// in the local request.
			packageUploadRequestObj.setId(saveResults[0].getId());
			System.out.println("PackagePushRequest created, ID: "
					+ saveResults[0].getId());
		} else {
			for (com.sforce.soap.tooling.Error error : saveResults[0]
					.getErrors()) {
				System.out.println("Exception :: " + error.getMessage());
			}
		}
	}

	public static void uploadStatusPolling() throws ConnectionException {
		// Finds the status of the PackageUploadRequest for a given Id
		String query = String
				.format("Select status,MetadataPackageVersionId from PackageUploadRequest where Id = '%s'",
						packageUploadRequestObj.getId());

		boolean toggle = false;
		boolean done = false;
		while (true) {
			QueryResult queryResult = connection.query(query);

			PackageUploadRequest updatedPackageUploadRequest = (PackageUploadRequest) queryResult
					.getRecords()[0];

			String status = updatedPackageUploadRequest.getStatus();
			switch (status) {
			case "Success":
				System.out.println(String.format("Package upload %s completed",
						packageUploadRequestObj.getId()));
				System.out
						.println(String
								.format("Package install url: %s/packaging/installPackage.apexp?p0=%s",
										baseUrl, updatedPackageUploadRequest
												.getMetadataPackageVersionId()));
				done = true;
				break;
			case "Error":
				PackageUploadErrors errors = updatedPackageUploadRequest
						.getErrors();

				if (errors.getErrors().length == 0) {
					System.out
							.println(String
									.format("%s: For upload of package %s, no further information available",
											updatedPackageUploadRequest
													.getStatus(),
											packageUploadRequestObj.getId()));
				} else {
					System.out.println(String.format(
							"%s: For upload of package %s",
							updatedPackageUploadRequest.getStatus(),
							packageUploadRequestObj.getId()));
					for (PackageUploadError error : errors.getErrors()) {
						System.out.println("Error detail: "
								+ error.getMessage());
					}
				}
				// assertTrue("Upload failure occurred", false);
				break;
			case "InProgress":
				if (!toggle) {
					System.out.println(String.format(
							"Package upload %s started",
							packageUploadRequestObj.getId()));
					toggle = true;
				}
				break;
			case "Unknown":
				System.out.println("Unexpected package upload status: "
						+ updatedPackageUploadRequest.getStatus());
			}

			if (done)
				break;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore interruptions
			}
		}
	}
}
