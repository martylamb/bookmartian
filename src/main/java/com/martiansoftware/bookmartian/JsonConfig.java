package com.martiansoftware.bookmartian;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.martiansoftware.boom.Json;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 *
 * @author mlamb
 */
public class JsonConfig {
    
    public static void init() {
        GsonBuilder creator = new GsonBuilder()
                .setFieldNamingStrategy(f -> f.getName().replaceAll("^_", ""))
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY)
                .registerTypeAdapter(TagRefs.class, new TagRefs.GsonAdapter());

        Json.use(creator.create());
    }

    
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
    
}
