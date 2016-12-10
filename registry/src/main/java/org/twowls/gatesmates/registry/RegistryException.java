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

import java.io.IOException;

/**
 * <p>Thrown if a problem accessing registry data has occurred.</p>
 *
 * <p>In addition to message and stacktrace this exception type contains also
 * error code. Positive error codes correspond to system errors and negative ones
 * means internal error occurred in the Java part.</p>
 *
 * <p>For the full list of Windows system error please refer to
 * <a href="https://msdn.microsoft.com/ru-ru/library/windows/desktop/ms681381(v=vs.85).aspx">
 * official documentation</a>.</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 * @see Registry
 */
public class RegistryException extends IOException {

    /** Windows registry is not available (i.e. current operating system is not Windows) */
    public static final int UNAVAILABLE = -1;
    /** Excepted value type does not match the actual */
    public static final int VALUE_TYPE_MISMATCH = -2;

    private final int errorCode;

    /**
     * <p>Creates a new instance of {@link RegistryException}.</p>
     * @param errorCode error code
     */
    public RegistryException(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * <p>Creates a new instance of {@link RegistryException}.</p>
     * @param errorCode error code
     * @param msg error message
     */
    public RegistryException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    /**
     * <p>Creates a new instance of {@link RegistryException}.</p>
     * @param errorCode error code
     * @param msg error message
     * @param cause the cause
     */
    public RegistryException(int errorCode, String msg, Throwable cause) {
        super(msg, cause);
        this.errorCode = errorCode;
    }

    /**
     * @return underlying error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return super.toString() + " (code " + errorCode + ")";
    }
}
