package com.martiansoftware.bookmartian.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.javalin.plugin.json.JavalinJson;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
public class Json {
    
    private static final Gson GSON;
    
    static {
        GSON = new GsonBuilder()
                .setFieldNamingStrategy(f -> f.getName().replaceAll("^_", ""))
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                .registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY)
                .registerTypeAdapter(TagNameSet.class, new TagNameSet.GsonAdapter())
                .registerTypeAdapter(TagName.class, new TagName.GsonAdapter())
                .registerTypeAdapter(Lurl.class, new Lurl.GsonAdapter())
                .registerTypeAdapter(Color.class, new Color.GsonAdapter())
                .registerTypeAdapter(Date.class, new UTCDateAdapter())
                .registerTypeAdapter(Bookmark.class, new Bookmark.GsonAdapter())
                .create();

        JavalinJson.setFromJsonMapper(GSON::fromJson);
        JavalinJson.setToJsonMapper(GSON::toJson);
    }

    public static void init() {} // allows client to force class initialization
    
    public static String toJson(Object o) { return GSON.toJson(o); }
    public static <T> T fromJson(String j, Class<T> clazz) { return GSON.fromJson(j, clazz); }
    
    // OptionalTypeAdapter adapted from
    // https://github.com/serenity-bdd/serenity-core/blob/master/serenity-core/src/main/java/net/thucydides/core/reports/json/gson/OptionalTypeAdapter.java
    // commit f0952b4b4245ef220d4678ea49246f09bd7cc287
    // made available by the serenity-core project under the Apache license v2.0:
    //    
    //      Copyright 2011 John Ferguson Smart
    //
    //      Licensed under the Apache License, Version 2.0 (the "License");
    //      you may not use this software except in compliance with the License.
    //      You may obtain a copy of the License at
    //
    //      http://www.apache.org/licenses/LICENSE-2.0
    //
    //      Unless required by applicable law or agreed to in writing, software
    //      distributed under the License is distributed on an "AS IS" BASIS,
    //      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    //      See the License for the specific language governing permissions and
    //      limitations under the License.
    //
    private static class OptionalTypeAdapter<E> extends TypeAdapter<Optional<E>> {

        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                Class<T> rawType = (Class<T>) type.getRawType();
                if (rawType != Optional.class) {
                    return null;
                }
                final ParameterizedType parameterizedType = (ParameterizedType) type.getType();
                final Type actualType = parameterizedType.getActualTypeArguments()[0];
                final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(actualType));
                return new OptionalTypeAdapter(adapter);
            }
        };
        private final TypeAdapter<E> adapter;

        public OptionalTypeAdapter(TypeAdapter<E> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void write(JsonWriter out, Optional<E> value) throws IOException {
            if ((value != null) && (value.isPresent())) {
                adapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }

        @Override
        public Optional<E> read(JsonReader in) throws IOException {
            final JsonToken peek = in.peek();
            if (peek != JsonToken.NULL) {
                return Optional.ofNullable(adapter.read(in));
            }
            return Optional.empty();
        }

    }
    
    // TypeAdapter that can tell you what types it can handle.
    public static abstract class Adapter<T> extends TypeAdapter<T> {
        public abstract Stream<Class> classes();
    }
    
    // TypeAdapter for classes that can be represented as a single string
    public static abstract class StringAdapter<T> extends Adapter<T> {
        
        protected abstract String toString(T t) throws IOException;   // will never be called with null t
        protected abstract T fromString(String s) throws IOException; // will never be called with null s

        @Override public void write(JsonWriter writer, T t) throws IOException {
            if (t == null) writer.nullValue();
            else writer.value(toString(t));
        }
        
        @Override public T read(JsonReader reader) throws IOException {
            if (reader.peek() != JsonToken.NULL) return fromString(reader.nextString());
            reader.nextNull();
            return null;            
        }
    }
    
    // this adapter courtesy of http://stackoverflow.com/questions/26044881/java-date-to-utc-using-gson
    private static class UTCDateAdapter implements JsonSerializer<Date>,JsonDeserializer<Date> {

        private final DateFormat dateFormat;

        public UTCDateAdapter() {
          dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);      //This is the format I need
          dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));                               //This is the key line which converts the date to UTC which cannot be accessed with the default serializer
        }

        @Override public synchronized JsonElement serialize(Date date,Type type,JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(dateFormat.format(date));
        }

        @Override public synchronized Date deserialize(JsonElement jsonElement,Type type,JsonDeserializationContext jsonDeserializationContext) {
          try {
            return dateFormat.parse(jsonElement.getAsString());
          } catch (ParseException e) {
            throw new JsonParseException(e);
          }
        }
    }    

}
