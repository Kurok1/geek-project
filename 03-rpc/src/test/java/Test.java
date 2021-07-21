import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.kurok1.rest.converter.JacksonJsonHttpBodyConverter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.21
 */
public class Test {

    public static void main(String[] args) {
        JacksonJsonHttpBodyConverter converter = new JacksonJsonHttpBodyConverter();
        ArrayList<User> data = new ArrayList<>();
        for (int i = 0;i<3;i++) {
            // HashMap<String, User> entry = new HashMap<>();
            // entry.put("a", User.of("a", i));
            // data.add(entry);
            data.add(User.of("a", i));
        }
        System.out.println(resolveConvertedType(data.getClass().getGenericSuperclass()));
        JavaType javaType = converter.getJavaType(data.getClass(), data.getClass().getGenericSuperclass());
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(data);
            System.out.println(json);
            List<User> result = mapper.readValue(json, javaType);
            System.out.println(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Class<?> resolveConvertedType(Type type) {
        Class<?> convertedType = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            if (pType.getRawType() instanceof Class) {
                Class<?> rawType = (Class) pType.getRawType();
                if (List.class.isAssignableFrom(rawType)) {
                    Type[] arguments = pType.getActualTypeArguments();
                    if (arguments.length == 1 && arguments[0] instanceof Class) {
                        convertedType = (Class) arguments[0];
                    }
                }
            }
        }
        return convertedType;
    }

    public static class User {
        private String name = "user";
        private int age = 18;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public static User of(String name, int age) {
            User user = new User();
            user.setAge(age);
            user.setName(name);
            return user;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

}
