package com.mediadriver.atlas.service.v2;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.mediadriver.atlas.v2.AtlasMapping;
import com.mediadriver.atlas.v2.StringMap;
import com.mediadriver.atlas.v2.StringMapEntry;

@ApplicationPath("/")
@Path("v2/atlas")
public class AtlasService extends Application {		
	
	final Application javaServiceApp;
	private String baseFolder = "target/mappings";
	
	public AtlasService() {
		javaServiceApp = new ResourceConfig()
		        .register(JacksonFeature.class);
	}

    @OPTIONS
    @Path("/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingOptions() throws Exception {
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE,OPTIONS,HEAD")
    			.build();
    }
    
    @OPTIONS
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingParameterizedOptions() throws Exception {
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE,OPTIONS,HEAD")
    			.build();
    }

    @OPTIONS
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingsOptions() throws Exception {
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE,OPTIONS,HEAD")
    			.build();
    }
	
    @GET
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMappings(@Context UriInfo uriInfo, final @QueryParam("filter") String filter) throws Exception {
    	StringMap sMap = new StringMap();
    	
    	java.nio.file.Path mappingFolder = Paths.get(baseFolder);
    	File[] mappings = mappingFolder.toFile().listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (filter != null && name != null && !name.toLowerCase().contains(filter.toLowerCase())) {
					return false;
				}
				if(name.matches("atlasmapping-[a-zA-Z0-9\\.\\-]+.xml")) {
					return true;
				}
				return false;
			}
		});
    	
    	if(mappings == null) {
    		return Response.ok()
        			.header("Access-Control-Allow-Origin", "*")
        			.header("Access-Control-Allow-Headers", "Content-Type")
        			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
        			.entity(sMap)
        			.build();
    	}
    	
    	for(File mapping : mappings) {
    		AtlasMapping map = getMappingFromFile(mapping.getAbsolutePath());
    		StringMapEntry mapEntry = new StringMapEntry();
    		mapEntry.setName(map.getName());
    		
        	UriBuilder builder = uriInfo.getBaseUriBuilder()
        			.path("v2").path("atlas").path("mapping").path(map.getName());
    		mapEntry.setValue(builder.build().toString());
    		sMap.getStringMapEntry().add(mapEntry);
    	}
    	    	
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.entity(sMap)
    			.build();
    }      
    
    @DELETE
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response removeMapping(@PathParam("mappingId") String mappingId) throws Exception {
    	
    	java.nio.file.Path mappingFilePath = Paths.get(baseFolder + File.separator + generateMappingFileName(mappingId));
    	File mappingFile = mappingFilePath.toFile();
    	
    	if(!mappingFile.exists()) {
    		return Response.noContent()
        			.header("Access-Control-Allow-Origin", "*")
        			.header("Access-Control-Allow-Headers", "Content-Type")
        			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
        			.build();
    	}
    	
    	mappingFile.delete();
    	    	
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.build();
    }
    
    @GET
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response getMapping(@PathParam("mappingId") String mappingId) throws Exception {
    	
    	java.nio.file.Path mappingFilePath = Paths.get(baseFolder + File.separator + generateMappingFileName(mappingId));
    	File mappingFile = mappingFilePath.toFile();
    	
    	if(!mappingFile.exists()) {
    		return Response.noContent()
        			.header("Access-Control-Allow-Origin", "*")
        			.header("Access-Control-Allow-Headers", "Content-Type")
        			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
        			.build();
    	}
    	
    	AtlasMapping atlasMapping = null;
    	try {
    		atlasMapping = getMappingFromFile(mappingFile.getAbsolutePath());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.entity(atlasMapping)
    			.build();
    }
    
    @PUT
    @Path("/mapping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMapping(AtlasMapping mapping, @Context UriInfo uriInfo) throws Exception {
    	return saveMapping(mapping, uriInfo);
    }
    
    @POST
    @Path("/mapping/{mappingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMapping(AtlasMapping mapping, @Context UriInfo uriInfo) throws Exception {
    	return saveMapping(mapping, uriInfo);
    }
    	
    protected Response saveMapping(AtlasMapping mapping, UriInfo uriInfo) throws Exception {
    	try {
    		saveMappingToFile(mapping);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    	builder.path(mapping.getName());

    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.location(builder.build())
    			.build();
    }
    
    protected AtlasMapping getMappingFromFile(String fileName) throws Exception {
    	JAXBContext jaxbContext = JAXBContext.newInstance("com.mediadriver.atlas.v2");
    	Marshaller marshaller = null;
    	Unmarshaller unmarshaller = null;

		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		unmarshaller = jaxbContext.createUnmarshaller();
		
		StreamSource fileSource = new StreamSource(new File(fileName));
		JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
		if(mappingElem != null) {
			return mappingElem.getValue();
		}
		return null;	
    }
    
    protected void saveMappingToFile(AtlasMapping atlasMapping) throws Exception {
    	JAXBContext jaxbContext = JAXBContext.newInstance("com.mediadriver.atlas.v2");
    	Marshaller marshaller = null;

		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		marshaller.marshal(atlasMapping, new File(baseFolder + File.separator + generateMappingFileName(atlasMapping.getName())));
    }
    
    protected String generateMappingFileName(String mappingName) {
    	return String.format("atlasmapping-%s.xml", mappingName);
    }
}