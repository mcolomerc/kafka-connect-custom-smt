package org.mcolomerc;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpFieldTransformer<R extends ConnectRecord<R>> implements Transformation<R> {

    public static final String FIELD_CONFIG = "field";
    public static final String REGEXP_CONFIG = "regexp";
    public static final String REPLACEMENT_CONFIG = "replacement";

    private String fieldName;
    private Pattern pattern;
    private String replacement;

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(FIELD_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Field to transform")
            .define(REGEXP_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Regular expression to match")
            .define(REPLACEMENT_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Replacement string");

    @Override
    public void configure(Map<String, ?> configs) {
        SimpleConfig config = new SimpleConfig(CONFIG_DEF, configs);
        fieldName = config.getString(FIELD_CONFIG);
        pattern = Pattern.compile(config.getString(REGEXP_CONFIG));
        replacement = config.getString(REPLACEMENT_CONFIG);
    }

    @Override
    public R apply(R record) {
        if (record.value() == null) {
            return record;
        }

        Object value = record.value();

        if (value instanceof Struct) {
            // Handle Avro/Protobuf/JSON Schema records
            Struct struct = (Struct) value;
            Schema schema = record.valueSchema();

            if (struct.schema().field(fieldName) != null) {
                String originalValue = struct.getString(fieldName);
                Matcher matcher = pattern.matcher(originalValue);
                String newValue = matcher.replaceAll(replacement);

                // Create a new Struct with the updated value
                Struct updatedStruct = new Struct(schema);
                for (Field field : schema.fields()) {
                    if (field.name().equals(fieldName)) {
                        updatedStruct.put(fieldName, newValue);
                    } else {
                        updatedStruct.put(field.name(), struct.get(field));
                    }
                }

                return record.newRecord(
                        record.topic(),
                        record.kafkaPartition(),
                        record.keySchema(),
                        record.key(),
                        schema,
                        updatedStruct,
                        record.timestamp()
                );
            }
        } else if (value instanceof Map) {
            // Handle schema-less JSON records
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) value;
            if (valueMap.containsKey(fieldName)) {
                String originalValue = (String) valueMap.get(fieldName);
                Matcher matcher = pattern.matcher(originalValue);
                String newValue = matcher.replaceAll(replacement);

                // Create a new Map with the updated value
                Map<String, Object> updatedMap = new HashMap<>(valueMap);
                updatedMap.put(fieldName, newValue);

                return record.newRecord(
                        record.topic(),
                        record.kafkaPartition(),
                        record.keySchema(),
                        record.key(),
                        record.valueSchema(),
                        updatedMap,
                        record.timestamp()
                );
            }
        }

        return record;
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {
        // No resources to clean up
    }
}
