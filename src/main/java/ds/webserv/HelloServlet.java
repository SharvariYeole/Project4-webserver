package ds.webserv;

import java.io.*;
import java.net.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.gson.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
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

        // Call the Google Books API
        //https://www.googleapis.com/books/v1/volumes?q={topic}&maxResults=10&key={api_key}
        String apiKey = "AIzaSyDSf7TR89aDcWPMQ3ir45AGW6dKAnetBKY";
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(param1, "UTF-8") + "&maxResults=10&key=" + apiKey;
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

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
    }

    public void destroy() {
    }
}