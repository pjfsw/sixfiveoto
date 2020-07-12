#include "headers.h"
#include <SPI.h>
#include "GD.h"

#define GDSEL 9

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
void setup() {
  init_gd();
  Serial.begin(115200);

  char output[8];

  gd_read(0);
  int mod = 16;

  Serial.println("--BEGIN DATA--");
  for (int i = 0; i < 0x2900; i++) {
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

void loop() {
}

int main(int argc, char **argv) {
  init();
  setup();
  while (1) {
    loop();
  }
  return 0;
}
