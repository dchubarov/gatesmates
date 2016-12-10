/*
 * Copyright (c) 2016 Twowls.org.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.twowls.gatesmates.registry;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.twowls.gatesmates.util.Handle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>Provides utility methods to access Windows registry.</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
public final class Registry {

    /** Registry key for the current user */
    public static final Key KEY_CURRENT_USER = Key.forHandle(Api.HKEY_CURRENT_USER);
    /** Registry key for the local machine */
    public static final Key KEY_LOCAL_MACHINE = Key.forHandle(Api.HKEY_LOCAL_MACHINE);

    /**
     * @return {@code true} if registry is available, otherwise {@code false}.
     */
    public static boolean isAvailable() {
        return Platform.isWindows();
    }

    /**
     * <p>Opens a registry key for reading.</p>
     * @param rootKey the root key that sought key belongs to
     * @param subPath sub key path relative to root key
     * @return a {@link Key} instance representing key resource
     * @throws RegistryException if a problem occurred while opening key
     */
    public static Key openKey(Key rootKey, String subPath) throws RegistryException {
        return openKey(rootKey, subPath, false);
    }

    /**
     * <p>Opens a registry key for reading or writing.</p>
     * @param rootKey the root key that sought key belongs to
     * @param subPath sub key path relative to root key
     * @param forWriting {@code true} if write access requested, otherwise {@code false}
     * @return a {@link Key} instance representing key resource
     * @throws RegistryException if a problem occurred while opening key
     */
    public static Key openKey(Key rootKey, String subPath, boolean forWriting)
            throws RegistryException {

        checkAvailable();
        Objects.requireNonNull(rootKey, "Root key must not be null");
        Objects.requireNonNull(subPath, "Sub key path must not be null");

        int[] handleBuffer = createBuffer(0);
        int err = Api.RegOpenKeyExA(rootKey.handle, toWindowsPath(subPath), Api.REG_OPTION_OPEN_LINK,
                (forWriting ? Api.KEY_WRITE : Api.KEY_READ) | Api.KEY_WOW64_64KEY, handleBuffer);

        if (Api.ERROR_SUCCESS != err) {
            throw new RegistryException(err, "Could not open registry key '" + subPath
                    + "' for " + (forWriting ? "writing" : "reading"));
        }

        return Key.forHandle(handleBuffer[0]);
    }

    /**
     * <p>Queries unnamed property value of the given key.</p>
     * @param key a {@link Key} previously open with {@link #openKey(Key, String, boolean)}
     * @param fallbackValue value to return if unnamed value is undefined to the given key
     * @return the value of unnamed property
     * @throws RegistryException if registry is not available or value cannot be read
     */
    public static String queryUnnamedValue(Key key, String fallbackValue)
            throws RegistryException {
        try {
            return queryUnnamedValue(key);
        } catch (RegistryException e) {
            if (Api.ERROR_NOT_FOUND == e.getErrorCode()) {
                return fallbackValue;
            }
            throw e;
        }
    }

    /**
     * <p>Queries unnamed property value of the given key.</p>
     * @param key a {@link Key} previously open with {@link #openKey(Key, String, boolean)}
     * @return the value of unnamed property
     * @throws RegistryException if registry is not available or value cannot be read
     */
    public static String queryUnnamedValue(Key key) throws RegistryException {
        return queryStringValue(key, "");
    }

    /**
     * <p>Queries value of a named textual property ({@code REG_SZ} or similar).</p>
     * @param key registry key previously open with {@link #openKey(Key, String, boolean)}
     * @param valueName the name of the property being queried
     * @param fallbackValue the value to return if property does not actually exists
     * @return property value or {@code fallbackValue} if property does not exist
     * @throws RegistryException if registry is not available or actual property type is not textual
     */
    public static String queryStringValue(Key key, String valueName, String fallbackValue)
            throws RegistryException {
        try {
            return queryStringValue(key, valueName);
        } catch (RegistryException e) {
            if (Api.ERROR_NOT_FOUND == e.getErrorCode()) {
                return fallbackValue;
            }
            throw e;
        }
    }

    /**
     * <p>Queries value of a named textual property ({@code REG_SZ} or similar).</p>
     * @param key registry key previously open with {@link #openKey(Key, String, boolean)}
     * @param valueName the name of the property being queried
     * @return property value or {@code fallbackValue} if property does not exist
     * @throws RegistryException if registry is not available or property does not exists
     *  or actual property type is not textual
     */
    public static String queryStringValue(Key key, String valueName)
            throws RegistryException {

        // first query returns actual type of the property and necessary buffer size
        int[] info = queryValue0(key, valueName, null);
        if (Api.REG_SZ != info[0] && Api.REG_EXPAND_SZ != info[0]) {
            throw new RegistryException(RegistryException.VALUE_TYPE_MISMATCH,
                    "Actual property type is not textual");
        }

        // second query actually read value into the buffer
        byte[] data = new byte[info[1]];
        queryValue0(key, valueName, data);
        return stringFromByteArray(data);
    }

    /**
     * <p>Queries value of a named numeric property ({@code REG_DWORD}).</p>
     * @param key registry key previously open with {@link #openKey(Key, String, boolean)}
     * @param valueName the name of the property being queried
     * @param fallbackValue the value to return if property does not exists
     * @return property value or {@code fallbackValue} if property does not exist
     * @throws RegistryException if registry is not available or actual property type is not numeric
     */
    public static Integer queryIntValue(Key key, String valueName, Integer fallbackValue)
            throws RegistryException {
        try {
            return queryIntValue(key, valueName);
        } catch (RegistryException e) {
            if (Api.ERROR_NOT_FOUND == e.getErrorCode()) {
                return fallbackValue;
            }
            throw e;
        }
    }

    /**
     * <p>Queries value of a named numeric property ({@code REG_DWORD}).</p>
     * @param key registry key previously open with {@link #openKey(Key, String, boolean)}
     * @param valueName the name of the property being queried
     * @return property value or {@code fallbackValue} if property does not exist
     * @throws RegistryException if registry is not available or property does not exists
     *  or actual property type is not numeric
     */
    public static Integer queryIntValue(Key key, String valueName) throws RegistryException {
        // first query returns actual type of the property and necessary buffer size
        int[] info = queryValue0(key, valueName, null);
        if (Api.REG_DWORD != info[0] && Api.REG_DWORD_BIG_ENDIAN != info[0]) {
            throw new RegistryException(RegistryException.VALUE_TYPE_MISMATCH,
                    "Actual property type is not numeric");
        }

        // second query actually read value into the buffer
        byte[] data = new byte[info[1]];
        queryValue0(key, valueName, data);

        // convert byte array to number according to property byte-order
        return ByteBuffer.wrap(data).order(Api.REG_DWORD_BIG_ENDIAN == info[0] ?
                ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static int[] queryValue0(Key key, String valueName, byte[] buffer) throws RegistryException {
        checkAvailable();
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(valueName, "Value name must not be null");

        int[] typeBuffer = createBuffer(0), sizeBuffer = createBuffer(buffer == null ? 0 : buffer.length);
        int err = Api.RegQueryValueExA(key.handle, valueName, null, typeBuffer, buffer, sizeBuffer);
        if (err != Api.ERROR_SUCCESS) {
            throw new RegistryException(err, "Failed to query value '" + valueName + "'");
        }

        return createBuffer(typeBuffer[0], sizeBuffer[0]);
    }

    private static void checkAvailable() throws RegistryException {
        if (!isAvailable()) {
            throw new RegistryException(RegistryException.UNAVAILABLE, "Registry is not available");
        }
    }

    private static String stringFromByteArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            int len = 0;
            for (byte b : bytes) {
                if (b == 0) break;
                len++;
            }
            return new String(bytes, 0, len);
        }
    }

    private static String toWindowsPath(String s) {
        return (s == null ? null : s.replace('/', '\\'));
    }

    private static int[] createBuffer(int... values) {
        return Arrays.copyOf(values, values.length);
    }

    /** Internal representation of a registry key */
    public static class Key implements Handle {

        private int handle;

        private Key(int handle) {
            this.handle = handle;
        }

        @Override
        public void close() throws RegistryException {
            try {
                int result = Api.RegCloseKey(handle);
                if (Api.ERROR_SUCCESS != result) {
                    throw new RegistryException(result, "Could not close key");
                }
            } finally {
                handle = 0;
            }
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + " (hKey = 0x" + Integer.toHexString(handle) + ")";
        }

        /**
         * <p>Creates a new {@link Key} object for the given {@code handle}.</p>
         * @param handle the value of system handle
         * @return a new instance of {@link Key}
         */
        static Key forHandle(int handle) {
            return new Key(handle);
        }
    }

    /** System methods and constants */
    static class Api {

        //
        // Error codes
        //

        static final int ERROR_SUCCESS = 0;
        static final int ERROR_NOT_FOUND = 2;

        //
        // Predefined key handles
        //

        //static final int HKEY_CLASSES_ROOT = 0x80000000;
        static final int HKEY_CURRENT_USER = 0x80000001;
        static final int HKEY_LOCAL_MACHINE = 0x80000002;

        //
        // Registry value types
        //

        //static final int REG_NONE = 0;
        static final int REG_SZ = 1;
        static final int REG_EXPAND_SZ = 2;
        //static final int REG_BINARY = 3;
        static final int REG_DWORD = 4;
        static final int REG_DWORD_BIG_ENDIAN = 5;
        //static final int REG_LINK = 6;
        //static final int REG_MULTI_SZ = 7;

        //
        // Registry key access mask
        //

        //static final int KEY_QUERY_VALUE = 0x1;
        //static final int KEY_SET_VALUE = 0x2;
        //static final int KEY_CREATE_SUB_KEY = 0x4;
        //static final int KEY_ENUMERATE_SUB_KEYS = 0x8;
        //static final int KEY_NOTIFY = 0x10;
        //static final int KEY_CREATE_LINK = 0x20;
        static final int KEY_WOW64_64KEY = 0x100;
        //static final int KEY_WOW64_32KEY = 0x200;
        //static final int KEY_WOW64_RES = 0x300;
        static final int KEY_READ = 0x20019;
        static final int KEY_WRITE = 0x20006;

        //
        // Registry key open mode mask
        //

        static final int REG_OPTION_OPEN_LINK = 0x8;

        //
        // Native methods
        //

        static native int RegOpenKeyExA(int handle, String path, int options, int access, int[] result);

        static native int RegQueryValueExA(int handle, String value, int[] ignore, int[] type, byte[] data, int[] size);

        static native int RegCloseKey(int handle);

        //
        // Native library name
        //

        static final String LIBRARY = "advapi32";

        static {
            if (isAvailable()) {
                Native.register(LIBRARY);
            }
        }
    }

    /* Prohibits instantiation */
    private Registry() {}
}
