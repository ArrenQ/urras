/*
/*
 * Copyright (c) 2015-2016, Chill Zhuang 庄骞 (smallchill@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuang.urras.toolskit.basic;

import com.chuang.urras.toolskit.basic.util.Convert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * 高频方法集合类
 */
public class BasicKit {
    private static final Logger log = LoggerFactory.getLogger(BasicKit.class);
    /**
     * 遍历ary的每个值，并交给c处理
     * @param ary 数组
     * @param c 数组元素处理器
     */
	public static <T> void foreach(T[] ary, Consumer<T> c) {
		if(null == ary) {
			return;
		}
        for (T anAry : ary) {
            c.accept(anAry);
        }
	}

    /**
     * 比较source 和 之后的所有字符串，只有有一个匹配就返回true
     * @param source 源字符串
     * @param eq 需要比较的字符串数组
     * @return 如果eq中有一个字符串和source匹配，就返回true，否则返回false
     */
	public static boolean equalsOr(String source, String... eq) {
		for(String s : eq){
			if(s.equals(source)) {
				return true;
			}
		}

		return false;
	}

    /**
     * 序列化
     * @param object 对象
     * @return 序列化后的byte数组
     */
	public static Optional<byte[]> serialize(@Nullable Object object) {
		if(object == null){
			return Optional.empty();
		}

		ObjectOutputStream oos;
		ByteArrayOutputStream baos;
		try {
			//
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return Optional.ofNullable(bytes);
		} catch (Exception ignored) {

		}
		return Optional.empty();
	}

    /**
     * 将序列化后的数组转成对象
     * @param bytes 字节数组
     * @return 反序列化后的对象
     */
	public static Optional<Object> unSerialize(byte[] bytes) {
		if(bytes == null){
			return Optional.empty();
		}
		ByteArrayInputStream bais;
		try {
			//
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return Optional.ofNullable(ois.readObject());
		} catch (Exception e) {
            log.error("", e);
		}
		return Optional.empty();
	}

