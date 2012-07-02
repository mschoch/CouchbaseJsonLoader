import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;


public class KivaJsonLoader { 

	  public static void main(String[] args) throws Exception {
		  
		  KivaJsonLoader kivaJsonLoader = new KivaJsonLoader();
		  kivaJsonLoader.loadKivaLenders(args);
		  
	    System.exit(0);
	  }

	
	  public  void loadKivaLenders(String [] args)
	  {
		  Gson headerGson = new Gson();
		  Gson lendersGson = new Gson();


		    try {
		    	int totalRecords = 0;
		    	File[] files=null;
		    	if (args.length > 0)
		    	{
		    		files = getListOfFiles(args[0]);
		    	}
		    	else
		    	{
		    		System.out.println("please provide kiva files path");
		    		System.exit(0);
		    	}
		    	
			      URI local = new URI("http://10.2.1.12:8091/pools");
			      List<URI> baseURIs = new ArrayList<URI>();
			      baseURIs.add(local);
		
			      CouchbaseClient c = new CouchbaseClient(baseURIs, "default2", "");
		    	
		    	for (File file : files)
		    	{
		    		System.out.print("file is "+file.getName()+", ");
		    		int extensionIndex = file.getName().lastIndexOf(".");
		    	    if (extensionIndex == -1)
		    	        continue;

		    	    String ext =  file.getName().substring(extensionIndex+1,file.getName().length());	
		    	    
		    	    if (!ext.equals("json"))
		    	    	continue;

			    	String filePath="";
		    		filePath =args[0]+file.getName();
		
//		    		filePath = args[0]+"632.json";
			    	System.out.print("filepath="+filePath+ ":  ");
			    	
					    	
			    	Lenders lenders = lendersGson.fromJson(new FileReader(filePath),
				    		Lenders.class);
		
		//		    KivaLender[] KivaLenders = gson.fromJson(new FileReader("994.json"),
			//	    		KivaLender[].class);
		
			      String headerJSONEntry = headerGson.toJson(lenders.header);
		          c.set(lenders.header.page, 0, headerJSONEntry).get();
//		          System.out.println("header: "+ lenders.header.page + " " + c.get(lenders.header.page));
		
		          int counter =0;
			      for (KivaLender entry : lenders.getLenders()) {
			        String JSONentry = lendersGson.toJson(entry);
			        if (entry == null || entry.lender_id == null)
			        {
			        	System.out.println("bad entry"+entry);
			        	continue;
			        }
			        for (int i=0;i<10;i++)
			        {
			        	c.set(entry.lender_id+"_"+i, 0, JSONentry).get();
			        
			        	counter++;
			        	totalRecords++;
			        }
		//	        System.out.println(entry.lender_id + " " + c.get(entry.lender_id));
		//	        System.out.println(entry.lender_id +" inserted" );
			      }
			      System.out.println(counter + " records were entered");
		
		   	}
	       System.out.println(totalRecords + " total number of records were entered");

	        c.shutdown(3, TimeUnit.SECONDS);

		    } catch (Exception e) {
		      System.err.println("Error connecting to Couchbase: " + e.getMessage());
		      e.printStackTrace();
		      System.exit(0);
		    }
	 	    
		  System.exit(0);
		  
	  }
	  
	  private File[] getListOfFiles(String path)
	  {
		  File dir = new File(path);

		  // The list of files can also be retrieved as File objects
		  File[] files = dir.listFiles();
		  // This filter only returns directories
		return files;

	  }

}

	
	

