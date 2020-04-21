package com.yp.pay.common.util;

import com.google.common.collect.Lists;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;

public final class EntityConverter {
    public EntityConverter() {
    }

    public static <T> T copyAndGetSingle(Object source, Class<T> type, DateFormat dateFormat) {
        T target = copyAndGetSingle(source, type);
        if (target != null && dateFormat != null) {
            PropertyDescriptor[] sourcePds = BeanUtils.getPropertyDescriptors(source.getClass());
            PropertyDescriptor[] var5 = sourcePds;
            int var6 = sourcePds.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                PropertyDescriptor sourcePd = var5[var7];
                if (sourcePd.getPropertyType().equals(Date.class)) {
                    PropertyDescriptor targetPd = BeanUtils.getPropertyDescriptor(type, sourcePd.getName());
                    if (targetPd != null && targetPd.getPropertyType().equals(String.class)) {
                        Method readMethod = sourcePd.getReadMethod();
                        Method writeMethod = targetPd.getWriteMethod();
                        if (readMethod != null) {
                            try {
                                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                    readMethod.setAccessible(true);
                                }

                                Date value = (Date)readMethod.invoke(source);
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }

                                writeMethod.invoke(target, dateFormat.format(value));
                            } catch (Throwable var13) {
                                throw new FatalBeanException("Could not copy property '" + targetPd.getName() + "' from source to target", var13);
                            }
                        }
                    }
                }
            }
        }

        return target;
    }

    public static <T> T copyAndGetSingle(Object source, Class<T> type) {
        if (source != null && type != null) {
            T target = BeanUtils.instantiateClass(type);
            BeanUtils.copyProperties(source, target);
            return target;
        } else {
            return null;
        }
    }

    public static <T> T copyAndGetSingle(Object source, Class<T> type, String... ignoreProperties) {
        if (source != null && type != null) {
            T target = BeanUtils.instantiateClass(type);
            BeanUtils.copyProperties(source, target, ignoreProperties);
            return target;
        } else {
            return null;
        }
    }

    public static <T> List<T> copyAndGetList(Object source, Class<T> type) {
        List<T> result = Lists.newArrayList();
        if (source != null && type != null && source instanceof List && !((List)source).isEmpty()) {
            Iterator var3 = ((List)source).iterator();

            while(var3.hasNext()) {
                Object obj = var3.next();
                T target = BeanUtils.instantiateClass(type);
                BeanUtils.copyProperties(obj, target);
                result.add(target);
            }

            return result;
        } else {
            return result;
        }
    }

    public static <T> List<T> copyAndGetList(Object source, Class<T> type, String... ignoreProperties) {
        List<T> result = Lists.newArrayList();
        if (source != null && type != null && source instanceof List && !((List)source).isEmpty()) {
            Iterator var4 = ((List)source).iterator();

            while(var4.hasNext()) {
                Object obj = var4.next();
                T target = BeanUtils.instantiateClass(type);
                BeanUtils.copyProperties(obj, target, ignoreProperties);
                result.add(target);
            }

            return result;
        } else {
            return result;
        }
    }
}
