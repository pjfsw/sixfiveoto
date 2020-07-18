#include "headers.h"
#include <SPI.h>
#include "GD.h"

#define GDSEL 9

int img = 63;
int spriteNo = 255;

void gd_write(unsigned int addr) {
  digitalWrite(GDSEL, LOW);
  SPI.transfer(0x80 | (addr >> 8));
  SPI.transfer(addr & 0xff);
}

void gd_read(unsigned int addr) {
  digitalWrite(GDSEL, LOW);
  SPI.transfer((addr >> 8));
  SPI.transfer(addr & 0xff);
}

void gd_end() {
  digitalWrite(GDSEL, HIGH);
}

void init_gd() {
  delay(250);
  pinMode(GDSEL, OUTPUT);
  SPI.begin();
  SPI.setClockDivider(SPI_CLOCK_DIV2);
  SPI.setBitOrder(MSBFIRST);
  SPI.setDataMode(SPI_MODE0);
  SPSR = (1 << SPI2X);
  digitalWrite(GDSEL, HIGH);
}

void dump() {
  Serial.begin(115200);

  char output[8];

  gd_read(0);
  int mod = 16;

  Serial.println("--BEGIN DATA--");
  for (unsigned int i = 0; i < 0x8000; i++) {
    int v = SPI.transfer(0);
    if (i % mod == 0) {
      Serial.println();
      //sprintf(output, "%04X ", i);
      //Serial.print(output);
    }
    sprintf(output, "%02X, ", v);
    Serial.print(output);
  }
  Serial.println();
  Serial.println("--END DATA--");
  Serial.end();
  gd_end();
}

void sprite(int x, int y) {
  // Sprite position
  gd_write(0x3000 + spriteNo*4);
  SPI.transfer(x & 255);
  SPI.transfer(0x80 | (x >> 8) | (2*5));
  SPI.transfer(y & 255);
  SPI.transfer((y >> 8) | (img << 1));
  gd_end();
}


void drawSprite() {

  // SPrite image data
  gd_write(0x4000 + 256 * img);
  for (int i = 0; i < 240; i++) {
    SPI.transfer(i % 4);
  }
  for (int i = 0; i < 8; i++) {
    SPI.transfer(1);
  }
  for (int i = 0; i < 8; i++) {
    SPI.transfer(0);
  }

  gd_end();

  int x = 10;
  int y = 10;
  
  // Sprite palette
  gd_write(0x2880);
  SPI.transfer(0);
  SPI.transfer(0);

  SPI.transfer(0xFF);
  SPI.transfer(0x7F);

  SPI.transfer(0x1F);
  SPI.transfer(0x00);

  SPI.transfer(0x0F);
  SPI.transfer(0x7F);
  gd_end();
}

void scroll() {
  gd_write(0x2804);
  SPI.transfer(0);
  SPI.transfer(0);
  SPI.transfer(0);  
  gd_end();
}

void setup() {
  delay(2000);
  init_gd();
  gd_write(0);
  char *s = "GAMEDUINO ASCII TEST FROM 6502!";
  do {
    SPI.transfer(*s);
    s++;
  } while (*s != 0);
  gd_end();
  gd_write(0x280B);
  SPI.transfer(0);
  gd_end();
  drawSprite();
  scroll();
}

void loop() {
    //sprite(0,i);
    //delay(16);
    gd_read(0x2802);
    int frame = SPI.transfer(0);
    gd_end();
    char s[4];
    sprintf(s, "%02X", frame);
    gd_write(64);
    SPI.transfer(s[0]);
    SPI.transfer(s[1]);
    gd_end();
    sprite(frame,frame);

  //delay(2000);
  //gd_write(0x280A);
  //SPI.transfer(1);
  //gd_end();
  //delay(2000);
  //gd_write(0x280A);
  //SPI.transfer(0);
  //gd_end();
}

int main(int argc, char **argv) {
  init();
  setup();
  while (1) {
    loop();
  }
  return 0;
}
