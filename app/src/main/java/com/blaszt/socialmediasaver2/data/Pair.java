package com.blaszt.socialmediasaver2.data;

import java.io.Serializable;

public class Pair implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5508760405710311584L;

    public final static int BY_NAME = 1;
    public final static int BY_DATE = 2;
    public final static int BY_SIZE = 4;
    public final static int BY_TYPE = 8;
    public final static int ORDER_ASCENDING = 16;
    public final static int ORDER_DESCENDING = 32;
}
