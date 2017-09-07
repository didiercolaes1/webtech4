package edu.ap.jaxrs;

import java.io.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.json.*;
import javax.servlet.ServletContext;

@Path("/products")
public class ProductResource {
	
	private String FILE;
	
	public ProductResource(@Context ServletContext servletContext) {
		FILE = servletContext.getInitParameter("FILE_PATH");
	}
	
	/*@GET
	@Produces({"text/html"})
	public String getProductsHTML() {
		String htmlString = "<html><body>";
		try {
			JAXBContext jaxbContext1 = JAXBContext.newInstance(ProductsXML.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
			File XMLfile = new File("/Users/philippepossemiers/Desktop/Products.xml");
			ProductsXML productsXML = (ProductsXML)jaxbUnmarshaller.unmarshal(XMLfile);
			ArrayList<Product> listOfProducts = productsXML.getProducts();
			
			for(Product product : listOfProducts) {
				htmlString += "<b>Name : " + product.getName() + "</b><br>";
				htmlString += "Id : " + product.getId() + "<br>";
				htmlString += "Brand : " + product.getBrand() + "<br>";
				htmlString += "Description : " + product.getDescription() + "<br>";
				htmlString += "Price : " + product.getPrice() + "<br>";
				htmlString += "<br><br>";
			}
		} 
		catch (JAXBException e) {
		   e.printStackTrace();
		}
		return htmlString;
	}*/
	
	@GET
	@Produces({"text/html"})
	public String getProductsHTML() {
		String htmlString = "<html><body>";
		try {
			JsonReader reader = Json.createReader(new StringReader(getProductsJSON()));
			JsonObject rootObj = reader.readObject();
			JsonArray array = rootObj.getJsonArray("products");
			
			for(int i = 0 ; i < array.size(); i++) {
				JsonObject obj = array.getJsonObject(i);
				htmlString += "<b>Name : " + obj.getString("name") + "</b><br>";
				htmlString += "ID : " + obj.getString("id") + "<br>";
				htmlString += "Brand : " + obj.getString("brand") + "<br>";
				htmlString += "Description : " + obj.getString("description") + "<br>";
				htmlString += "Price : " + obj.getJsonNumber("price") + "<br>";
				htmlString += "<br><br>";
			}
		}
		catch(Exception ex) {
			htmlString = "<html><body>" + ex.getMessage();
		}

		return htmlString + "</body></html>";
	}
	
	@GET
	@Produces({"application/json"})
	public String getProductsJSON() {
		String jsonString = "";
		try {
			InputStream fis = new FileInputStream(FILE);
	        JsonReader reader = Json.createReader(fis);
	        JsonObject obj = reader.readObject();
	        reader.close();
	        fis.close();
	        
	        jsonString = obj.toString();
		} 
		catch (Exception ex) {
			jsonString = ex.getMessage();
		}
		
		return jsonString;
	}
	
	@GET
	@Path("{id}")
	@Produces({"application/json"})
	public String getProductJSON(@PathParam("id") String id) {
		String jsonString = "";
		try {
			InputStream fis = new FileInputStream(FILE);
	        JsonReader reader = Json.createReader(fis);
	        JsonObject jsonObject = reader.readObject();
	        reader.close();
	        fis.close();
	        
	        JsonArray array = jsonObject.getJsonArray("products");
	        for(int i = 0; i < array.size(); i++) {
	        	JsonObject obj = array.getJsonObject(i);
	        	if(obj.getString("id").equalsIgnoreCase(id)) {
	            	jsonString = obj.toString();
	            	break;
	            }
	        }
		} 
		catch (Exception ex) {
			jsonString = ex.getMessage();
		}
		
		return jsonString;
	}
	
	@GET
	@Path("/add")
	@Produces("text/html")
	public String getProductForm() {
		String form = "<html><body><h1>Add Product</h1><form action='/JAX-RS/products' method='post'><p>";
		form += "ID : <input type='text' name='id' /></p><p>Name : <input type='text' name='name' />";
		form += "</p><p>Brand : <input type='text' name='brand' /></p><p>Price : <input type='text' name='price' /></p>";
		form += "<p>Description : <input type='text' name='description' /></p><input type='submit' value='Add Product' />";
		form += "</form></body></html>";
		
		return form;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addProduct(@FormParam("id") String id, @FormParam("name") String name, 
							 @FormParam("brand") String brand, @FormParam("price") float price, 
							 @FormParam("description") String description) {
	
		System.out.println(description);
		
		java.net.URI location = null;
		try {
			// read existing products
			InputStream fis = new FileInputStream(FILE);
			JsonReader jsonReader1 = Json.createReader(fis);
			JsonObject jsonObject = jsonReader1.readObject();
			jsonReader1.close();
			fis.close();
			
			JsonArray array = jsonObject.getJsonArray("products");
			JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
				        
			for(int i = 0; i < array.size(); i++){
				 // add existing products
				 JsonObject obj = array.getJsonObject(i);
				 arrBuilder.add(obj);
			}
			// add new product
			JsonObjectBuilder b = Json.createObjectBuilder().
					add("id", id).
					add("name", name).
					add("brand", brand).
					add("price", price).
					add("description", description);
			arrBuilder.add(b.build());
			
			// now wrap it in a JSON object
	        JsonArray newArray = arrBuilder.build();
	        JsonObjectBuilder builder = Json.createObjectBuilder();
	        builder.add("products", newArray);
	        JsonObject newJSON = builder.build();

	        // write to file
	        OutputStream os = new FileOutputStream(FILE);
	        JsonWriter writer = Json.createWriter(os);
	        writer.writeObject(newJSON);
	        writer.close();
			
			location = new java.net.URI("/JAX-RS/products");
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return Response.seeOther(location).build();
	}
	
	@POST
	@Consumes({"application/json"})
	public Response addProduct(String productJSON) {
		java.net.URI location = null;
		try {
			// read existing products
			InputStream fis = new FileInputStream(FILE);
	        JsonReader jsonReader1 = Json.createReader(fis);
	        JsonObject jsonObject = jsonReader1.readObject();
	        jsonReader1.close();
	        fis.close();
	        
	        JsonReader jsonReader2 = Json.createReader(new StringReader(productJSON));
	        JsonObject newObject = jsonReader2.readObject();
	        jsonReader2.close();
	        
	        JsonArray array = jsonObject.getJsonArray("products");
	        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
	        
	        for(int i = 0; i < array.size(); i++){
	        	// add existing products
	        	JsonObject obj = array.getJsonObject(i);
	        	arrBuilder.add(obj);
	        }
	        // add new product
	        arrBuilder.add(newObject);
	        
	        // now wrap it in a JSON object
	        JsonArray newArray = arrBuilder.build();
	        JsonObjectBuilder builder = Json.createObjectBuilder();
	        builder.add("products", newArray);
	        JsonObject newJSON = builder.build();

	        // write to file
	        OutputStream os = new FileOutputStream(FILE);
	        JsonWriter writer = Json.createWriter(os);
	        writer.writeObject(newJSON);
	        writer.close();
	        
	        location = new java.net.URI("/JAX-RS/products");
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return Response.seeOther(location).build();
	}
}