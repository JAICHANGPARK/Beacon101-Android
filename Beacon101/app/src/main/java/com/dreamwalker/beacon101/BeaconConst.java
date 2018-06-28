package com.dreamwalker.beacon101;

public class BeaconConst {

    public static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    public static final String EDDYSTONE_TLM_LAYOUT = "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
    public static final String EDDYSTONE_UID_LAYOUT = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    public static final String EDDYSTONE_URL_LAYOUT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v";
    public static final String URI_BEACON_LAYOUT = "s:0-1=fed8,m:2-2=00,p:3-3:-41,i:4-21v";

    public static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    public static final String I_BEACON = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final String I_BEACON_V2 = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    public static final String EDDYSTON_BEACON_PARSER = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19;d:20-21";



}
