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

import org.twowls.gatesmates.util.GatesConst;

/**
 * <p>Registry specific constants</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
public interface RegistryConst extends GatesConst {

    //
    // Predefined key handles
    //

    int HKEY_CLASSES_ROOT = 0x80000000;
    int HKEY_CURRENT_USER = 0x80000001;
    int HKEY_LOCAL_MACHINE = 0x80000002;

    //
    // Registry value types
    //

    //static final int REG_NONE = 0;
    int REG_SZ = 1;
    int REG_EXPAND_SZ = 2;
    //static final int REG_BINARY = 3;
    int REG_DWORD = 4;
    int REG_DWORD_BIG_ENDIAN = 5;
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
    int KEY_WOW64_64KEY = 0x100;
    //static final int KEY_WOW64_32KEY = 0x200;
    //static final int KEY_WOW64_RES = 0x300;
    int KEY_READ = 0x20019;
    int KEY_WRITE = 0x20006;

    //
    // Registry key open mode mask
    //

    int REG_OPTION_OPEN_LINK = 0x8;
}
