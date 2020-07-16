package com.stirante.controllerviz;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.stirante.controllerviz.model.Transition;

import java.lang.reflect.Type;
import java.util.Map;

public class TransitionDeserializer implements JsonDeserializer<Transition> {

    @Override
    public Transition deserialize(JsonElement paramJsonElement, Type paramType,
                                  JsonDeserializationContext paramJsonDeserializationContext) throws JsonParseException {

        Transition transition = new Transition();
        Map.Entry<String, JsonElement> entry = paramJsonElement.getAsJsonObject()
                .entrySet()
                .stream()
                .findFirst()
                .orElseThrow(() -> new JsonParseException("Empty transition!"));

        transition.targetState = entry.getKey();
        transition.condition = entry.getValue().getAsString();

        return transition;
    }

}