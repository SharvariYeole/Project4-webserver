package ds.webserv;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.gson.*;
import org.bson.Document;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;
    String encodedUsername = URLEncoder.encode("sharvariyeole@gmail.com", StandardCharsets.UTF_8);
    String encodedPassword = URLEncoder.encode("EknathKalbandhe@2707", StandardCharsets.UTF_8);

    String connectionURL = "mongodb://" + encodedUsername + ":" + encodedPassword + "@CLUSTER.mongodb.net/mydb?retryWrites=true&w=majority";
    //private static final String CONNECTION_STRING = "mongodb://sharvariyeole@gmail.com:EknathKalbandhe@2707@CLUSTER.mongodb.net/mydb?retryWrites=true&w=majority";
    private MongoClient mongoClient;
    MongoCollection<Document> collection;


    @Override
    public void init() throws ServletException {
        // Create a MongoClient using the connection string
        message = "Welcome to Dashboard";
        ConnectionString connectionString = new ConnectionString(connectionURL);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("databaseName");

        // Get a collection (creates it if it doesn't exist)

        collection = database.getCollection("collectionName");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Insert a document
        Document query = new Document("key", "value");
        FindIterable<Document> documents = collection.find(query);

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");

        // Iterate over the matching documents
        MongoCursor<Document> cursor = documents.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            out.println("<h2>" + document.toJson() + "</h2>");
        }

        out.println("</body></html>");

        // Close the cursor and client when done
        cursor.close();
        mongoClient.close();


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Read the request body as a String
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        String requestBody = sb.toString();

        // Parse the JSON string
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

        // Extract parameters from JSON
        String param1 = json.get("key").getAsString();
        System.out.println("param1: " + param1);

        // Insert a document
        Document document = new Document("key", param1);
        collection.insertOne(document);

        // Call the Google Books API
        //https://www.googleapis.com/books/v1/volumes?q={topic}&maxResults=10&key={api_key}
        String apiKey = "AIzaSyDSf7TR89aDcWPMQ3ir45AGW6dKAnetBKY";
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(param1, "UTF-8") + "&maxResults=10&key=" + apiKey;
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Read the API response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder apiResponse = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                apiResponse.append(inputLine);
            }
            in.close();

            // Parse the JSON response using Gson
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(apiResponse.toString(), JsonObject.class);

            // Extract information from the JSON response
            JsonArray items = jsonResponse.getAsJsonArray("items");
            JsonArray result = new JsonArray();

            for (JsonElement item : items) {
                JsonObject volumeInfo = item.getAsJsonObject().getAsJsonObject("volumeInfo");
                String title = volumeInfo.get("title").getAsString();
                JsonArray authorsArray = volumeInfo.getAsJsonArray("authors");
                String pubDate = volumeInfo.get("publishedDate").getAsString();

                String authors = "";
                if (authorsArray != null) {
                    authors = String.join(", ", gson.fromJson(authorsArray, String[].class));
                }

                JsonObject book = new JsonObject();
                book.addProperty("title", title);
                book.addProperty("author", authors);
                book.addProperty("date", pubDate);
                result.add(book);

                System.out.println("Title: " + title);
            }

            // Set response content type
            resp.setContentType("application/json");

            // Send a success message as the response to the POST request
            PrintWriter out = resp.getWriter();
            out.println(result.toString());
        } else {
            // Handle error response
            resp.setContentType("application/json");
            resp.setStatus(conn.getResponseCode());
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("code", conn.getResponseCode());
            errorObject.addProperty("message", "Resp received from Google's API");

            // Write the JSON object to the response output stream
            try (PrintWriter out = resp.getWriter()) {
                out.println(errorObject.toString());
            }

        }
    }

    public void destroy() {
    }
}