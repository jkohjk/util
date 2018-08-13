package com.jkoh.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonUtil {
	private static ObjectMapper jsonMapper = null;
	private static final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static final SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
	
	public static ObjectMapper getJsonMapper () {
		if (jsonMapper == null) {
			SimpleModule simpleModule = new SimpleModule();
			simpleModule.addSerializer(Date.class, new DateSerializer());
			simpleModule.addDeserializer(Date.class, new DateDeserializer());
			simpleModule.setDeserializerModifier(new MapDeserializerModifier());
			jsonMapper = new ObjectMapper();
			jsonMapper.registerModule(simpleModule);
			jsonMapper.setVisibilityChecker(jsonMapper.getSerializationConfig().getDefaultVisibilityChecker()
					.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
					.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
					.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
					.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
					.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
			jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			jsonMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
			jsonMapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());
		}
		return jsonMapper;
	}
	
	public static <T> T cloneObject (T object) {
		return (T)cloneObject(object, object.getClass());
	}
	public static <T> T cloneObject (Object object, Class<T> clazz) {
		return fromJson(toJson(object), clazz);
	}
	
	public static <T> T fromJson (String jsonString, Class<T> clazz) {
		try {
			return getJsonMapper().readValue(jsonString, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static <T> String toJson (T object) {
		try {
			return getJsonMapper().writeValueAsString(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static <T> String toJsonPretty (T object) {
		try {
			return getJsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T fromJsonFile (String parent, String filename, Class<T> clazz) {
		return fromJsonFile(new File(parent, filename), clazz);
	}
	public static <T> T fromJsonFile (String filename, Class<T> clazz) {
		return fromJsonFile(new File(filename), clazz);
	}
	public static <T> T fromJsonFile (File file, Class<T> clazz) {
		try {
			return fromJson(FileUtils.readFileToString(file), clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Custom Json serializer for Date
	 */
	public static class DateSerializer extends JsonSerializer<Date> {
		@Override
		public void serialize (Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeStartObject();
			jgen.writeStringField("$date", format1.format(value));
			jgen.writeEndObject();
		}
	}
	/**
	 * Custom Json deserializer for Date
	 */
	public static class DateDeserializer extends JsonDeserializer<Date> {
		@Override
		public Date deserialize (JsonParser jsonparser, DeserializationContext ctxt) throws IOException {
			TreeNode treeNode = jsonparser.readValueAsTree();
			String date = ((ObjectNode) treeNode).findValue("$date").asText();
			date = date.replace("Z", "+0000");
			try {
				return format1.parse(date);
			} catch (ParseException e) {
				try {
					return format2.parse(date);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}
	}
	
	/**
	 * Custom Json deserializer for Collection<Object> that handles Date properly
	 */
	public static class DateCompatibleCollectionDeserializer extends CollectionDeserializer {
		public DateCompatibleCollectionDeserializer(CollectionDeserializer src) {
			super(src);
		}
		@Override
		protected DateCompatibleCollectionDeserializer withResolved(JsonDeserializer<?> dd, JsonDeserializer<?> vd, 
			TypeDeserializer vtd, NullValueProvider nuller, Boolean unwrapSingle) {
			return new DateCompatibleCollectionDeserializer(super.withResolved(dd, vd, vtd, nuller, unwrapSingle));
		}
		@Override
		public Collection<Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
			Collection<Object> coll = super.deserialize(jp, ctxt);
			deserializeDates(coll);
			return coll;
		}
	}
	/**
	 * Custom Json deserializer for Map<?, Object> that handles Date in values properly
	 */
	public static class DateCompatibleMapDeserializer extends MapDeserializer {
		public DateCompatibleMapDeserializer(MapDeserializer src) {
			super(src);
		}
		@Override
		protected DateCompatibleMapDeserializer withResolved(KeyDeserializer keyDeser, TypeDeserializer valueTypeDeser, 
			JsonDeserializer<?> valueDeser, NullValueProvider nuller, Set<String> ignorable) {
			return new DateCompatibleMapDeserializer(super.withResolved(keyDeser, valueTypeDeser, valueDeser, nuller, ignorable));
		}
		@Override
		public Map<Object, Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
			Map<Object, Object> map = super.deserialize(jp, ctxt);
			deserializeDates(map);
			return map;
		}
	}
	
	/**
	 * Modify deserializers to deserialize Date properly if object type is not specified
	 */
	public static class MapDeserializerModifier extends BeanDeserializerModifier {
		@Override
		public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config, CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
			if(type.getContentType().getRawClass() == Object.class) {
				return new DateCompatibleCollectionDeserializer((CollectionDeserializer)deserializer);
			}
			return deserializer;
		}
		@Override
		public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
			if(type.getContentType().getRawClass() == Object.class) {
				return new DateCompatibleMapDeserializer((MapDeserializer)deserializer);
			}
			return deserializer;
		}
	}
	
	/**
	 * Custom Json deserializer for null keys in maps
	 */
	public static class NullKeySerializer extends StdSerializer<Object> {
		public NullKeySerializer() {
			super(Object.class);
		}
		@Override
		public void serialize (Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeFieldName("");
		}
	}
	
	/**
	 * Recursively convert Collection elements with $date to Date
	 */
	private static void deserializeDates(Collection<Object> coll) {
		if(coll instanceof List) {
			for(ListIterator<Object> iter = ((List)coll).listIterator(); iter.hasNext(); ) {
				Object element = iter.next();
				if(element instanceof Map) {
					if(((Map)element).containsKey("$date")) {
						iter.set(cloneObject(element, Date.class));
					} else {
						deserializeDates((Map)element);
					}
				} else if(element instanceof Collection) {
					deserializeDates((Collection)element);
				}
			}
		} else {
			List<Object> temp = new ArrayList<>(coll);
			deserializeDates(temp);
			coll.clear();
			coll.addAll(temp);
		}
	}
	/**
	 * Recursively convert Map values with $date to Date
	 */
	private static void deserializeDates(Map<Object, Object> map) {
		for(Map.Entry<Object, Object> entry : map.entrySet()) {
			if(entry.getValue() instanceof Map) {
				if(((Map)entry.getValue()).containsKey("$date")) {
					entry.setValue(cloneObject(entry.getValue(), Date.class));
				} else {
					deserializeDates((Map)entry.getValue());
				}
			} else if(entry.getValue() instanceof Collection) {
				deserializeDates((Collection)entry.getValue());
			}
		}
	}
}