package net.issachanzi.resteasy.model;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.annotation.NoHttp;

import java.lang.reflect.*;

/**
 * A field of an EasyModel exposed over the REST API
 *
 * <p>
 *     Intended to abstract the getting and setting of fields that may or may
 *     not have custom getters and setters
 * </p>
 *
 * @param <T> The type of the field
 */
public class HttpField <T> {
    private final String name;
    private final Class <?> type;
    private final Getter <T> getter;
    private final Setter <T> setter;

    /**
     * Constructs a HttpField with a specified getter and setter
     *
     * @param getter A function to get the value of the field
     * @param setter A function to set the value of the field
     */
    public HttpField (
            String name,
            Class<?> type,
            Getter <T> getter,
            Setter <T> setter
    ) {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    public String name() {
        return this.name;
    }

    public Class<?> type() {
        return this.type;
    }

    /**
     * Checks if this {@code HttpField} has a getter.
     *
     * @return {@code true} if a getter is present, {@code false} if it is not.
     */
    public boolean canGet () {
        return getter != null;
    }

    /**
     * Checks if this {@code HttpField} has a setter.
     *
     * @return {@code true} if a setter is present, {@code false} if it is not.
     */
    public boolean canSet () {
        return setter != null;
    }

    /**
     * Gets the value of this field
     *
     * @param model The model instance to get the value from.
     * @return The value of this field
     * @throws HttpErrorStatus If a custom getter throws an exception
     */
    public T get (EasyModel model) throws HttpErrorStatus {
        return getter.get(model);
    }

    /**
     * Sets the value of this field
     *
     * @param model The model instance to set the value from.
     * @param value The value to set for this field
     * @throws HttpErrorStatus If a custom getter throws an exception
     */
    public void set (EasyModel model, Object value) throws HttpErrorStatus {
        setter.set (model, (T) value);
    }

    /**
     * A functional interface for a getter method.
     *
     * @param <T> The type of field to get
     */
    @FunctionalInterface
    public interface Getter <T> {
        T get (EasyModel model) throws HttpErrorStatus;
    }

    /**
     * A functional interface for a setter method.
     *
     * @param <T> The type of field to set
     */
    @FunctionalInterface
    public interface Setter <T> {
        void set (EasyModel model, T value) throws HttpErrorStatus;
    }

    /**
     * Constructs a HttpField for a given field.
     *
     * <p>
     *      Scans for custom getters and
     *      setters in the class and uses those if they exist. Otherwise, just gets
     *      and sets the value of the field directly.
     * </p>
     *
     * <p>
     *     Both traditional ({@code int getFoo()}, {@code void setFoo(int foo)})
     *     and records style ({@code void foo(int foo)}, {@code int foo()}) are
     *     supported. If both are present, traditional style methods take
     *     priority.
     * </p>
     *
     * @param clazz The class containing the field to get and set
     * @param field The field to get and set
     * @param fieldType The type of field to get and set
     * @return The new {@code HttpField}
     * @param <T> The type of field to get and set
     */
    public static <T> HttpField <T> forField (
            Class <? extends EasyModel> clazz,
            Field field,
            Class <T> fieldType
    ) {
        if (fieldType != field.getType()) {
            throw new IllegalArgumentException();
        }

        String name = field.getName();
        Class<?> type = field.getType();
        Getter <T> getter = findGetter (clazz, field, fieldType);
        Setter <T> setter = findSetter (clazz, field, fieldType);

        return new HttpField<T>(name, type, getter, setter);
    }

    @SuppressWarnings("unchecked")
    public T asFieldType (Object obj) throws ClassCastException {
        return (T) obj;
    }

    public boolean isFieldType (Object obj) {
        try {
            var unused = (T) obj;
            return true;
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /**
     * Checks if a member is {@code public}
     *
     * @param member The member to check visibility of.
     * @return {@code true} if the member is public, {@code false} if it is not.
     */
    public static boolean isPublic(Member member) {
        return (member.getModifiers() & Modifier.PUBLIC) != 0;
    }

    @SuppressWarnings("unchecked")
    private static <T> Getter<T> findGetter(
            Class<? extends EasyModel> clazz,
            Field field,
            Class <T> fieldType
    ) {
        String methodNameRecord = field.getName();
        String methodNameGet    = "get"
                                + methodNameRecord.substring(0, 1)
                                    .toUpperCase()
                                + methodNameRecord.substring(1);
        Getter <T> result = null;

        Method method = null;
        try {
            method = clazz.getMethod(methodNameGet);
            if (!isPublic(method)) {
                method = null;
                throw new NoSuchMethodException();
            }
        }
        catch (NoSuchMethodException ex) {
            try {
                method = clazz.getMethod(methodNameRecord);
                if (!isPublic(method)) {
                    method = null;
                    throw new NoSuchMethodException();
                }
            }
            catch (NoSuchMethodException ignored) {}
        }

        if (method != null) {
            Method finalMethod = method;
            result = model -> {
                try {
                    return (T) finalMethod.invoke(model);
                } catch (IllegalAccessException |
                         InvocationTargetException |
                         ClassCastException ex
                ) {
                    throw new RuntimeException(ex);
                }
            };
        }
        else if (isPublic (field)){
            result = model -> {
                try {
                    return (T) field.get(model);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        else {
            result = model -> null;
        }

        return result;
    }

    private static <T> Setter<T> findSetter(
            Class<? extends EasyModel> clazz,
            Field field,
            Class <T> fieldType
    ) {
        String methodNameRecord = field.getName();
        String methodNameSet = "set"
                + methodNameRecord.substring(0, 1)
                .toUpperCase()
                + methodNameRecord.substring(1);
        Setter <T> result;

        Method method = null;
        try {
            method = clazz.getMethod(methodNameSet, fieldType);
            if (!isPublic(method)) {
                method = null;
                throw new NoSuchMethodException();
            }
        }
        catch (NoSuchMethodException ex) {
            try {
                method = clazz.getMethod(methodNameRecord, fieldType);
                if (!isPublic(method)) {
                    method = null;
                    throw new NoSuchMethodException();
                }
            }
            catch (NoSuchMethodException ignored) {}
        }

        if (field.getAnnotation(NoHttp.class) != null) {
            result = null;
        }
        else if (method != null) {
            Method finalMethod = method;
            result = (model, value) -> {
                try {
                    finalMethod.invoke(model, value);
                } catch (IllegalAccessException |
                         InvocationTargetException ex
                ) {
                    throw new RuntimeException(ex);
                }
            };
        }
        else if (isPublic (field)){
            result = (model, value) -> {
                try {
                    field.set(model, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        else {
            result = null;
        }

        return result;
    }
}
