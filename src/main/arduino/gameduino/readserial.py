#!/usr/bin/python3 

import serial
import io

ser = serial.Serial('/dev/ttyACM0', 115200, timeout=1)
sio = io.TextIOWrapper(ser)

while True:
    #print(ser.readline())
    print(sio.readline().rstrip())
    sio.flush()

