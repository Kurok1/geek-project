package org.geektimes.configuration.microprofile.config.discover.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.geektimes.configuration.microprofile.config.discover.ProtocolBasedConfigSourceFactory;
import org.geektimes.configuration.microprofile.config.source.MapConfigSource;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 从JSON文件创建 {@link ConfigSource}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.10
 * @see ObjectMapper
 * @see JsonNode
 */
public class JsonConfigSourceFactory extends ProtocolBasedConfigSourceFactory {

    private final ObjectMapper JSON = new ObjectMapper();
    
    @Override
    public ConfigSource createConfigSource(String name, int ordinal, URL resource, String encoding) {
        try (InputStreamReader reader = new InputStreamReader(resource.openStream(), encoding)) {
            JsonNode rootNode = JSON.readTree(reader);
            Map<String, String> map = new HashMap<>();
            if (rootNode.isObject()) {
                map = resolveJsonNode(rootNode, "", map);
            }

            if (rootNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode itemNode = arrayNode.get(i);
                    if (itemNode.isValueNode()) {
                        //忽略
                        continue;
                    }

                    if (itemNode.isObject()) {
                        //递归
                        map = resolveJsonNode(itemNode, "", map);
                        continue;
                    }

                    //todo 暂时不支持双层数组解析
                }
            }

            return new MapConfigSource(name, ordinal, map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    /**
     * 递归解析json对象子节点
     * @param node
     * @param map
     * @return
     */
    private Map<String, String> resolveJsonNode(JsonNode node, String prefix, Map<String, String> map) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();
            if (valueNode.isValueNode()) {
                //值类型，作为字符串
                if ("".equals(prefix))
                    map.put(field.getKey(), valueNode.textValue());
                else map.put(String.format("%s.%s", prefix, field.getKey()), valueNode.textValue());
                continue;
            }

            if (valueNode.isObject()) {
                //对象类型,递归解析
                map = resolveJsonNode(valueNode, field.getKey(), map);
                continue;
            }

            if (valueNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) valueNode;

                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode itemNode = arrayNode.get(i);
                    String key = "".equals(prefix) ? String.format("%s[%d]", field.getKey(), i) : String.format("%s.%s[%d]", prefix, field.getKey(), i);
                    if (itemNode.isValueNode()) {
                        map.put(key, itemNode.textValue());
                        continue;
                    }

                    if (itemNode.isObject()) {
                        //递归
                        map = resolveJsonNode(itemNode, key, map);
                        continue;
                    }

                    if (itemNode.isArray()) {
                        //todo 暂时不支持双层数组解析
                    }
                }
            }
        }

        return map;
    }

    @Override
    public String getSupportedProtocol() {
        return "json";
    }
}
