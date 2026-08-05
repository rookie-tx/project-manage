package cn.tx.aiagent.common.util;

import tools.jackson.databind.ObjectMapper;

public class MapperUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T readValue(String value, Class<T> valueType) {
        return MAPPER.readValue(value, valueType);
    }
}
