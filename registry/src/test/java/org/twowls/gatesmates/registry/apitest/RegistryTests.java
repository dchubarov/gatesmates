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

package org.twowls.gatesmates.registry.apitest;

import org.junit.Assert;
import org.junit.Test;
import org.twowls.gatesmates.registry.Registry;
import org.twowls.gatesmates.registry.RegistryConst;
import org.twowls.gatesmates.registry.RegistryException;

import static org.junit.Assert.*;

/**
 * <p>High level registry tests.</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
public abstract class RegistryTests {

    protected static final String EXISTENT_SUB_KEY = "Windows/CurrentVersion";
    protected static final String EXISTENT_SUB_SUB_KEY = "Explorer";
    protected static final String NON_EXISTENT_SUB_KEY = "$$NON-EXISTENT$$";

    protected static final String UNNAMED_PROPERTY = "";
    protected static final String UNNAMED_PROPERTY_VALUE = "String-Value";

    protected static final String NAMED_STRING_PROPERTY = "Named-Property";
    protected static final String NAMED_STRING_PROPERTY_VALUE = "Named-String-Property-Value";

    protected static final String NAMED_DWORD_PROPERTY = "Named-Int-Property";
    protected static final int NAMED_DWORD_PROPERTY_VALUE = 1234;

    @Test
    public void openNonExistentKeyShouldThrowNotFoundError() throws RegistryException {
        Registry.Key key = null;
        int errorCode = 0;
        try {
            key = Registry.openKey(Registry.KEY_LOCAL_MACHINE, NON_EXISTENT_SUB_KEY);
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
            errorCode = e.getErrorCode();
        }
        Assert.assertEquals(RegistryConst.ERROR_NOT_FOUND, errorCode);
        Registry.closeKey(key);
    }

    @Test
    public void testOpenSubKeyOfNonRootKey() throws RegistryException {
        try (Registry.Key key = Registry.openKey(Registry.KEY_CURRENT_USER, EXISTENT_SUB_KEY)) {
            try (Registry.Key childKey = key.openSubKey(EXISTENT_SUB_SUB_KEY)) {
                System.out.println("ChildKey = " + childKey.toString());
                assertFalse(childKey.toString().endsWith("x0)"));
            }
        }
    }

    @Test
    public void testOpenSubKeyOfPredefinedKey() throws RegistryException {
        try (Registry.Key childKey = Registry.KEY_CURRENT_USER.openSubKey(EXISTENT_SUB_KEY, false)) {
            System.out.println("ChildKey = " + childKey.toString());
            assertFalse(childKey.toString().endsWith("x0)"));
        }
    }

    @Test
    public void openKeyShouldReturnHandle() throws RegistryException {
        try (Registry.Key key = Registry.openKey(Registry.KEY_CURRENT_USER, EXISTENT_SUB_KEY, false)) {
            System.out.println("Key = " + key.toString());
            assertFalse(key.toString().endsWith("x0)"));
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    @Test
    public void queryUnnamedPropertyReturnsStringValue() throws RegistryException {
        try (Registry.Key key = Registry.openKey(Registry.KEY_CURRENT_USER, EXISTENT_SUB_KEY, false)) {
            System.out.println("Key = " + key.toString());
            assertFalse(key.toString().endsWith("x0)"));

            String value = key.queryUnnamedValue();
            System.out.println("Unnamed value = '" + value + "'");
            assertTrue(value != null && !value.isEmpty());

            assertEquals(value, key.queryUnnamedValue(null));
            assertEquals(value, key.queryUnnamedValue("Never-Returned"));
            assertEquals(value, Registry.queryUnnamedValue(key));
            assertEquals(value, Registry.queryUnnamedValue(key, null));
            assertEquals(value, Registry.queryUnnamedValue(key, "Never-Returned"));
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    @Test
    public void queryNamedPropertyReturnsStringValue() throws RegistryException {
        try (Registry.Key key = Registry.openKey(Registry.KEY_CURRENT_USER, EXISTENT_SUB_KEY, false)) {
            System.out.println("Key = " + key.toString());
            assertFalse(key.toString().endsWith("x0)"));

            String value = key.queryStringValue(NAMED_STRING_PROPERTY);
            System.out.println("Named property '" + NAMED_STRING_PROPERTY + "' value = '" + value + "'");
            assertTrue(value != null && !value.isEmpty());

            assertEquals(value, key.queryStringValue(NAMED_STRING_PROPERTY,null));
            assertEquals(value, key.queryStringValue(NAMED_STRING_PROPERTY,"Never-Returned"));
            assertEquals(value, Registry.queryStringValue(key, NAMED_STRING_PROPERTY));
            assertEquals(value, Registry.queryStringValue(key, NAMED_STRING_PROPERTY,null));
            assertEquals(value, Registry.queryStringValue(key, NAMED_STRING_PROPERTY,"Never-Returned"));
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    @Test
    public void testQueryNamedIntValue() throws RegistryException {
        try (Registry.Key key = Registry.openKey(Registry.KEY_CURRENT_USER, EXISTENT_SUB_KEY, false)) {
            System.out.println("Key = " + key.toString());
            assertFalse(key.toString().endsWith("x0)"));

            int value = key.queryIntValue(NAMED_DWORD_PROPERTY);
            System.out.println("Named property '" + NAMED_DWORD_PROPERTY + "' value = '" + value + "'");
            assertEquals(value, NAMED_DWORD_PROPERTY_VALUE);

            assertEquals(Integer.valueOf(value), key.queryIntValue(NAMED_DWORD_PROPERTY, null));
            assertEquals(Integer.valueOf(value), key.queryIntValue(NAMED_DWORD_PROPERTY, 0));
            assertEquals(value, Registry.queryIntValue(key, NAMED_DWORD_PROPERTY));
            assertEquals(Integer.valueOf(value), Registry.queryIntValue(key, NAMED_DWORD_PROPERTY,null));
            assertEquals(Integer.valueOf(value), Registry.queryIntValue(key, NAMED_DWORD_PROPERTY, 0));
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }
}
