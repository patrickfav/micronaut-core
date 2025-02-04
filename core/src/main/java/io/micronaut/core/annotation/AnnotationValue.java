/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.core.annotation;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A runtime representation of the an annotation and its values.
 *
 * <p>This class implements the {@link AnnotationValueResolver} interface and methods such as {@link AnnotationValueResolver#get(CharSequence, Class)} can be used to retrieve the values of annotation members.</p>
 *
 * <p>If a member is not present then the methods of the class will attempt to resolve the default value for a given annotation member. In this sense the behaviour of this class is similar to how
 * a implementation of {@link Annotation} behaves.</p>
 *
 * @author Graeme Rocher
 * @since 1.0
 * @param <A> The annotation type
 */
public class AnnotationValue<A extends Annotation> implements AnnotationValueResolver {

    private final String annotationName;
    private final ConvertibleValues<Object> convertibleValues;
    private final Map<CharSequence, Object> values;
    private final Map<String, Object> defaultValues;
    private final Function<Object, Object> valueMapper;

    /**
     * @param annotationName The annotation name
     * @param values         The values
     */
    @UsedByGeneratedCode
    public AnnotationValue(String annotationName, Map<CharSequence, Object> values) {
        this.annotationName = annotationName;
        this.convertibleValues = newConvertibleValues(values);
        this.values = values;
        this.defaultValues = Collections.emptyMap();
        this.valueMapper = null;
    }

    /**
     * @param annotationName The annotation name
     * @param values         The values
     * @param defaultValues The default values
     */
    @UsedByGeneratedCode
    public AnnotationValue(String annotationName, Map<CharSequence, Object> values, Map<String, Object> defaultValues) {
        this.annotationName = annotationName;
        this.convertibleValues = newConvertibleValues(values);
        this.values = values;
        this.defaultValues = defaultValues != null ? defaultValues : Collections.emptyMap();
        this.valueMapper = null;
    }

    /**
     * @param annotationName The annotation name
     */
    @SuppressWarnings("unchecked")
    @UsedByGeneratedCode
    public AnnotationValue(String annotationName) {
        this.annotationName = annotationName;
        this.convertibleValues = ConvertibleValues.EMPTY;
        this.values = Collections.emptyMap();
        this.defaultValues = Collections.emptyMap();
        this.valueMapper = null;
    }

    /**
     * @param annotationName    The annotation name
     * @param convertibleValues The convertible values
     */
    public AnnotationValue(String annotationName, ConvertibleValues<Object> convertibleValues) {
        this.annotationName = annotationName;
        this.convertibleValues = convertibleValues;
        Map<String, Object> existing = convertibleValues.asMap();
        this.values = new HashMap<>(existing.size());
        this.values.putAll(existing);
        this.defaultValues = Collections.emptyMap();
        this.valueMapper = null;
    }

    /**
     * Internal copy constructor.
     * @param target The target
     * @param defaultValues The default values
     * @param convertibleValues The convertible values
     * @param valueMapper The value mapper
     */
    @Internal
    @UsedByGeneratedCode
    protected AnnotationValue(AnnotationValue<A> target, Map<String, Object> defaultValues, ConvertibleValues<Object> convertibleValues, Function<Object, Object> valueMapper) {
        this.annotationName = target.annotationName;
        this.defaultValues = defaultValues != null ? defaultValues : target.defaultValues;
        this.values = target.values;
        this.convertibleValues = convertibleValues;
        this.valueMapper = valueMapper;
    }

    /**
     * Return the enum value of the given member of the given enum type.
     *
     * @param member The annotation member
     * @param enumType The required type
     * @return An {@link Optional} of the enum value
     * @param <E> The enum type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends Enum> Optional<E> enumValue(@Nonnull String member, @Nonnull Class<E> enumType) {
        ArgumentUtils.requireNonNull("enumType", enumType);
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o != null) {
                if (enumType.isInstance(o)) {
                    return Optional.of((E) o);
                } else {
                    try {
                        E e = (E) Enum.valueOf(enumType, o.toString());
                        return Optional.of(e);
                    } catch (IllegalArgumentException ex) {
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * The value of the annotation as a Class.
     *
     * @return An {@link Optional} class
     */
    @Override
    public  @Nonnull Optional<Class<?>> classValue() {
        return classValue(AnnotationMetadata.VALUE_MEMBER);
    }

    /**
     * The value of the given annotation member as a Class.
     *
     * @param member The annotation member
     * @return An {@link Optional} class
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<Class<?>> classValue(@Nonnull String member) {
        return classValue(member, valueMapper);
    }

    /**
     * The value of the given annotation member as a Class.
     *
     * @param member The annotation member
     * @param requiredType The required type
     * @return An {@link Optional} class
     * @param <T> The required type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Class<? extends T>> classValue(@Nonnull String member, @Nonnull Class<T> requiredType) {
        ArgumentUtils.requireNonNull("requiredType", requiredType);
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof AnnotationClassValue) {
                Class<?> t = ((AnnotationClassValue<?>) o).getType().orElse(null);
                if (t != null && requiredType.isAssignableFrom(t)) {
                    return Optional.of((Class<? extends T>) t);
                }
                return Optional.empty();
            } else if (o instanceof Class) {
                Class t = (Class) o;
                if (requiredType.isAssignableFrom(t)) {
                    return Optional.of((Class<? extends T>) t);
                }
                return Optional.empty();
            } else if (o != null) {
                Class t = ClassUtils.forName(o.toString(), getClass().getClassLoader()).orElse(null);
                if (t != null && requiredType.isAssignableFrom(t)) {
                    return Optional.of((Class<? extends T>) t);
                }
            }
        }
        return Optional.empty();
    }


    /**
     * The value of the given annotation member as a Class.
     *
     * @param member The annotation member
     * @param valueMapper The raw value mapper
     * @return An {@link Optional} class
     */
    public Optional<Class<?>> classValue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof AnnotationClassValue) {
                return ((AnnotationClassValue) o).getType();
            } else if (o instanceof Class) {
                return Optional.of(((Class) o));
            }
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public String[] stringValues(@Nonnull String member) {
        Function<Object, Object> valueMapper = this.valueMapper;
        return stringValues(member, valueMapper);
    }

    /**
     * The string values for the given member and mapper.
     * @param member The member
     * @param valueMapper The mapper
     * @return The string values
     */
    public String[] stringValues(@Nonnull String member, Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = values.get(member);
            String[] strs = resolveStringValues(o, valueMapper);
            if (strs != null) {
                return strs;
            }
        }
        return StringUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public Class<?>[] classValues(@Nonnull String member) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = values.get(member);
            Class<?>[] type = resolveClassValues(o);
            if (type != null) {
                return type;
            }

        }
        return ReflectionUtils.EMPTY_CLASS_ARRAY;
    }

    /**
     * The integer value of the given member.
     *
     * @param member The annotation member
     * @return An {@link OptionalInt}
     */
    @Override
    public OptionalInt intValue(@Nonnull String member) {
        return intValue(member, valueMapper);
    }

    /**
     * The integer value of the given member.
     *
     * @param member The annotation member
     * @param valueMapper The value mapper
     * @return An {@link OptionalInt}
     */
    public OptionalInt intValue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof Number) {
                return OptionalInt.of(((Number) o).intValue());
            } else if (o instanceof CharSequence) {
                try {
                    return OptionalInt.of(Integer.parseInt(o.toString()));
                } catch (NumberFormatException e) {
                    return OptionalInt.empty();
                }
            }
        }
        return OptionalInt.empty();
    }

    /**
     * The integer value of the given member.
     *
     * @return An {@link OptionalInt}
     */
    @Override
    public OptionalInt intValue() {
        return intValue(AnnotationMetadata.VALUE_MEMBER);
    }

    @Override
    public OptionalLong longValue(@Nonnull String member) {
        return longValue(member, null);
    }

    /**
     * The integer value of the given member.
     *
     * @param member The annotation member
     * @param valueMapper The value mapper
     * @return An {@link OptionalInt}
     */
    public OptionalLong longValue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof Number) {
                return OptionalLong.of((((Number) o).longValue()));
            } else if (o instanceof CharSequence) {
                try {
                    return OptionalLong.of(Long.parseLong(o.toString()));
                } catch (NumberFormatException e) {
                    return OptionalLong.empty();
                }
            }
        }
        return OptionalLong.empty();
    }

    /**
     * The boolean value of the given member.
     *
     * @param member The annotation member
     * @param valueMapper The value mapper
     * @return An {@link Optional} boolean
     */
    public Optional<Boolean> booleanValue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof Boolean) {
                return Optional.of((Boolean) o);
            } else if (o instanceof CharSequence) {
                return Optional.of(StringUtils.isTrue(o.toString()));
            }
        }
        return Optional.empty();
    }

    /**
     * The double value of the given member.
     *
     * @param member The annotation member
     * @return An {@link OptionalDouble}
     */
    @Override
    public OptionalDouble doubleValue(@Nonnull String member) {
        return doubleValue(member, valueMapper);
    }

    /**
     * The double value of the given member.
     *
     * @param member The annotation member
     * @param valueMapper The value mapper
     * @return An {@link OptionalDouble}
     */
    public OptionalDouble doubleValue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof Number) {
                return OptionalDouble.of(((Number) o).doubleValue());
            } else if (o instanceof CharSequence) {
                try {
                    return OptionalDouble.of(Double.parseDouble(o.toString()));
                } catch (NumberFormatException e) {
                    return OptionalDouble.empty();
                }
            }
        }
        return OptionalDouble.empty();
    }

    /**
     * The double value of the given member.
     *
     * @return An {@link OptionalDouble}
     */
    @Override
    public OptionalDouble doubleValue() {
        return doubleValue(AnnotationMetadata.VALUE_MEMBER);
    }

    /**
     * The string value of the given member.
     *
     * @param member The annotation member
     * @return An {@link OptionalInt}
     */
    @Override
    public Optional<String> stringValue(@Nonnull String member) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o != null) {
                return Optional.of(o.toString());
            }
        }
        return Optional.empty();
    }

    /**
     * The string value of the given member.
     *
     * @param member The annotation member
     * @param valueMapper An optional raw value mapper
     * @return An {@link OptionalInt}
     */
    public Optional<String> stringValue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o != null) {
                return Optional.of(o.toString());
            }
        }
        return Optional.empty();
    }

    /**
     * The double value of the given member.
     *
     * @return An {@link OptionalInt}
     */
    @Override
    public Optional<String> stringValue() {
        return stringValue(AnnotationMetadata.VALUE_MEMBER);
    }

    @Override
    public Optional<Boolean> booleanValue(@Nonnull String member) {
        return booleanValue(member, null);
    }

    /**
     * Is the given member present.
     * @param member The member
     * @return True if it is
     */
    @Override
    public final boolean isPresent(CharSequence member) {
        if (StringUtils.isNotEmpty(member)) {
            return values.containsKey(member);
        }
        return false;
    }

    /**
     * @return Is the value of the annotation true.
     */
    @Override
    public boolean isTrue() {
        return isTrue(AnnotationMetadata.VALUE_MEMBER);
    }

    /**
     * @param member The member
     *
     * @return Is the value of the annotation true.
     */
    @Override
    public boolean isTrue(String member) {
        return isTrue(member, valueMapper);
    }

    /**
     * @param member The member
     * @param valueMapper The value mapper
     * @return Is the value of the annotation true.
     */
    public boolean isTrue(@Nonnull String member, @Nullable Function<Object, Object> valueMapper) {
        if (StringUtils.isNotEmpty(member)) {
            Object o = getRawSingleValue(member, valueMapper);
            if (o instanceof Boolean) {
                return (Boolean) o;
            } else if (o != null) {
                return StringUtils.isTrue(o.toString());
            }
        }
        return false;
    }


    /**
     * @return Is the value of the annotation true.
     */
    @Override
    public boolean isFalse() {
        return !isTrue(AnnotationMetadata.VALUE_MEMBER);
    }

    /**
     * @param member The member
     *
     * @return Is the value of the annotation true.
     */
    @Override
    public boolean isFalse(String member) {
        return !isTrue(member);
    }

    /**
     * The annotation name.
     *
     * @return The annotation name
     */
    public @Nonnull final String getAnnotationName() {
        return annotationName;
    }

    /**
     * Whether a particular member is present.
     * @param member The member
     * @return True if it is
     */
    public final boolean contains(String member) {
        return isPresent(member);
    }

    /**
     * Resolves the names of all the present annotation members.
     *
     * @return The names of the members
     */
    public @Nonnull final Set<CharSequence> getMemberNames() {
        return values.keySet();
    }

    /**
     * @return The attribute values
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nonnull Map<CharSequence, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * @return The convertible values
     */
    public @Nonnull ConvertibleValues<Object> getConvertibleValues() {
        return convertibleValues;
    }

    @Override
    public <T> Optional<T> get(CharSequence member, ArgumentConversionContext<T> conversionContext) {
        Optional<T> result = convertibleValues.get(member, conversionContext);
        if (!result.isPresent()) {
            Object dv = defaultValues.get(member.toString());
            if (dv != null) {
                return ConversionService.SHARED.convert(dv, conversionContext);
            }
        }
        return result;
    }

    /**
     * Get the value of the {@code value} member of the annotation.
     *
     * @param conversionContext The conversion context
     * @param <T> The type
     * @return The result
     */
    public <T> Optional<T> getValue(ArgumentConversionContext<T> conversionContext) {
        return get(AnnotationMetadata.VALUE_MEMBER, conversionContext);
    }

    /**
     * Get the value of the {@code value} member of the annotation.
     *
     * @param argument The argument
     * @param <T> The type
     * @return The result
     */
    public final <T> Optional<T> getValue(Argument<T> argument) {
        return getValue(ConversionContext.of(argument));
    }

    /**
     * Get the value of the {@code value} member of the annotation.
     *
     * @param type The type
     * @param <T> The type
     * @return The result
     */
    public final <T> Optional<T> getValue(Class<T> type) {
        return getValue(ConversionContext.of(type));
    }

    /**
     * Get the value of the {@code value} member of the annotation.
     *
     * @param type The type
     * @param <T> The type
     * @throws IllegalStateException If no member is available that conforms to the given type
     * @return The result
     */
    public @Nonnull final <T> T getRequiredValue(Class<T> type) {
        return getRequiredValue(AnnotationMetadata.VALUE_MEMBER, type);
    }

    /**
     * Get the value of the {@code value} member of the annotation.
     *
     * @param member The member
     * @param type The type
     * @param <T> The type
     * @throws IllegalStateException If no member is available that conforms to the given name and type
     * @return The result
     */
    public @Nonnull final <T> T getRequiredValue(String member, Class<T> type) {
        return get(member, ConversionContext.of(type)).orElseThrow(() -> new IllegalStateException("No value available for annotation member @" + annotationName + "[" + member + "] of type: " + type));
    }

    /**
     * Gets a list of {@link AnnotationValue} for the given member.
     *
     * @param member The member
     * @param type The type
     * @param <T> The type
     * @throws IllegalStateException If no member is available that conforms to the given name and type
     * @return The result
     */
    public @Nonnull <T extends Annotation> List<AnnotationValue<T>> getAnnotations(String member, Class<T> type) {
        ArgumentUtils.requireNonNull("member", member);
        ArgumentUtils.requireNonNull("type", type);
        Object v = values.get(member);
        AnnotationValue[] values = null;
        if (v instanceof AnnotationValue) {
            values = new AnnotationValue[] {(AnnotationValue) v};
        } else if (v instanceof AnnotationValue[]) {
            values = (AnnotationValue[]) v;
        }
        if (ArrayUtils.isNotEmpty(values)) {
            List<AnnotationValue<T>> list = new ArrayList<>(values.length);
            String typeName = type.getName();
            for (AnnotationValue value : values) {
                if (value == null) {
                    continue;
                }
                if (value.getAnnotationName().equals(typeName)) {
                    //noinspection unchecked
                    list.add(value);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Gets a list of {@link AnnotationValue} for the given member.
     *
     * @param member The member
     * @param <T> The type
     * @throws IllegalStateException If no member is available that conforms to the given name and type
     * @return The result
     */
    @SuppressWarnings("unchecked")
    public @Nonnull <T extends Annotation> List<AnnotationValue<T>> getAnnotations(String member) {
        ArgumentUtils.requireNonNull("member", member);
        Object v = values.get(member);
        if (v instanceof AnnotationValue) {
            return Collections.singletonList((AnnotationValue) v);
        } else if (v instanceof AnnotationValue[]) {
            return Arrays.asList((AnnotationValue[]) v);
        }
        return Collections.emptyList();
    }

    /**
     * Gets a list of {@link AnnotationValue} for the given member.
     *
     * @param member The member
     * @param type The type
     * @param <T> The type
     * @throws IllegalStateException If no member is available that conforms to the given name and type
     * @return The result
     */
    public @Nonnull final <T extends Annotation> Optional<AnnotationValue<T>> getAnnotation(String member, Class<T> type) {
        return getAnnotations(member, type).stream().findFirst();
    }

    @Override
    public int hashCode() {
        return 31 * annotationName.hashCode() + AnnotationUtil.calculateHashCode(getValues());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!AnnotationValue.class.isInstance(obj)) {
            return false;
        }

        AnnotationValue other = AnnotationValue.class.cast(obj);

        if (!annotationName.equals(other.getAnnotationName())) {
            return false;
        }

        Map<CharSequence, Object> otherValues = other.getValues();
        Map<CharSequence, Object> values = getValues();
        if (values.size() != otherValues.size()) {
            return false;
        }

        // compare annotation member values
        for (Map.Entry<CharSequence, Object> member : values.entrySet()) {
            Object value = member.getValue();
            Object otherValue = otherValues.get(member.getKey());

            if (!AnnotationUtil.areEqual(value, otherValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Start building a new annotation for the given name.
     *
     * @param annotationName The annotation name
     * @param <T> The annotation type
     * @return The builder
     */
    public static <T extends Annotation> AnnotationValueBuilder<T> builder(String annotationName) {
        return new AnnotationValueBuilder<>(annotationName);
    }

    /**
     * Start building a new annotation for the given name.
     *
     * @param annotation The annotation name
     * @param <T> The annotation type
     * @return The builder
     */
    public static <T extends Annotation> AnnotationValueBuilder<T> builder(Class<T> annotation) {
        return new AnnotationValueBuilder<>(annotation);
    }

    /**
     * The string values for the given value.
     * @param value The value
     * @param valueMapper The value mapper
     * @return The string[] or null
     */
    @Internal
    public static @Nullable String[] resolveStringValues(@Nullable Object value, @Nullable Function<Object, Object> valueMapper) {
        if (value == null) {
            return null;
        }
        if (valueMapper != null) {
            value = valueMapper.apply(value);
        }
        if (value instanceof CharSequence) {
            return new String[] { value.toString() };
        } else if (value instanceof String[]) {
            String[] strs = (String[]) value;
            return resolveStringArray(strs, valueMapper);
        } else if (value != null) {
            if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                String[] newArray = new String[len];
                for (int i = 0; i < newArray.length; i++) {
                    Object entry = Array.get(value, i);
                    if (entry != null) {
                        newArray[i] = entry.toString();
                    }
                }
                return newArray;
            } else {
                return new String[] { value.toString() };
            }
        }
        return null;
    }

    /**
     * The string[] values for the given value.
     * @param strs The strings
     * @param valueMapper The value mapper
     * @return The string[] or the original string
     */
    @Internal
    public static String[] resolveStringArray(String[] strs, @Nullable Function<Object, Object> valueMapper) {
        if (valueMapper != null) {
            String[] newStrs = new String[strs.length];
            for (int i = 0; i < strs.length; i++) {
                String str = strs[i];
                newStrs[i] = valueMapper.apply(str).toString();
            }
            return newStrs;
        } else {
            return strs;
        }
    }

    /**
     * The classes class values for the given value.
     * @param value The value
     * @return The class values or null
     */
    @Internal
    public static @Nullable Class<?>[] resolveClassValues(@Nullable Object value) {
        // conditional branches ordered from most likely to least likely
        // generally at runtime values are always AnnotationClassValue
        // A class can be present at compilation time
        if (value instanceof AnnotationClassValue) {
            Class<?> type = ((AnnotationClassValue<?>) value).getType().orElse(null);
            if (type != null) {
                return new Class[] { type };
            }
        } else if (value instanceof AnnotationClassValue[]) {
            AnnotationClassValue<?>[] values = (AnnotationClassValue<?>[]) value;
            return Arrays.stream(values).flatMap(av -> {
                Optional<? extends Class<?>> t = av.getType();
                return t.map(Stream::of).orElse(Stream.empty());
            }).toArray(Class[]::new);
        } else if (value instanceof Class) {
            return new Class[] {(Class) value};
        } else if (value instanceof Class[]) {
            return (Class<?>[]) value;
        }
        return null;
    }

    /**
     * Subclasses can override to provide a custom convertible values instance.
     *
     * @param values The values
     * @return The instance
     */
    private ConvertibleValues<Object> newConvertibleValues(Map<CharSequence, Object> values) {
        if (CollectionUtils.isEmpty(values)) {
            return ConvertibleValues.EMPTY;
        } else {
            return ConvertibleValues.of(values);
        }
    }

    private @Nullable Object getRawSingleValue(@Nonnull String member, Function<Object, Object> valueMapper) {
        Object rawValue = values.get(member);
        if (rawValue != null) {
            if (rawValue.getClass().isArray()) {
                int len = Array.getLength(rawValue);
                if (len > 0) {
                    rawValue = Array.get(rawValue, 0);
                }
            }
        }
        if (valueMapper != null && rawValue instanceof String) {
            return valueMapper.apply(rawValue);
        }
        return rawValue;
    }
}
