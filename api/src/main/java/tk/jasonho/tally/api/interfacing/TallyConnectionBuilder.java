package tk.jasonho.tally.api.interfacing;

import com.google.gson.*;
import lombok.Getter;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.exceptions.APIException;
import tk.jasonho.tally.api.util.TallyLogger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TallyConnectionBuilder {

    @Getter
    private final String host;
    @Getter
    private final HttpURLConnection connection;

    @SneakyThrows
    public TallyConnectionBuilder(String host) {
        TallyLogger.optionalLog("TallyConnectionBuilder: " + host);
        this.host = host;
        this.connection = ((HttpURLConnection) new URL(this.host).openConnection());
        this.header("User-Agent", "Tally/Java");
    }

    /**
     * Methods.
     */
    @SneakyThrows
    private TallyConnectionBuilder withMethod(String method) {
        TallyLogger.optionalLog("  ...method: " + method);
        this.connection.setRequestMethod(method);
        return this;
    }

    public TallyConnectionBuilder get() {
        this.withMethod("GET");
        return this;
    }

    public TallyConnectionBuilder post() {
        this.withMethod("POST");
        return this;
    }

    public TallyConnectionBuilder header(String key, String val) {
        TallyLogger.optionalLog("  ...header: " + key + " => " + val);
        this.connection.setRequestProperty(key, val);
        return this;
    }

    public TallyConnectionBuilder authBearer(String token) {
        this.header("Authorization", "Bearer " + token);
        return this;
    }

    public TallyConnectionBuilder json() {
        this.header("Content-Type", "application/json");
        return this;
    }

    public TallyConnectionBuilder writeJson(JsonElement json) {
        String body = new Gson().toJson(json);
        TallyLogger.optionalLog("  ...JSON to body: " + body);
        this.json().writeOut(body);
        return this;
    }

    @SneakyThrows
    public TallyConnectionBuilder writeOut(String string) {
        TallyLogger.optionalLog("Writing out to " + this.host + ": " + string);
        this.connection.setDoOutput(true);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        this.connection.setFixedLengthStreamingMode(bytes.length);
        try(OutputStream outputStream = this.connection.getOutputStream()) {
            outputStream.write(bytes);
        }
        return this;
    }

    @Getter
    public String read;

    public JsonElement getReadJson() throws JsonSyntaxException {
        if (this.read == null) {
            this.readIn();
        }
        return new JsonParser().parse(this.read)
                .getAsJsonObject();
    }

    private String responseMessage(JsonObject response) {
        if (!response.has("message")) {
            return null;
        }
        JsonElement message = response.get("message");
        if (message.isJsonPrimitive()) {
            return message.getAsString();
        } else if (message.isJsonNull()) {
            return null;
        } else {
            throw new IllegalStateException("The `message` field of a Tally response must be a string, null, or undefined.");
        }
    }

    private boolean responseErrored(JsonObject response) {
        if (!response.has("error")) {
            return false;
        }
        JsonElement error = response.get("error");
        if (error.isJsonPrimitive()) {
            return error.getAsBoolean();
        } else if (error.isJsonNull()) {
            return false;
        } else {
            throw new IllegalStateException("The `error` field of a Tally response must be a boolean, null, or undefined");
        }
    }

    @SneakyThrows
    public TallyConnectionBuilder verifyJsonThrowing() {
        if (!this.verifyJson()) {
            throw new APIException("Verification of response failed!");
        }
        return this;
    }

    public boolean verifyJson() {
        try {
            JsonElement readJson = this.getReadJson();
            if (!readJson.isJsonObject()) {
                TallyLogger.say("Response was not a JSON object!");
                return false;
            }
            JsonObject asJsonObject = readJson.getAsJsonObject();

            String message = this.responseMessage(asJsonObject);
            if (message != null) {
                TallyLogger.say("Message from Tally endpoint: " + message);
            }

            if (this.responseErrored(asJsonObject)) {
                TallyLogger.say("An error was produced in request: " + message);
                TallyLogger.say(asJsonObject.toString());
                return false;
            }

            TallyLogger.optionalLog("Validated response.");
            return true;
        } catch(Exception e) {
            TallyLogger.say("Error produced whilst verifying json request.");
            e.printStackTrace();
            return false;
        }
    }

    @SneakyThrows
    public TallyConnectionBuilder readIn() {
        TallyLogger.optionalLog("Reading in for request at " + this.host);
        StringBuilder stringBuilder = new StringBuilder();
        InputStream readInStream = null;
        if((this.connection.getResponseCode() + "").startsWith("2")) {
            readInStream = this.connection.getInputStream();
        } else {
            readInStream = this.connection.getErrorStream();
        }

        InputStreamReader inReader = new InputStreamReader(readInStream, StandardCharsets.UTF_8);
        BufferedReader bReader = new BufferedReader(inReader);
        String line = null;
        while((line = bReader.readLine()) != null) {
            stringBuilder.append(line.trim());
        }

        this.read = stringBuilder.toString();
        TallyLogger.optionalLog("Read for " + this.host + ": " + this.read);
        return this;
    }
}
