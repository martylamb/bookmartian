/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.function.Function;

/**
 *
 * @author mlamb
 */
public class ObjectIO {
    
    public static long assertMaxVersion(ObjectInput in, long maxVersion) throws IOException {
        long foundVersion = in.readLong();
        if (foundVersion > maxVersion)
            throw new IOException(String.format("expected serialized version <= %d but found %d", maxVersion, foundVersion));
        return foundVersion;
    }
    
    public static void writeNullableUTF(ObjectOutput out, String s) throws IOException {
        out.writeBoolean(s != null);
        if (s != null) out.writeUTF(s);
    }
    
    public static String readNullableUTF(ObjectInput in) throws IOException {
        return in.readBoolean() ? in.readUTF() : null;
    }

    public static <T> T fromNullableUTF(ObjectInput in, Function<String, T> f) throws IOException {
        String s = readNullableUTF(in);
        return (s == null) ? null : f.apply(s);
    }

    public static void writeNullableDate(ObjectOutput out, Date d) throws IOException {
        out.writeBoolean(d != null);
        if (d != null) out.writeObject(d);
    }
    
    public static Date readNullableDate(ObjectInput in) throws IOException, ClassNotFoundException {
        return in.readBoolean() ? (Date) in.readObject() : null;
    }
}
