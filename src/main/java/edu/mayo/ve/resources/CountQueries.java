/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.ve.resources;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import edu.mayo.ve.util.MongoConnection;
import edu.mayo.ve.util.Tokens;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author m102417
 */
@Path("/ve/count")
public class CountQueries {
     Mongo m = MongoConnection.getMongo();
    
     @GET
     @Path("/w/{workspaceid}")
     @Produces("application/json")
     public String countVariantsInWorkspace(@PathParam("workspaceid") String workspaceID) {
             DB db = m.getDB( Tokens.WORKSPACE_DATABASE );
             DBCollection col = db.getCollection(workspaceID);
             long count = col.count(new BasicDBObject());
             return "{\"VariantCount\":" + count  + "}\n";
     }
    
}
