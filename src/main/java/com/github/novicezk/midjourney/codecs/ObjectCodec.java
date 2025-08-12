package com.github.novicezk.midjourney.codecs;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;

public class ObjectCodec implements Codec<Object> {

    @Override
    public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {
        writeValue(writer, value);
    }

    @Override
    public Class<Object> getEncoderClass() {
        return Object.class;
    }

    @Override
    public Object decode(BsonReader reader, DecoderContext decoderContext) {
        return readValue(reader);
    }

    private static Object readValue(BsonReader bsonReader) {
        var type = bsonReader.getCurrentBsonType();
        switch (type) {
            case ARRAY:
                ArrayList<Object> array = new ArrayList<>();
                bsonReader.readStartArray();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    array.add(readValue(bsonReader));
                }
                bsonReader.readEndArray();
                return array;
            case BINARY:
                return bsonReader.readBinaryData();
            case BOOLEAN:
                return bsonReader.readBoolean();
            case DATE_TIME:
                return bsonReader.readDateTime();
            case DB_POINTER:
                return bsonReader.readDBPointer();
            case DECIMAL128:
                return bsonReader.readDecimal128();
            case DOCUMENT:
                HashMap<String, Object> nestedMap = new HashMap<>();
                bsonReader.readStartDocument();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    nestedMap.put(bsonReader.readName(), readValue(bsonReader));
                }
                bsonReader.readEndDocument();
                return nestedMap;
            case DOUBLE:
                return bsonReader.readDouble();
            case INT32:
                return bsonReader.readInt32();
            case INT64:
                return bsonReader.readInt64();
            case NULL:
                return null;
            case OBJECT_ID:
                return bsonReader.readObjectId();
            case STRING:
                return bsonReader.readString();
            case TIMESTAMP:
                return bsonReader.readTimestamp();
            case UNDEFINED:
                return null;
            default:
                return null;

        }
    }

    private static void writeValue(BsonWriter bsonWriter, Object value) {
        if (value instanceof String) {
            bsonWriter.writeString(value.toString());
        } else if (value instanceof Integer) {
            bsonWriter.writeInt32((Integer) value);
        } else if (value instanceof Long) {
            bsonWriter.writeInt64((Long) value);
        } else if (value instanceof BigDecimal) {
            bsonWriter.writeDecimal128(Decimal128.parse(value.toString()));
        } else if (value instanceof Double) {
            bsonWriter.writeDouble((Double) value);
        } else if (value instanceof Boolean) {
            bsonWriter.writeBoolean((Boolean) value);
        } else if (value instanceof HashMap) {
            // Recursively handle HashMap for nesting
            bsonWriter.writeStartDocument();
            HashMap<?, ?> nestedMap = (HashMap<?, ?>) value;
            for (Map.Entry<?, ?> entry : nestedMap.entrySet()) {
                String key = entry.getKey().toString();
                Object nestedValue = entry.getValue();
                bsonWriter.writeName(key);
                writeValue(bsonWriter, nestedValue);
            }
            bsonWriter.writeEndDocument();
        } else if (value instanceof ArrayList) {
            ArrayList<?> arrayList = (ArrayList<?>) value;
            bsonWriter.writeStartArray();
            for (Object item : arrayList) {
                writeValue(bsonWriter, item);
            }
            bsonWriter.writeEndArray();
        } else if (value.getClass().isArray()) {
            bsonWriter.writeStartArray();
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(value, i);
                writeValue(bsonWriter, item);
            }
            bsonWriter.writeEndArray();
        } else {
            try {
                Class<?> clazz = value.getClass();
                bsonWriter.writeStartDocument();
                while (clazz != null) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        int modifiers = field.getModifiers();
                        if (!java.lang.reflect.Modifier.isFinal(modifiers)) {
                            field.setAccessible(true);
                            String fieldName = field.getName();
                            if (fieldName.equals("id")) {
                                fieldName = "_id";
                            }
                            Object fieldValue = field.get(value);
                            bsonWriter.writeName(fieldName);
                            writeValue(bsonWriter, fieldValue);
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
                bsonWriter.writeEndDocument();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}