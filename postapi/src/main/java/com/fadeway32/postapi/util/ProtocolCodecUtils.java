package com.fadeway32.postapi.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.MessageLite;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class ProtocolCodecUtils {
    private static final ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    private ProtocolCodecUtils() {
    }

    public static byte[] toProtobufBytes(MessageLite message) {
        return message == null ? new byte[0] : message.toByteArray();
    }

    public static byte[] toKryoBytes(Object value) {
        if (value == null) {
            return new byte[0];
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (Output output = new Output(byteArrayOutputStream)) {
            KRYO.get().writeClassAndObject(output, value);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromKryoBytes(byte[] bytes) {
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            return (T) KRYO.get().readClassAndObject(input);
        }
    }
}