	/**
	 * 比较两个对象是否相等。<br>
	 * 相同的条件有两个，满足其一即可：<br>
	 * 1. obj1 == null && obj2 == null; 2. obj1.equals(obj2)
	 * 
	 * @param obj1
	 *            对象1
	 * @param obj2
	 *            对象2
	 * @return 是否相等
	 */
	public static boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
		return Objects.equals(obj1, obj2);
	}

	/**
	 * 计算对象长度，如果是字符串调用其length函数，集合类调用其size函数，数组调用其length属性，其他可遍历对象遍历计算长度
	 * 
	 * @param obj
	 *            被计算长度的对象
	 * @return 长度
	 */
	public static int length(@Nullable Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof CharSequence) {
			return ((CharSequence) obj).length();
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).size();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).size();
		}

		int count;
		if (obj instanceof Iterator) {
			Iterator<?> iter = (Iterator<?>) obj;
			count = 0;
			while (iter.hasNext()) {
				count++;
				iter.next();
			}
			return count;
		}
		if (obj instanceof Enumeration) {
			Enumeration<?> enumeration = (Enumeration<?>) obj;
			count = 0;
			while (enumeration.hasMoreElements()) {
				count++;
				enumeration.nextElement();
			}
			return count;
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj);
		}
		return -1;
	}

	/**
	 * 对象中是否包含元素
	 * 
	 * @param obj
	 *            对象
	 * @param element
	 *            元素
	 * @return 是否包含
	 */
	public static boolean contains(Object obj, Object element) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof String) {
			if (element == null) {
				return false;
			}
			return ((String) obj).contains(element.toString());
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).contains(element);
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).values().contains(element);
		}

		if (obj instanceof Iterator) {
			Iterator<?> iter = (Iterator<?>) obj;
			while (iter.hasNext()) {
				Object o = iter.next();
				if (equals(o, element)) {
					return true;
				}
			}
			return false;
		}
		if (obj instanceof Enumeration) {
			Enumeration<?> enumeration = (Enumeration<?>) obj;
			while (enumeration.hasMoreElements()) {
				Object o = enumeration.nextElement();
				if (equals(o, element)) {
					return true;
				}
			}
			return false;
		}
		if (obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			for (int i = 0; i < len; i++) {
				Object o = Array.get(obj, i);
				if (equals(o, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 对象是否为空
	 * 
	 * @param o
	 *            String,List,Map,Object[],int[],long[]
	 */
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof String) {
            return o.toString().trim().equals("");
		} else if (o instanceof List) {
            return ((List<?>) o).size() == 0;
		} else if (o instanceof Map) {
            return ((Map<?, ?>) o).size() == 0;
		} else if (o instanceof Set) {
            return ((Set<?>) o).size() == 0;
		} else if (o.getClass().isArray()) {
            return ((Object[]) o).length == 0;
		}
		return false;
	}

	/**
	 * 对象组中是否存在 Empty Object
	 * 
	 * @param os
	 *            对象组
	 * @return 如果os中有一个对象是null就返回true，否则返回false
	 */
	public static boolean isOneEmpty(Object... os) {
		for (Object o : os) {
			if (isEmpty(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 对象组中是否全是 Empty Object
	 * 
	 * @param os 需要验证的对象数组
	 * @return 如果os中所有对象都为null就返回true，否则返回false
	 */
	public static boolean isAllEmpty(Object... os) {
		for (Object o : os) {
			if (!isEmpty(o)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否为数字
	 */
	public static boolean isNum(Object obj) {
		try {
			Integer.parseInt(obj.toString());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 如果为空, 则调用默认值
	 */
	public static Object getValue(Object str, Object defaultValue) {
		if (isEmpty(str)) {
			return defaultValue;
		}
		return str;
	}

	/**
	 * 格式化文本
	 * 
	 * @param template
	 *            文本模板，被替换的部分用 {} 表示
	 * @param values
	 *            参数值
	 * @return 格式化后的文本
	 */
	public static String format(String template, Object... values) {
		return StringKit.format(template, values);
	}


	/**
	 * 格式化字符串 去掉前后空格
	 *
	 */
	public static String toStr(@Nullable Object str) {
		if (null == str) {
			return "";
		}
		return str.toString().trim();
	}

	/**
	 * 强转->int
	 *
	 */
	public static int toInt(Object value) {
		return toInt(value, -1);
	}

	/**
	 * 强转->int
	 */
	public static int toInt(Object value, int defaultValue) {
		return Convert.toInt(value, defaultValue);
	}

	/**
	 * 强转->long
	 */
	public static long toLong(Object value) {
		return toLong(value, -1);
	}

	/**
	 * 强转->long
	 *
	 */
	public static long toLong(Object value, long defaultValue) {
		return Convert.toLong(value, defaultValue);
	}

	public static String encodeUrl(String url) {
		return URLKit.encode(url, StringKit.UTF_8);
	}

	public static String decodeUrl(String url) {
		return URLKit.decode(url, StringKit.UTF_8);
	}

	/**
	 * map的key转为小写
	 */
	public static Map<String, Object> caseInsensitiveMap(Map<String, Object> map) {
		Map<String, Object> tempMap = new HashMap<>();
		for (String key : map.keySet()) {
			tempMap.put(key.toLowerCase(), map.get(key));
		}
		return tempMap;
	}
	
	/**
     * 获取map中第一个数据值
     *
     * @param <K> Key的类型
     * @param <V> Value的类型
     * @param map 数据源
     * @return 返回的值
     */
    public static <K, V> V getFirstOrNull(Map<K, V> map) {
        V obj = null;
        for (Entry<K, V> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

	/**
	 * 创建StringBuilder对象
	 * 
	 * @return StringBuilder对象
	 */
	public static StringBuilder builder(String... strs) {
		final StringBuilder sb = new StringBuilder();
		for (String str : strs) {
			sb.append(str);
		}
		return sb;
	}

	/**
	 * 创建StringBuilder对象
	 *
	 */
	public static void builder(StringBuilder sb, String... strs) {
		for (String str : strs) {
			sb.append(str);
		}
	}

	public static boolean isBasicType(Object obj) {
		return obj instanceof Boolean ||
				obj instanceof Byte ||
				obj instanceof Short ||
				obj instanceof Character ||
				obj instanceof Integer ||
				obj instanceof Long ||
				obj instanceof Float ||
				obj instanceof Double;
	}

	public static boolean isBasicTypeOrString(Object obj) {
		return isBasicType(obj) || obj instanceof String;
	}

	public static boolean isBasicToStringType(Object obj) {
		return isBasicTypeOrString(obj) ||
				obj instanceof Enum ||
				obj instanceof Date ||
				obj instanceof LocalDateTime ||
				obj instanceof Number;
	}
}
