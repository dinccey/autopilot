#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11);  // RX, TX
char inputByte = 'z';
uint8_t LEFT = 5;
uint8_t RIGHT = 6;

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);  // There's a LED that turn on if there are input data
  pinMode(LEFT, OUTPUT);//left
  pinMode(RIGHT, OUTPUT);//right
  Serial.begin(9600);
  mySerial.begin(9600);
}

void loop() {  // run over and over

  while (mySerial.available()) {
    inputByte = mySerial.read();
    Serial.println(inputByte);
    if (inputByte == 'L') {
      leftTurn();
    } else if (inputByte == 'R') {
      rightTurn();
    }else if(inputByte == 'N'){
      stopTurn();
    }
    //mySerial.flush();
    delay(100);
  }
  stopTurn();
}

void leftTurn(){  
  digitalWrite(RIGHT,LOW);  
  digitalWrite(LEFT,HIGH);
  digitalWrite(LED_BUILTIN, HIGH);
}

void rightTurn(){
  digitalWrite(LEFT,LOW);
  digitalWrite(RIGHT,HIGH);
  digitalWrite(LED_BUILTIN, HIGH);
}
void stopTurn(){
  digitalWrite(LEFT,LOW);
  digitalWrite(RIGHT,LOW);
  digitalWrite(LED_BUILTIN, LOW);
}