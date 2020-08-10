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
import java.util.Base64;

public class TallyConnectionBuilder {

    @Getter
    private final String host;
    @Getter
    private final HttpURLConnection connection;

    @SneakyThrows
    public TallyConnectionBuilder(String host) {
        this.host = host;
        this.connection = ((HttpURLConnection) new URL(this.host).openConnection());
        this.header("User-Agent", "Tally/Java");
    }

    public TallyConnectionBuilder build() {
        return this;
    }

    @SneakyThrows
    public TallyConnectionBuilder withMethod(String method) {
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
        this.connection.setRequestProperty(key, val);
        return this;
    }

    public TallyConnectionBuilder authBearer(String token) {
        this.connection.setRequestProperty("Authorization", "Bearer " + token);
        return this;
    }

    public TallyConnectionBuilder authBasic(String user, String pass) {
        String var1 = user + ":" + pass;
        this.connection.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode(var1.getBytes())));
        return this;
    }

    public TallyConnectionBuilder json() {
        this.connection.setRequestProperty("Content-Type", "application/json");
        return this;
    }

    public TallyConnectionBuilder writeJson(JsonElement json) {
        this.json().writeOut(new Gson().toJson(json));
        return this;
    }

    @SneakyThrows
    public TallyConnectionBuilder writeOut(String string) {
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
        return new JsonParser().parse(this.read)
                .getAsJsonObject();
    }

    public JsonElement verifyJsonReturn(boolean againstTallyExpectations) {
        if(this.verifyJson(againstTallyExpectations)) {
            return this.getReadJson();
        } else {
            return null;
        }
    }

    public TallyConnectionBuilder verifyJsonCallback(boolean againstTallyAPIExpectations, ParameterizedCallback<JsonElement> callback, ParameterizedCallback<Exception> err) {
        try {
            this.verifyJsonThrowing(againstTallyAPIExpectations);
            callback.run(this.getReadJson());
        } catch(Exception e) {
            err.run(e);
        }
        return this;
    }

    @SneakyThrows
    public TallyConnectionBuilder verifyJsonThrowing(boolean againstTallyAPIExpectations) {
        if(this.verifyJson(againstTallyAPIExpectations)) {
            return this;
        } else {
            throw new APIException("Failed verification of json request!");
        }
    }

    public boolean verifyJson(boolean againstTallyAPIExpectations) {
        try {
            JsonElement readJson = this.getReadJson();
            if(againstTallyAPIExpectations) {
                JsonObject asJsonObject = readJson.getAsJsonObject();
                if(asJsonObject.has("error") && asJsonObject.get("error").getAsBoolean()) {
                    TallyLogger.say("An error was produced in request: " + asJsonObject.get("message").getAsString());
                    return false;
                }
                if(asJsonObject.has("warning") && asJsonObject.get("warning").getAsBoolean()) {
                    TallyLogger.say("Warning was produced in request: " + asJsonObject.get("message").getAsString());
                }
            }
            return true;
        } catch(Exception e) {
            TallyLogger.say("Error produced whilst verifying json request.");
            e.printStackTrace();
            return false;
        }
    }

    @SneakyThrows
    public TallyConnectionBuilder readIn() {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream readInStream = null;
        if((this.connection.getResponseCode() + "").startsWith("2")) {
            readInStream = this.connection.getInputStream();
        } else {
            readInStream = this.connection.getErrorStream();
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(readInStream, "utf-8"))) {
            String line = null;
            while((line = br.readLine()) != null) {
                stringBuilder.append(line.trim());
            }
        }
        this.read = stringBuilder.toString();
        return this;
    }
}
