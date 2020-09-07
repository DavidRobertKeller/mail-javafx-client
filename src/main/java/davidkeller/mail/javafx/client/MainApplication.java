package davidkeller.mail.javafx.client;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import davidkeller.sign.utils.CreateVisibleSignature2;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Main Application, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
        
        testDownload();
    }

    public static void main(String[] args) {
        launch();
    }

    public void testDownload() {
    	MailDocumentWebClient client = new MailDocumentWebClient();
    	String mailId = "5f56640362f5e00d54e5ebc3";
    	String documentId = "5f56640862f5e00d54e5ebc4";
    	final Path path = FileSystems.getDefault().getPath("C:\\data\\test\\cert\\document-" + System.currentTimeMillis() + ".pdf");
    	client.consume(mailId, documentId, path);
    }
    
    public void testSign() {
        String passphrase = "pass";
        File keystoreFile = new File("C:\\data\\test\\cert\\certificate-private.p12");
        File document1File = new File("C:\\data\\test\\cert\\test1.pdf");
        File document2File = new File("C:\\data\\test\\cert\\test2.pdf");
        File document3File = new File("C:\\data\\test\\cert\\test3.pdf");
        File imageFile = new File("C:\\data\\test\\cert\\signature.png");

        System.out.println("keystoreFile: " + keystoreFile);
        System.out.println("document1File: " + document1File);
        System.out.println("imageFile: " + imageFile);
        

        Security.addProvider(new BouncyCastleProvider());
        
        try (FileInputStream fis = new FileInputStream(keystoreFile)) {
            KeyStore keystore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
			keystore.load(fis, passphrase.toCharArray());
//	        Rectangle2D stampLocation = new Rectangle2D.Float(100, 200, 150, 50);
	        Rectangle2D stampLocation = new Rectangle2D.Float(100, 400, 180, 80);
	        String signatureFieldName = "Signature1";
	        sign(keystore, passphrase, document1File, document2File, imageFile, stampLocation, signatureFieldName);

	        stampLocation = new Rectangle2D.Float(400, 400, 180, 80);
	        signatureFieldName = "Signature2";
	        sign(keystore, passphrase, document2File, document3File, imageFile, stampLocation, signatureFieldName);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    
    public void sign(
    		KeyStore keystore,
    		String password,
    		File documentInFile,
    		File documentOutFile,
    		File imageFile,
    		Rectangle2D stampLocation,
    		String signatureFieldName) 
    throws GeneralSecurityException, IOException
    {

        String tsaUrl = null;
        // External signing is needed if you are using an external signing service, e.g. to sign
        // several files at once.
        boolean externalSig = false;

        CreateVisibleSignature2 signing = new CreateVisibleSignature2(keystore, password.toCharArray());
//        signing.setSignatureAlgorithm("SHA256withECDSA");
        signing.setSignatureAlgorithm("SHA256withPLAIN-ECDSA");
        signing.setImageFile(imageFile);
        signing.setExternalSigning(externalSig);

        // Set the signature rectangle
        // Although PDF coordinates start from the bottom, humans start from the top.
        // So a human would want to position a signature (x,y) units from the
        // top left of the displayed page, and the field has a horizontal width and a vertical height
        // regardless of page rotation.

        signing.signPDF(documentInFile, documentOutFile, stampLocation, tsaUrl, signatureFieldName);
    }

}