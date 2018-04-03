package org.jynergy.cme.protocol;

/*
 * Copyright (c) 2018.
 * Licensed under LPGL v3 (http://www.gnu.org/licenses/lgpl.txt) or Apache License v2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */

/**
 * Is the Protocol that the client and server will use
 */
public interface Protocol {
    /**
     * Written if  to many users tried to logon
     */
    public static char TO_MANY_USERS = 'T';

    /**
     * Written is the user already exists
     */
    public static char USER_EXISTS = 'U';

    /**
     * Beginning char for userList
     */
    public static char USER_LIST = 'L';

    /**
     * Beginning char for message
     */
    public static char MESSAGE = 'M';

    /**
     * Used in {@link #MESSAGE} to seperate the to user(s) from the from user
     */
    public static char SEPERATOR = "\f".charAt( 0 );

    /**
     * Used in {@link #USER_LIST} to seperate users
     */
    public static char COMMA = ',';

    /**
     * Used to designate All users
     */
    public static String ALL = "All";
}
