package com.airline.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.airline.database.DBConnection;

public class TravelCompanionService {

    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    /*
     * Current Groq model.
     * Do NOT hard-code your API key here.
     */
    private static final String MODEL =
            "openai/gpt-oss-120b";

    public String getLatestDestination(int passengerId)
            throws Exception {

        String sql =
                "SELECT f.destination " +
                "FROM bookings b " +
                "JOIN flights f ON b.flight_id = f.flight_id " +
                "WHERE b.passenger_id = ? " +
                "AND b.booking_status = 'CONFIRMED' " +
                "ORDER BY b.booking_date DESC " +
                "LIMIT 1";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setInt(1, passengerId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("destination");
                }
            }
        }

        return null;
    }

    public String askGroq(String destination)
            throws Exception {

        String apiKey =
                System.getenv("GROQ_API_KEY");

        if (apiKey == null ||
                apiKey.isBlank()) {

            throw new IllegalStateException(
                    "GROQ_API_KEY is not set."
            );
        }

        String prompt =
                "Create a useful travel guide for "
                + destination
                + ". Include:\n"
                + "1. Top attractions\n"
                + "2. Local food to try\n"
                + "3. Transportation tips\n"
                + "4. Things to pack\n"
                + "5. Budget tips\n"
                + "6. Safety and emergency tips\n"
                + "7. One interesting fact\n\n"
                + "Keep the answer concise and suitable "
                + "for a college travel application.";

        String json =
                "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{"
                + "\"role\":\"system\","
                + "\"content\":\"You are a helpful travel assistant.\""
                + "},"
                + "{"
                + "\"role\":\"user\","
                + "\"content\":\""
                + escapeJson(prompt)
                + "\""
                + "}"
                + "]"
                + "}";

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {

            throw new IOException(
                    "Groq API Error "
                    + response.statusCode()
                    + ": "
                    + response.body()
            );
        }

        return extractContent(
                response.body()
        );
    }

    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String extractContent(String json)
            throws Exception {

        String marker =
                "\"content\":\"";

        int start =
                json.indexOf(marker);

        if (start == -1) {

            throw new Exception(
                    "Could not read AI response."
            );
        }

        start += marker.length();

        StringBuilder result =
                new StringBuilder();

        boolean escaped = false;

        for (int i = start;
             i < json.length();
             i++) {

            char ch = json.charAt(i);

            if (escaped) {

                if (ch == 'n') {
                    result.append('\n');
                } else if (ch == 'r') {
                    result.append('\r');
                } else if (ch == 't') {
                    result.append('\t');
                } else {
                    result.append(ch);
                }

                escaped = false;

            } else if (ch == '\\') {

                escaped = true;

            } else if (ch == '"') {

                break;

            } else {

                result.append(ch);
            }
        }

        return result.toString();
    }
}