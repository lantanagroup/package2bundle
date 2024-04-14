package com.lantanagroup.package2bundle;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.json.JsonLikeStructure;
import ca.uhn.fhir.parser.json.jackson.JacksonStructure;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Resource;

public class PackageToBundle {

	private static FhirContext ctxR4 = FhirContext.forR4();
	private static Bundle.BundleType bundleType = Bundle.BundleType.TRANSACTION;
	private static JsonParser jp = (JsonParser)ctxR4.newJsonParser();
	
	
    public static void main( String[] args ){
        PackageToBundle app = new PackageToBundle();
        for (String filePath : args) {
            File pkgFile = new File(filePath);
            File outputFile =  new File(pkgFile.getParent(),pkgFile.getName()+".json");
            if (outputFile.exists()) outputFile.delete();
            try {
	    			Bundle b = app.package2Bundle(pkgFile);
	    			Path out = outputFile.toPath();
	    			String serialized = jp.encodeResourceToString(b);
	    			Files.write(out.toAbsolutePath(), serialized.getBytes(), StandardOpenOption.CREATE);
	    			System.out.println();
	    			System.out.println("Done, package bundle created at " + out.toString());
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
        }
    }
    
    private Bundle package2Bundle(File pkgFile) throws IOException {
    		Bundle b = null;
    		File destDir = Files.createTempDirectory("tgz-").toFile();
    		destDir.deleteOnExit();
    		extractPackage(pkgFile,destDir);
    	//	System.out.println(destDir.getAbsolutePath());
    		File packageFolder = new File(destDir,"package");
    		if (packageFolder.exists()) {
    			b = new Bundle();
    			b.setType(bundleType);
    			addFolderToBundle(b, packageFolder);
    		}
    		return b;
    }

	private void addFolderToBundle(Bundle b, File packageFolder) {
	//	System.out.println("Processing folder " + packageFolder.getName());
		File[] files = packageFolder.listFiles();
		for (File f: files) {
			if (f.isDirectory() == false) {
				if (f.getName().equals("package.json") == false 
						&& f.getName().equals(".index.json") == false
						&& f.getName().equals(".index.db") == false) {
					try {
						FileReader fr = new FileReader(f);
						JsonLikeStructure jls = new JacksonStructure();
						jls.load(fr);
						IBaseResource r = jp.parseResource(jls);
						BundleEntryComponent bec = b.addEntry().setResource((Resource)r);
						bec.getRequest().setMethod(HTTPVerb.PUT).setUrl(r.fhirType() + "/" + r.getIdElement().getIdPart());
						System.out.print(".");
					} catch (Exception e) {
						System.err.println("Error processing file " + f.getAbsolutePath());
						e.printStackTrace();
					}
				}
			} else {
				if (f.getName().equals("example")) addFolderToBundle(b, f);
			}
				
		}
	}
    
    private void extractPackage (File sourceFile, File destDir) {
    		final TarGZipUnArchiver ua = new TarGZipUnArchiver();
    		ConsoleLoggerManager manager = new ConsoleLoggerManager();
    		manager.initialize();
    		ua.enableLogging(manager.getLoggerForComponent("bla"));
    		ua.setSourceFile(sourceFile);
    		destDir.mkdirs();
    		ua.setDestDirectory(destDir);
    		ua.extract();
    }
}
