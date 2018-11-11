package net.toastynetworks.MCLAdmin.DAL.Contexts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import net.toastynetworks.MCLAdmin.DAL.Contexts.Interfaces.IModpackContext;
import net.toastynetworks.MCLAdmin.Domain.Modpack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ModpackRestApiContext implements IModpackContext {

    static {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public List<String> GetAllModpackNames() {
        return null;
    }

    public List<String> GetAllModpackVersions() {
        return null;
    }

    public List<Modpack> GetAllModpacks() {
        return GetModpackList();
    }

    public void AddModpack(Modpack modpack) {
        try {
            Modpack modpackObject = new Modpack(modpack.getModpackName(), modpack.getModpackVersionType());
            System.out.println(modpackObject.getModpackName() + modpackObject.getModpackVersionType());
            HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:8080/v1/modpack/addModpack")
                    .header("Content-Type", "application/json")
                    .body(modpackObject)
                    .asJson();

            if (jsonResponse.getStatus() != 200) {
                System.out.println("Headers: " + jsonResponse.getHeaders());
                System.out.println("Body: " + jsonResponse.getBody());
                throw new RuntimeException("Failed: HTTP error code : " + jsonResponse.getStatus() + " " + jsonResponse.getStatusText());
            }

        } catch (Exception exception) {
            System.out.println(exception);
        }
    }
    public List<Modpack> GetModpackList() {
        try{
            HttpResponse<Modpack[]> modpackListResponse = Unirest.get("http://localhost:8080/v1/modpack").asObject(Modpack[].class);
            Modpack[] modpackObjectArray = modpackListResponse.getBody();
            return Arrays.asList(modpackObjectArray);
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }


}
