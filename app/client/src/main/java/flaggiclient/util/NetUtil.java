/*
 * Author: Matěj Šťastný
 * Date created: 12/1/2024
 * Github link: https://github.com/kireiiiiiiii/Flaggi
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package flaggiclient.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * TODO: Not yet working
 * Use: https://api.github.com/repos/kireiiiiiiii/Flaggi/tags
 *
 */
public class NetUtil {
    /**
     * Fetches the tag of the latest release for a given GitHub repository.
     *
     * @param owner the owner of the repository (e.g., "octocat").
     * @param repo  the name of the repository (e.g., "Hello-World").
     * @param token optional GitHub personal access token for authentication (can be
     *              null).
     * @return the tag name of the latest release, or null if not found.
     * @throws Exception if an error occurs while fetching the release information.
     */
    public static String getLatestReleaseTag(String owner, String repo, String token) throws Exception {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", owner, repo);

        // Open the connection to the GitHub API
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Add authorization header if a token is provided
        if (token != null && !token.isEmpty()) {
            connection.setRequestProperty("Authorization", "token " + token);
        }

        // Check the response code
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to fetch latest release. HTTP Response Code: " + responseCode);
        }

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the JSON response to extract the tag name
        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("tag_name");
    }

    public static void main(String[] args) {
        try {
            // Replace with the repository owner and name you want to query
            String owner = "octocat";
            String repo = "Hello-World";
            String token = null; // Provide your GitHub token here, or leave as null for unauthenticated requests

            String latestTag = getLatestReleaseTag(owner, repo, token);
            System.out.println("Latest release tag: " + latestTag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
